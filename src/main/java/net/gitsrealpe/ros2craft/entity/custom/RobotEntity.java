package net.gitsrealpe.ros2craft.entity.custom;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonObject;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Service;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;
import edu.wpi.rail.jrosbridge.services.ServiceRequest;
import edu.wpi.rail.jrosbridge.services.ServiceResponse;
import net.gitsrealpe.ros2craft.ROS2Craft;
import net.gitsrealpe.ros2craft.ros.ROSManager;
import net.gitsrealpe.ros2craft.screen.custom.RobotMenu;
import net.gitsrealpe.ros2craft.sensors.Lidar;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class RobotEntity extends Mob implements MenuProvider, ROSManager.ConnectionListener {
    // private static final EntityDataAccessor<String> ROBOT_NAME =
    // SynchedEntityData
    // .defineId(RobotEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<BlockPos> FIXED_FRAME = SynchedEntityData
            .defineId(RobotEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData
            .defineId(RobotEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData
            .defineId(RobotEntity.class, EntityDataSerializers.STRING);
    public final ItemStackHandler inventory = new ItemStackHandler(3);
    public String robotName;
    public Topic twistSubscriber;
    public Topic rawPublisher;
    // Track if this robot instance registered with ROS manager
    private boolean rosConnected = false;
    private int node_id = 0;
    Ros ros;
    public BlockPos blockPos;
    private boolean isOwner = false;

    Lidar lidar;

    protected RobotEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        // constructor -> initializa data with default values, see mob
        // superclass
        // this.robotName = "robot";
        String cname = this.hasCustomName() ? this.getCustomName().getString() : "no cname";
        ROS2Craft.LOGGER.info("custom name at constructor " + cname);
        if (!level.isClientSide()) {
            if (!this.hasCustomName()) {
                ROS2Craft.LOGGER.info("setting cname by id ");
                this.setCustomName(Component.literal("robot" + getId()));
            }
            // ROS2Craft.LOGGER.info("name at constructor " + this.robotName);
        }
        if (level.isClientSide()) {
            ROS2Craft.LOGGER.info("name at constructor " + this.robotName);

            lidar = new Lidar(this, 180, 3.1416f, 10f);

        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // initialize with default value
        // builder.define(ROBOT_NAME, "robot");
        builder.define(FIXED_FRAME, new BlockPos(0, 0, 0));
        builder.define(OWNER_UUID, Optional.of(Util.NIL_UUID));
        builder.define(OWNER_NAME, "noOwner");
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (this.level().isClientSide()) {
            // if (ROBOT_NAME.equals(key)) {
            // ROS2Craft.LOGGER.info("my accesor ");
            // ROS2Craft.LOGGER.info("old name " + this.robotName);
            // if (this.robotName != null) {
            // // removeFromROS(false);
            // }
            // // this.setCustomName(Component.literal(this.robotName));
            // // ROS2Craft.LOGGER.info(this.robotName);
            // // // removeFromROS(false);
            // ROS2Craft.LOGGER.info("synced name data " + this.entityData.get(ROBOT_NAME));
            // this.robotName = this.entityData.get(ROBOT_NAME);
            // // addToROS(this.robotName);
            // ROS2Craft.LOGGER.info("client id " + this.getId());
            // }
            if (FIXED_FRAME.equals(key)) {
                ROS2Craft.LOGGER.info(this.entityData.get(FIXED_FRAME).toString());
            }
            if (OWNER_UUID.equals(key) && ros == null) {
                if (isOwner(Minecraft.getInstance().player)) {
                    ROS2Craft.LOGGER.info("yes you are owner " + Minecraft.getInstance().player.toString());
                    ROS2Craft.LOGGER.info("of robot: " + this.getCustomName());
                    isOwner = true;

                    ros = ROSManager.getInstance().getRosConnection();
                    ROSManager.getInstance().addConnectionListener(this);

                    if (ros.isConnected()) {
                        addToROS(this.getCustomName().getString());
                        rosConnected = true;
                    }
                } else {
                    ROS2Craft.LOGGER.info("not you not owner " + Minecraft.getInstance().player.toString());
                }
            }
        }
    }

    protected abstract void twistCallback(Message message);

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        // ROS2Craft.LOGGER.info("name at addded " + this.robotName);
        String cname = this.hasCustomName() ? this.getCustomName().getString() : "no cname";
        ROS2Craft.LOGGER.info("custom name at added " + cname);

        if (!this.hasCustomName()) {
            ROS2Craft.LOGGER.info("no custom name set " + this.getName().getString());
        }
        // assign name for first time if default was set
        // if (!level().isClientSide() && this.robotName.equals("robot")) {
        // ROS2Craft.LOGGER.info("name at addded1 " + this.robotName);
        // this.robotName = "robot" + getId();
        // ROS2Craft.LOGGER.info("name at addded2 " + this.robotName);
        // entityData.set(ROBOT_NAME, this.robotName);
        // ROS2Craft.LOGGER.info("name at addded3 " + this.robotName);
        // }
        if (level().isClientSide()) {

        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        // ROS2Craft.LOGGER.info("name at custom " + this.robotName);
        String cname = this.hasCustomName() ? this.getCustomName().getString() : "no cname";
        ROS2Craft.LOGGER.info("custom name at custom " + cname);
        if (!this.level().isClientSide()) {
            ROS2Craft.LOGGER.info("CustomName set: '{}'", name.getString());
            // this.robotName = name.getString();
            // this.entityData.set(ROBOT_NAME, name.getString());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide() && isOwner) {
            // ROS2Craft.LOGGER.info("yes mi owner");
            if (rosConnected) {
                publishRawData();
                this.lidar.captureDepth(this);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.level().isClientSide()) {
            ROS2Craft.LOGGER.info("robot removed");
            if (rosConnected) {
                removeFromROS();
            }
            ROSManager.getInstance().releaseRobot();
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level().isClientSide()) {
            ROS2Craft.LOGGER.info("robot die");
            if (rosConnected)
                removeFromROS();
            ROSManager.getInstance().releaseRobot();
        }
    }

    /**
     * Clean up ROS resources when entity is removed
     */
    protected void removeFromROS() {
        if (rosConnected) {
            Service opService = new Service(ros,
                    "/ComponentManager/_container/unload_node",
                    "composition_interfaces/srv/LoadNode");
            JsonObject req = Json.createObjectBuilder()
                    .add("unique_id", this.node_id)
                    .build();
            ServiceRequest request = new ServiceRequest(req);
            ServiceResponse response = opService.callServiceAndWait(request);
            System.out.println(response.toString());
        }
        if (this.twistSubscriber != null)
            this.twistSubscriber.unsubscribe();
        if (this.rawPublisher != null)
            this.rawPublisher.unadvertise();

        this.lidar.clearPublisher();
        ROSManager.getInstance().removeConnectionListener(this);
    }

    private void addToROS(String id) {
        Service opService = new Service(ros, "/ComponentManager/_container/load_node",
                "composition_interfaces/srv/LoadNode");
        JsonObject req = Json.createObjectBuilder()
                .add("package_name", "ros2craft_manager")
                .add("plugin_name", "ros2craft::MCRobot")
                .add("node_name", id)
                .build();
        ServiceRequest request = new ServiceRequest(req);
        ServiceResponse response = opService.callServiceAndWait(request);
        this.node_id = response.toJsonObject().getInt("unique_id");
        System.out.println(response.toString());
        // publishers
        this.rawPublisher = new Topic(ros, "/" + id + "/rawData",
                "ros2craft_interfaces/MCRawRobotData");
        // subscribers
        this.twistSubscriber = new Topic(ros, "/" + id + "/cmd_vel",
                "geometry_msgs/Twist");
        this.twistSubscriber.subscribe(this::twistCallback);
        // sensors
        this.lidar.setPublisher(new Topic(ros, "/" + id + "/lidar", "sensor_msgs/LaserScan"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("RobotName", this.getCustomName().getString());
        BlockPos fFrame = this.entityData.get(FIXED_FRAME);
        CompoundTag fFrameTag = new CompoundTag();
        fFrameTag.putInt("X", fFrame.getX());
        fFrameTag.putInt("Y", fFrame.getY());
        fFrameTag.putInt("Z", fFrame.getZ());
        tag.put("FixedFrame", fFrameTag);

        UUID ownerUuid = this.entityData.get(OWNER_UUID).orElse(null);
        tag.putUUID("Owner", ownerUuid);
        tag.putString("OwnerName", this.entityData.get(OWNER_NAME));
        tag.put("Inventory", inventory.serializeNBT(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RobotName")) {
            // this.robotName = tag.getString("RobotName");
            ROS2Craft.LOGGER.info("read tag " + tag.getString("RobotName"));
            // this.entityData.set(ROBOT_NAME, tag.getString("RobotName"));
        }
        if (tag.contains("FixedFrame")) {
            CompoundTag posTag = tag.getCompound("FixedFrame");
            this.entityData.set(FIXED_FRAME, new BlockPos(
                    posTag.getInt("X"),
                    posTag.getInt("Y"),
                    posTag.getInt("Z")));
        }
        if (tag.contains("Owner", CompoundTag.TAG_INT_ARRAY)) {
            this.entityData.set(OWNER_UUID, Optional.of(tag.getUUID("Owner")));
        }
        if (tag.contains("OwnerName")) {
            this.entityData.set(OWNER_NAME, tag.getString("OwnerName"));
        }
        if (tag.contains("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            inventory.deserializeNBT(this.registryAccess(), tag.getCompound("Inventory"));
        }
    }

    /**
     * Set the fixed frame MC coordinates for this robot
     **/
    public void setFixedFrame(BlockPos blockPos) {
        this.entityData.set(FIXED_FRAME, blockPos);
    }

    // Set ownership of this robot when spawning
    public void setOwnerUUID(UUID ownerUuid) {
        ROS2Craft.LOGGER.info("setting owner uuid");
        this.entityData.set(OWNER_UUID, Optional.of(ownerUuid));
        this.entityData.set(OWNER_NAME, this.level().getPlayerByUUID(ownerUuid).getName().getString());
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER_NAME);
    }

    private boolean isOwner(LocalPlayer player) {
        UUID owner = getOwnerUUID();
        ROS2Craft.LOGGER.info("The owner " + this.level().getPlayerByUUID(owner));
        return owner != null && owner.equals(player.getUUID());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this, buf -> {
                buf.writeVarInt(this.getId());
            });
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new RobotMenu(containerId, playerInv, this.getId());

    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        return;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public void publishRawData() {
        Vec3 fixedFrame = new Vec3(
                this.entityData.get(FIXED_FRAME).getX(),
                this.entityData.get(FIXED_FRAME).getY(),
                this.entityData.get(FIXED_FRAME).getZ());
        Vec3 relativePos = this.getPosition(0.0f).subtract(fixedFrame);
        JsonObject rawData = Json.createObjectBuilder()
                .add("frame_id", "world_origin")
                .add("position", Json.createObjectBuilder()
                        .add("x", relativePos.x())
                        .add("y", relativePos.y())
                        .add("z", relativePos.z()))
                .add("orientation", Json.createObjectBuilder()
                        .add("x", this.getRotationVector().x)
                        .add("y", this.getYRot())
                        .add("z", 0.0f))
                .add("velocity", Json.createObjectBuilder()
                        .add("x", this.getDeltaMovement().x)
                        .add("y", this.getDeltaMovement().y)
                        .add("z", this.getDeltaMovement().z))
                .build();
        Message toSend = new Message(rawData, "ros2craft_interfaces/msg/MCRawRobotData");
        rawPublisher.publish(toSend);
    }

    @Override
    public void onROSConnected() {
        ROS2Craft.LOGGER.info("=== ROS IS NOW CONNECTED ===");
        addToROS(this.getCustomName().getString());
        rosConnected = true;
    }

    @Override
    public void onROSDisconnected() {
        ROS2Craft.LOGGER.warn("=== ROS DISCONNECTED ===");
        removeFromROS();
        rosConnected = false;
    }

    @Override
    public void onROSLostConnection() {
        ROS2Craft.LOGGER.warn("=== LOST ROS CONNECTION ===");
        rosConnected = false;
    }

    @Override
    public void onROSConnectionError(String message) {
        ROS2Craft.LOGGER.error("ROS connection error: {}", message);
    }
}
