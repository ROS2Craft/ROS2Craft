package net.gitsrealpe.roscraft.events;

import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.entity.ModEntities;
import net.gitsrealpe.roscraft.entity.client.TurtlebotModel;
import net.gitsrealpe.roscraft.entity.custom.TurtlebotEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

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
}
