package net.gitsrealpe.roscraft.events;

import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.entity.ModEntities;
import net.gitsrealpe.roscraft.entity.client.TurtlebotModel;
import net.gitsrealpe.roscraft.entity.custom.TurtlebotEntity;
import net.gitsrealpe.roscraft.network.ServerROSPayloadHandler;
import net.gitsrealpe.roscraft.network.TwistPacket;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// warning advices to remove bus, will no longer be needed
@EventBusSubscriber(modid = ROScraft.MODID)
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
