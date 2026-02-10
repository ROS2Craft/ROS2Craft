package net.gitsrealpe.roscraft.ros;

import edu.wpi.rail.jrosbridge.Ros;
import net.gitsrealpe.roscraft.ROScraft;

/**
 * Singleton manager for the ROS connection.
 * Ensures only one WebSocket connection to ROS is maintained,
 * shared across all robot entities.
 */
public class ROSManager {
    private static ROSManager instance;
    private Ros ros;
    private boolean connected = false;
    private int activeRobots = 0;

    private ROSManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the singleton instance
     */
    public static synchronized ROSManager getInstance() {
        if (instance == null) {
            instance = new ROSManager();
        }
        return instance;
    }

    /**
     * Get or create the ROS connection.
     * Call this when a robot entity is created.
     */
    public synchronized Ros getRosConnection() {
        if (ros == null) {
            ros = new Ros("localhost");
        }

        if (!connected) {
            try {
                ros.connect();
                connected = true;
                ROScraft.LOGGER.info("ROS connection established");
            } catch (Exception e) {
                ROScraft.LOGGER.error("Failed to connect to ROS: ", e);
            }
        }

        activeRobots++;
        ROScraft.LOGGER.info("Active robots: {}", activeRobots);
        return ros;
    }

    /**
     * Notify that a robot is no longer using the connection.
     * Call this when a robot entity is removed/dies.
     */
    public synchronized void releaseRobot() {
        activeRobots--;
        ROScraft.LOGGER.info("Active robots: {}", activeRobots);

        // Disconnect when no robots are active
        if (activeRobots <= 0 && connected) {
            activeRobots = 0;
            disconnect();
        }
    }

    /**
     * Force disconnect from ROS
     */
    public synchronized void disconnect() {
        if (ros != null && connected) {
            ros.disconnect();
            connected = false;
            ROScraft.LOGGER.info("ROS connection closed");
        }
    }

    /**
     * Check if currently connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Get the number of active robots
     */
    public int getActiveRobotCount() {
        return activeRobots;
    }
}