package net.gitsrealpe.roscraft.block.entity;

import java.util.function.Supplier;

import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ROScraft.MODID);

    public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL_BE = BLOCK_ENTITIES.register(
            "pedestal_be",
            () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.PEDESTAL.get()).build(null));

    public static final Supplier<BlockEntityType<ROS2LinkBlockEntity>> ROS2LINK_BE = BLOCK_ENTITIES.register(
            "ros2link_be",
            () -> BlockEntityType.Builder.of(ROS2LinkBlockEntity::new, ModBlocks.ROS2LINK_BLOCK.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
