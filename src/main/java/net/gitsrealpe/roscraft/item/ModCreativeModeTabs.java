package net.gitsrealpe.roscraft.item;

import java.util.function.Supplier;

import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ROScraft.MODID);

    public static final Supplier<CreativeModeTab> ROSCRAFT_ITEMS_TAB = CREATIVE_MODE_TAB.register("roscraft_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BISMUTH.get()))
                    .title(Component.translatable("creativetab.roscraft.roscraft_items"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModItems.BISMUTH);
                        output.accept(ModBlocks.BISMUTH_BLOCK);
                        output.accept(ModBlocks.MAGIC_BLOCK);
                        output.accept(ModBlocks.PEDESTAL.get());
                        output.accept(ModBlocks.ROS2LINK_BLOCK.get());
                        output.accept(ModItems.TURTLEBOT.get());

                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
