package net.gitsrealpe.roscraft.entity.custom;

import javax.json.JsonObject;

import org.joml.Vector3f;

import edu.wpi.rail.jrosbridge.messages.Message;
import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.network.TwistPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class TurtlebotEntity extends Robot {

    // local vars
    // server vars
    float x_vel = 0;
    float w_vel = 0;

    public TurtlebotEntity(EntityType<? extends TurtlebotEntity> entityType, Level level) {
        super(entityType, level);

        if (!level.isClientSide()) {
            // this.entityData.set(NAME, generateDefaultDisplayId());
        }
        if (level.isClientSide()) {
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D) // 3 hearts
                .add(Attributes.ARMOR, 0.0D); // each point half chestplate
    }

    // callback assigned only in client, but variable send to server
    protected void twistCallback(Message message) {
        ROScraft.LOGGER.info("From ROS: " + message.toString());
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

        if (!this.level().isClientSide() && this.isAlive()) {

            // 1. Apply angular velocity to body yaw (this is what should turn the robot)
            float yawIncrementDeg = this.w_vel * (180.0f / (float) Math.PI) / 20.0f; // rad/s → deg/tick

            float currentBodyYaw = this.getYRot(); // or this.yBodyRot if you prefer
            float newBodyYaw = currentBodyYaw + yawIncrementDeg;

            // Wrap to [-180, 180) or [0, 360) — both work
            newBodyYaw = Mth.wrapDegrees(newBodyYaw);
            // 2. Apply to both body and head (so head follows body)
            this.setYRot(newBodyYaw); // main entity yaw
            this.yBodyRot = newBodyYaw;
            this.yHeadRot = newBodyYaw; // head follows body exactly
            // Optional: smooth previous values so render doesn't jitter
            this.yRotO = this.yRotO + yawIncrementDeg;
            this.yBodyRotO = this.yBodyRotO + yawIncrementDeg;
            this.yHeadRotO = this.yHeadRotO + yawIncrementDeg;
            // 3. Now use the final (wrapped) yaw for movement direction
            float yawRad = (float) Math.toRadians(this.getYRot()); // or newBodyYaw

            this.setDeltaMovement(new Vec3(
                    this.x_vel * -Math.sin(yawRad),
                    this.getDeltaMovement().y,
                    this.x_vel * Math.cos(yawRad)));

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
        this.x_vel = 0.0f;
        this.w_vel = 0.0f;
        this.cleanup(true);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

}