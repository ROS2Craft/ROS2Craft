package net.gitsrealpe.ros2craft.sensors;

import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;
import net.gitsrealpe.ros2craft.entity.custom.RobotEntity;
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
    private Topic publisher;

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

    public void setPublisher(Topic topic) {
        this.publisher = topic;
    }

    public void clearPublisher() {
        this.publisher.unadvertise();
    }

    // check if i can used robot reference from constructor
    public void captureDepth(RobotEntity entity) {
        JsonArrayBuilder rangesBuilder = Json.createArrayBuilder();
        Level level = entity.level();
        Vec3 eyePos = entity.getEyePosition(1.0f);
        float yaw = (float) Math.toRadians(entity.getYRot());
        for (int r = 0; r < this.rays; r++) {
            float angle = ((this.angleStep * r) - (this.fov / 2)) + yaw;
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
            rangesBuilder.add(distance);
        }

        JsonObject rangeData = Json.createObjectBuilder()
                .add("header", Json.createObjectBuilder()
                        .add("frame_id", entity.getCustomName().getString()))
                .add("angle_min", -(this.fov / 2))
                .add("angle_max", (this.fov / 2))
                .add("angle_increment", this.angleStep)
                .add("range_min", 0.0f)
                .add("range_max", this.maxDistance)
                .add("ranges", rangesBuilder)
                .build();
        Message toSend = new Message(rangeData, entity.getCustomName().getString() + "/lidar");
        this.publisher.publish(toSend);

    }
}