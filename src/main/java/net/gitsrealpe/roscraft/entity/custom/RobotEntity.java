package net.gitsrealpe.roscraft.entity.custom;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonObject;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Service;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;
import edu.wpi.rail.jrosbridge.services.ServiceRequest;
import edu.wpi.rail.jrosbridge.services.ServiceResponse;
import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.ros.ROSManager;
import net.gitsrealpe.roscraft.screen.custom.RobotMenu;
import net.gitsrealpe.roscraft.sensors.Lidar;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class RobotEntity extends Mob implements MenuProvider {
    private static final EntityDataAccessor<String> ROBOT_NAME = SynchedEntityData
            .defineId(RobotEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<BlockPos> FIXED_FRAME = SynchedEntityData
            .defineId(RobotEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData
            .defineId(RobotEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public final ItemStackHandler inventory = new ItemStackHandler(3);
    public String robotName;
    public Topic twistSubscriber;
    public Topic rawPublisher;
    // Track if this robot instance registered with ROS manager
    private boolean rosRegistered = false;
    Ros ros;
    public BlockPos blockPos;
    private UUID ownerUuid = Util.NIL_UUID;
    private boolean isOwner = false;

    Lidar lidar;

    protected RobotEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        // constructor -> initializa data with default values, see mob
        // superclass
        // this.robotName = "robot";
        String cname = this.hasCustomName() ? this.getCustomName().getString() : "no cname";
        ROScraft.LOGGER.info("custom name at constructor " + cname);
        if (!level.isClientSide()) {
            if (!this.hasCustomName()) {
                ROScraft.LOGGER.info("setting cname by id ");
                this.setCustomName(Component.literal("robot" + getId()));
            }
            // ROScraft.LOGGER.info("name at constructor " + this.robotName);
        }
        if (level.isClientSide()) {
            ROScraft.LOGGER.info("name at constructor " + this.robotName);
            lidar = new Lidar(this, 3, 3.1416f, 12.8f);

        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // initialize with default value
        builder.define(ROBOT_NAME, "robot");
        builder.define(FIXED_FRAME, new BlockPos(0, 0, 0));
        builder.define(OWNER_UUID, Optional.of(Util.NIL_UUID));
    }

    protected abstract void twistCallback(Message message);

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        // ROScraft.LOGGER.info("name at addded " + this.robotName);
        String cname = this.hasCustomName() ? this.getCustomName().getString() : "no cname";
        ROScraft.LOGGER.info("custom name at added " + cname);

        if (!this.hasCustomName()) {
            ROScraft.LOGGER.info("no custom name set " + this.getName().getString());
        }
        // assign name for first time if default was set
        // if (!level().isClientSide() && this.robotName.equals("robot")) {
        // ROScraft.LOGGER.info("name at addded1 " + this.robotName);
        // this.robotName = "robot" + getId();
        // ROScraft.LOGGER.info("name at addded2 " + this.robotName);
        // entityData.set(ROBOT_NAME, this.robotName);
        // ROScraft.LOGGER.info("name at addded3 " + this.robotName);
        // }
        if (level().isClientSide()) {
            ros = ROSManager.getInstance().getRosConnection();
            rosRegistered = true;
        }
    }

    // once data like name has synced, create/rename topics
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (this.level().isClientSide()) {
            if (ROBOT_NAME.equals(key)) {
                ROScraft.LOGGER.info("my accesor ");
                ROScraft.LOGGER.info("old name " + this.robotName);
                if (this.robotName != null) {
                    // removeFromROS(false);
                }
                // this.setCustomName(Component.literal(this.robotName));
                // ROScraft.LOGGER.info(this.robotName);
                // // removeFromROS(false);
                ROScraft.LOGGER.info("synced name data " + this.entityData.get(ROBOT_NAME));
                this.robotName = this.entityData.get(ROBOT_NAME);
                // addToROS(this.robotName);
                ROScraft.LOGGER.info("client id " + this.getId());
            }
            if (FIXED_FRAME.equals(key)) {
                ROScraft.LOGGER.info(this.entityData.get(FIXED_FRAME).toString());
            }
            if (OWNER_UUID.equals(key)) {
                // this.level().playe
            }
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        // ROScraft.LOGGER.info("name at custom " + this.robotName);
        String cname = this.hasCustomName() ? this.getCustomName().getString() : "no cname";
        ROScraft.LOGGER.info("custom name at custom " + cname);
        if (!this.level().isClientSide()) {
            ROScraft.LOGGER.info("CustomName set: '{}'", name.getString());
            // this.robotName = name.getString();
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
        removeFromROS(true);
    }

    /**
     * Clean up ROS resources when entity is removed
     */
    protected void removeFromROS(boolean release) {
        if (this.level().isClientSide() && rosRegistered) {
            Service opService = new Service(ros, "/roscraft/robot_factory",
                    "roscraft_msgs/srv/FactoryReq");
            JsonObject req = Json.createObjectBuilder()
                    .add("operation", 1)
                    .add("robot_name", this.robotName)
                    .build();
            ServiceRequest request = new ServiceRequest(req);
            ServiceResponse response = opService.callServiceAndWait(request);
            System.out.println(response.toString());
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

    private void addToROS(String id) {
        Service opService = new Service(ros, "/roscraft/robot_factory", "roscraft_msgs/srv/FactoryReq");
        JsonObject req = Json.createObjectBuilder()
                .add("operation", 0)
                .add("robot_name", this.robotName)
                .build();
        ServiceRequest request = new ServiceRequest(req);
        ServiceResponse response = opService.callServiceAndWait(request);
        System.out.println(response.toString());
        // publishers
        this.rawPublisher = new Topic(ros, "/" + this.robotName + "/rawData",
                "roscraft_msgs/MCRawRobotData");
        // subscribers
        this.twistSubscriber = new Topic(ros, "/" + this.robotName + "/cmd_vel",
                "geometry_msgs/Twist");
        this.twistSubscriber.subscribe(this::twistCallback);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("RobotName", this.getCustomName().getString());
        BlockPos fFrame = this.entityData.get(FIXED_FRAME);
        if (fFrame != null) {
            CompoundTag fFrameTag = new CompoundTag();
            fFrameTag.putInt("X", fFrame.getX());
            fFrameTag.putInt("Y", fFrame.getY());
            fFrameTag.putInt("Z", fFrame.getZ());
            tag.put("FixedFrame", fFrameTag);
        }
        if (!ownerUuid.equals(Util.NIL_UUID)) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.put("Inventory", inventory.serializeNBT(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RobotName")) {
            // this.robotName = tag.getString("RobotName");
            ROScraft.LOGGER.info("read tag " + tag.getString("RobotName"));
            this.entityData.set(ROBOT_NAME, tag.getString("RobotName"));
        }
        if (tag.contains("FixedFrame")) {
            CompoundTag posTag = tag.getCompound("FixedFrame");
            this.entityData.set(FIXED_FRAME, new BlockPos(
                    posTag.getInt("X"),
                    posTag.getInt("Y"),
                    posTag.getInt("Z")));
        }
        if (tag.contains("Owner", CompoundTag.TAG_INT_ARRAY)) {
            ownerUuid = tag.getUUID("Owner");
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

    // Set ownership of this robot
    public void setOwnerUUID(Optional<UUID> ownerUuid) {
        this.entityData.set(OWNER_UUID, ownerUuid);
        // this.ownerUuid = ownerUuid;
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

}
