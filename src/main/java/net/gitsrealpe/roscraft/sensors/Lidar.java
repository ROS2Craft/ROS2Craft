package net.gitsrealpe.roscraft.sensors;

import java.util.Arrays;

import net.gitsrealpe.roscraft.entity.custom.RobotEntity;
// import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class Lidar {

    private int rays;
    private float[] depthGrid;
    private RobotEntity robot;
    private float fov, angleStep;
    private float maxDistance;

    /**
     * Create Lidar sensor object
     * 
     * @param robot       robot entity
     * @param rays        number of lidar rays
     * @param fov         field of view
     * @param maxDistance maximum detection distance.
     */
    public Lidar(RobotEntity robot, int rays, float fov, float maxDistance) {
        this.fov = fov;
        this.rays = rays;
        this.robot = robot;
        this.maxDistance = maxDistance;
        this.depthGrid = new float[rays];
        this.angleStep = fov / (rays - 1);
    }

    public void captureDepth(RobotEntity entity) {
        Level level = entity.level();
        Vec3 eyePos = entity.getEyePosition(1.0f);
        float yaw = (float) Math.toRadians(entity.getYRot());
        for (int r = 0; r < this.rays; r++) {
            float angle = ((angleStep * r) - (this.fov / 2)) + yaw;
            // System.out.println("ray " + r + " angle " + Math.toDegrees(angle));
            Vec3 angleDir = new Vec3(Math.sin(angle), 0.0f, Math.cos(angle));
            Vec3 endPos = eyePos.add(angleDir.scale(maxDistance));
            BlockHitResult hit = level.clip(new ClipContext(eyePos, endPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    entity));
            float distance = hit.getType() == HitResult.Type.MISS ? maxDistance
                    : (float) eyePos.distanceTo(hit.getLocation());
            depthGrid[r] = distance;
        }
        // System.out.println(Arrays.toString(depthGrid));
        // TODO: sensors should have their own publisher/services
    }
}