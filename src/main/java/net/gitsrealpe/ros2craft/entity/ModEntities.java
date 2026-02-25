package net.gitsrealpe.ros2craft.entity;

import java.util.function.Supplier;

import net.gitsrealpe.ros2craft.ROS2Craft;
import net.gitsrealpe.ros2craft.entity.custom.TurtlebotEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(BuiltInRegistries.ENTITY_TYPE, ROS2Craft.MODID);

    public static final Supplier<EntityType<TurtlebotEntity>> TURTLEBOT = ENTITY_TYPES.register("turtlebot",
            () -> EntityType.Builder.of(TurtlebotEntity::new, MobCategory.MISC)
                    .sized(.75f, .375f)
                    .eyeHeight(.34375f)
                    .build("turtlebot"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}
