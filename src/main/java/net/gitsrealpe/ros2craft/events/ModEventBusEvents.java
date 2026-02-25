package net.gitsrealpe.ros2craft.events;

import net.gitsrealpe.ros2craft.ROS2Craft;
import net.gitsrealpe.ros2craft.entity.ModEntities;
import net.gitsrealpe.ros2craft.entity.client.TurtlebotModel;
import net.gitsrealpe.ros2craft.entity.custom.TurtlebotEntity;
import net.gitsrealpe.ros2craft.network.ServerROSPayloadHandler;
import net.gitsrealpe.ros2craft.network.TwistPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// warning advices to remove bus, will no longer be needed
@EventBusSubscriber(modid = ROS2Craft.MODID)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TurtlebotModel.LAYER_LOCATION, TurtlebotModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TURTLEBOT.get(), TurtlebotEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(TwistPacket.TYPE, TwistPacket.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                ServerROSPayloadHandler.handleVelocity(payload, context);
            }).exceptionally(e -> {
                context.disconnect(Component.literal("Packet error: " + e.getMessage()));
                return null;
            });
        });

    }
}
