package net.gitsrealpe.roscraft.entity.custom;

import javax.json.Json;
import javax.json.JsonObject;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;
import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.ros.ROSManager;
import net.gitsrealpe.roscraft.sensors.Lidar;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class Robot extends LivingEntity {
    private static final EntityDataAccessor<String> ROBOT_NAME = SynchedEntityData
            .defineId(Robot.class, EntityDataSerializers.STRING);

    public String robotName;
    public Topic twistSubscriber;
    public Topic rawPublisher;
    // Track if this robot instance registered with ROS manager
    private boolean rosRegistered = false;
    Ros ros;

    Lidar lidar;

    protected Robot(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        // constructor -> initializa data with default values, see livingentity
        // superclass
        this.robotName = "robot";
        if (level.isClientSide()) {
            ROScraft.LOGGER.info("name at constructor " + this.robotName);
            lidar = new Lidar(this, 3, 3.1416f, 12.8f);

        }
    }

    protected abstract void twistCallback(Message message);

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (!level().isClientSide() && this.robotName.equals("robot")) {
            this.robotName = "robot" + getId();
            entityData.set(ROBOT_NAME, this.robotName);
        }
        if (level().isClientSide()) {
            ros = ROSManager.getInstance().getRosConnection();
            rosRegistered = true;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // initialize with default value
        builder.define(ROBOT_NAME, "robotDefault");
    }

    // once data like name has synced, create/rename topics
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (this.level().isClientSide() && ROBOT_NAME.equals(key)) {
            ROScraft.LOGGER.info("syncd name data");
            this.robotName = this.entityData.get(ROBOT_NAME);
            this.setCustomName(Component.literal(this.robotName));
            ROScraft.LOGGER.info(this.robotName);
            cleanup(false);
            // publishers
            this.rawPublisher = new Topic(ros, "/" + this.robotName + "/rawData",
                    "roscraft_msgs/MCRawRobotData");
            // subscribers
            this.twistSubscriber = new Topic(ros, "/" + this.robotName + "/cmd_vel",
                    "geometry_msgs/Twist");
            this.twistSubscriber.subscribe(this::twistCallback);
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        if (!this.level().isClientSide()) {
            ROScraft.LOGGER.info("CustomName set: '{}'", name.getString());
            this.robotName = name.getString();
            this.entityData.set(ROBOT_NAME, name.getString());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            JsonObject rawData = Json.createObjectBuilder()
                    .add("frame_id", "world_origin")
                    .add("position", Json.createObjectBuilder()
                            .add("x", this.getPosition(0.0f).x())
                            .add("y", this.getPosition(0.0f).y())
                            .add("z", this.getPosition(0.0f).z()))
                    .add("orientation", Json.createObjectBuilder()
                            .add("x", this.getRotationVector().x)
                            .add("y", this.getYRot())
                            .add("z", 0.0f))
                    .add("velocity", Json.createObjectBuilder()
                            .add("x", this.getDeltaMovement().x)
                            .add("y", this.getDeltaMovement().y)
                            .add("z", this.getDeltaMovement().z))
                    .build();
            Message toSend = new Message(rawData, "geometry_msgs/Vector3Stamped");
            rawPublisher.publish(toSend);

            this.lidar.captureDepth(this);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        cleanup(true);
    }

    /**
     * Clean up ROS resources when entity is removed
     */
    protected void cleanup(boolean release) {
        if (this.level().isClientSide() && rosRegistered) {
            if (this.twistSubscriber != null) {
                this.twistSubscriber.unsubscribe();
            }
            if (this.rawPublisher != null) {
                this.rawPublisher.unadvertise();
            }
            if (release) {
                // Notify the manager that this robot is done
                ROSManager.getInstance().releaseRobot();
                rosRegistered = false;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("RobotName", this.robotName);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RobotName")) {
            this.robotName = tag.getString("RobotName");
            this.entityData.set(ROBOT_NAME, this.robotName);
            ROScraft.LOGGER.info("read tag " + this.robotName);
        }
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

}
