package net.gitsrealpe.roscraft.entity.custom;

import javax.json.JsonObject;

import org.joml.Vector3f;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;
import net.gitsrealpe.roscraft.network.TwistPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class TurtlebotEntity extends LivingEntity {

    Minecraft mc = Minecraft.getInstance();
    // local vars
    Ros ros;
    Topic echoBack;
    int ticker = 0;
    // server vars
    // private UUID uuid; server already givess uuid to each entity, no need to
    // create another one
    float x_vel = 0;
    float w_vel = 0;

    public TurtlebotEntity(EntityType<? extends TurtlebotEntity> entityType, Level level) {
        super(entityType, level);

        if (!level.isClientSide()) {
            // this.entityData.set(NAME, generateDefaultDisplayId());
        }
        if (level.isClientSide()) {
            this.ros = new Ros("localhost");
            this.ros.connect();
            this.echoBack = new Topic(ros, "/turtlebot" + 5 + "/cmd_vel", "geometry_msgs/Twist");
            this.echoBack.subscribe(this::velCallback);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D) // 3 hearts
                .add(Attributes.ARMOR, 0.0D); // each point half chestplate
    }

    // callback assigned only in client, but variable send to server
    private void velCallback(Message message) {
        System.out.println("From ROS: " + message.toString());
        JsonObject data = message.toJsonObject();
        JsonObject linear = data.getJsonObject("linear");
        Vector3f linear_vel = new Vector3f((float) linear.getJsonNumber("x").doubleValue(), 0.0f, 0.0f);
        JsonObject angular = data.getJsonObject("angular");
        Vector3f angular_vel = new Vector3f(0.0f, 0.0f, (float) angular.getJsonNumber("z").doubleValue());
        PacketDistributor.sendToServer(new TwistPacket(this.getId(), linear_vel, angular_vel));
    }

    // this method is only executed in server, by payload C->S received
    public void setTwist(TwistPacket packet) {
        this.x_vel = packet.twist_linear().x;
        this.w_vel = packet.twist_angular().z;
    }

    @Override
    public void tick() {
        super.tick();
        // String side = (this.level().isClientSide()) ? "client" : "server";
        // System.out.println(side + this.getId());
        if (!this.level().isClientSide()) {
            float new_rot = this.getYHeadRot() + ((float) Math.toDegrees(this.w_vel)) / 20.0f;
            new_rot = Mth.wrapDegrees(new_rot);
            this.setYHeadRot(new_rot);
            // this.setYBodyRot(new_rot);
            // this.yBodyRotO = this.yRotO = rotation;
            // this.yHeadRotO = this.yHeadRot = rotation;
            float yaw = (float) Math.toRadians(this.getYHeadRot());
            System.out.println("yaw:  " + yaw);
            System.out.println("vel x:  " + this.x_vel * -Math.sin(yaw));
            System.out.println("vel z:  " + this.x_vel * Math.cos(yaw));
            this.setDeltaMovement(new Vec3(
                    this.x_vel * -Math.sin(yaw),
                    this.getDeltaMovement().y,
                    this.x_vel * Math.cos(yaw)));
            this.hasImpulse = true;
        }
    }

    // override vanilla head and body turn
    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        this.yBodyRotO = this.yBodyRot = this.getYHeadRot();
        return animStep;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level().isClientSide()) {
            this.echoBack.unsubscribe();
            this.ros.disconnect();
        }
        this.x_vel = 0.0f;
        this.w_vel = 0.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
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
