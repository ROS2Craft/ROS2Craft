package net.gitsrealpe.ros2craft.ros;

import edu.wpi.rail.jrosbridge.Ros;
import net.gitsrealpe.ros2craft.ROS2Craft;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton manager for the ROS connection.
 * Ensures only one WebSocket connection to ROS is maintained,
 * shared across all robot entities.
 */
public class ROSManager {
    private static ROSManager instance;
    private Ros ros;
    private int activeRobots = 0;

    // === NEW: Connection listeners ===
    public interface ConnectionListener {
        /**
         * Called when the connection is successfully established
         */
        void onROSConnected();

        /** Called when the connection is closed */
        void onROSDisconnected();

        /** Called when the connection is lost */
        void onROSLostConnection();

        /** Called on connection errors or failed reconnect attempts */
        void onROSConnectionError(String message);
    }

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();

    // === NEW: Reconnection scheduler ===
    private ScheduledExecutorService reconnectScheduler;
    private static final long RECONNECT_CHECK_INTERVAL_SECONDS = 5;

    private ROSManager() {
        // Private constructor
    }

    public static synchronized ROSManager getInstance() {
        if (instance == null) {
            instance = new ROSManager();
        }
        return instance;
    }

    /**
     * Register a listener for connection state changes.
     */
    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyConnected() {
        for (ConnectionListener l : listeners) {
            try {
                l.onROSConnected();
            } catch (Exception e) {
                ROS2Craft.LOGGER.warn("Listener threw exception onConnected", e);
            }
        }
    }

    private void notifyDisconnected() {
        for (ConnectionListener l : listeners) {
            try {
                l.onROSDisconnected();
            } catch (Exception e) {
                ROS2Craft.LOGGER.warn("Listener threw exception onDisconnected", e);
            }
        }
    }

    private void notifyConnectionLost() {
        for (ConnectionListener l : listeners) {
            try {
                l.onROSLostConnection();
            } catch (Exception e) {
                ROS2Craft.LOGGER.warn("Listener threw exception onConnectionLost", e);
            }
        }
    }

    private void notifyConnectionError(String message) {
        for (ConnectionListener l : listeners) {
            try {
                l.onROSConnectionError(message);
            } catch (Exception e) {
                ROS2Craft.LOGGER.warn("Listener threw exception onConnectionError", e);
            }
        }
    }

    /**
     * Get or create the ROS connection.
     * Automatically attempts reconnection if needed.
     * Call this when a robot entity is created/activated.
     */
    public synchronized Ros getRosConnection() {
        if (ros == null) {
            ros = new Ros("localhost");
        }
        if (!ros.isConnected()) {
            attemptConnection();
        }
        activeRobots++;
        ROS2Craft.LOGGER.info("Active robots: {}", activeRobots);
        // Start monitoring/reconnection when the first robot appears
        if (activeRobots == 1) {
            startReconnectScheduler();
        }
        return ros;
    }

    /**
     * Attempt to connect.
     */
    private synchronized void attemptConnection() {
        if (ros == null)
            return;
        if (ros.connect()) {
            ROS2Craft.LOGGER.info("ROS connection established");
            notifyConnected();
        } else {
            ROS2Craft.LOGGER.error("Failed to connect to ROS");
            notifyConnectionError("Failed to establish ROS connection");
        }
    }

    /**
     * Notify that a robot is no longer using the connection.
     */
    public synchronized void releaseRobot() {
        activeRobots--;
        if (activeRobots < 0)
            activeRobots = 0;
        ROS2Craft.LOGGER.info("Active robots: {}", activeRobots);

        if (activeRobots <= 0) {
            notifyDisconnected();
            if (ros != null && ros.isConnected()) {
                disconnect();
            }
            stopReconnectScheduler();
            activeRobots = 0;
        }
    }

    /**
     * Force disconnect from ROS.
     */
    public synchronized void disconnect() {
        if (ros != null && ros.isConnected()) {
            ros.disconnect();
            ROS2Craft.LOGGER.info("ROS connection closed");
        }
    }

    public boolean isConnected() {
        return ros != null && ros.isConnected();
    }

    public int getActiveRobotCount() {
        return activeRobots;
    }

    public void reset() {
        ROS2Craft.LOGGER.info("logged out, resetting ROSManager");
        stopReconnectScheduler();
        activeRobots = 0;
        notifyDisconnected();
        disconnect();
        ros = null;
        listeners.clear();
    }

    // ====================== RECONNECTION LOGIC ======================

    private void startReconnectScheduler() {
        if (reconnectScheduler == null || reconnectScheduler.isShutdown()) {
            reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ROS-Reconnect-Thread");
                t.setDaemon(true);
                return t;
            });
        }

        reconnectScheduler.scheduleAtFixedRate(this::checkAndReconnectIfNeeded,
                RECONNECT_CHECK_INTERVAL_SECONDS,
                RECONNECT_CHECK_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    private void stopReconnectScheduler() {
        if (reconnectScheduler != null && !reconnectScheduler.isShutdown()) {
            reconnectScheduler.shutdownNow();
            reconnectScheduler = null;
        }
    }

    /**
     * Periodic check (runs on background daemon thread).
     * If we still have active robots but lost the connection -> reconnect.
     */
    private void checkAndReconnectIfNeeded() {
        // Safe to call from background thread because getRosConnection /
        // attemptConnection are synchronized
        if (activeRobots > 0 && ros != null && !ros.isConnected()) {
            ROS2Craft.LOGGER.warn("ROS connection lost (detected by background check) - attempting reconnect...");
            notifyConnectionLost();
            attemptConnection();
        }
    }
}