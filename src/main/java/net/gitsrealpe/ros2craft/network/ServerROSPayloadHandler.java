package net.gitsrealpe.ros2craft.network;

import java.util.List;

import net.gitsrealpe.ros2craft.entity.custom.TurtlebotEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerROSPayloadHandler {
    public static void handleVelocity(TwistPacket payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        ServerLevel level = player.serverLevel();

        // find entity to apply received payload
        // Find entity – example: nearest Turtlebot within 32 blocks
        Vec3 min = new Vec3(32, 32, 32);
        AABB bbox = new AABB(player.position().subtract(min), player.position().add(min));
        List<TurtlebotEntity> list = player.level().getEntitiesOfClass(TurtlebotEntity.class, bbox);
        if (list.isEmpty()) {
            player.sendSystemMessage(Component.literal("No Turtlebot nearby!"));
            return;
        }

        Entity someEntity = level.getEntity(payload.robotId());
        if (someEntity instanceof TurtlebotEntity turtlebot && !turtlebot.isRemoved()) {
            turtlebot.setTwist(payload);
        }
    }
}
