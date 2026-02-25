package net.gitsrealpe.ros2craft;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.gitsrealpe.ros2craft.block.ModBlocks;
import net.gitsrealpe.ros2craft.block.entity.ModBlockEntities;
import net.gitsrealpe.ros2craft.block.entity.renderer.PedestalBlockEntityRenderer;
import net.gitsrealpe.ros2craft.block.entity.renderer.ROS2LinkBlockEntityRenderer;
import net.gitsrealpe.ros2craft.component.ModDataComponents;
import net.gitsrealpe.ros2craft.entity.ModEntities;
import net.gitsrealpe.ros2craft.entity.client.TurtlebotRenderer;
import net.gitsrealpe.ros2craft.item.ModCreativeModeTabs;
import net.gitsrealpe.ros2craft.item.ModItems;
import net.gitsrealpe.ros2craft.ros.ROSManager;
import net.gitsrealpe.ros2craft.screen.ModMenuTypes;
import net.gitsrealpe.ros2craft.screen.custom.PedestalScreen;
import net.gitsrealpe.ros2craft.screen.custom.ROS2LinkScreen;
import net.gitsrealpe.ros2craft.screen.custom.RobotScreen;
import net.gitsrealpe.ros2craft.sound.ModSounds;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ROS2Craft.MODID)
public class ROS2Craft {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ros2craft";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public ROS2Craft(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod)
        // to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in
        // this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModSounds.register(modEventBus);

        // Register the item to a creative tab
        // modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    // private void addCreative(BuildCreativeModeTabContentsEvent event) {
    // if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
    // event.accept(ModItems.BISMUTH);
    // }

    // if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
    // event.accept(ModBlocks.BISMUTH_BLOCK);
    // event.accept(ModBlocks.MAGIC_BLOCK);
    // }
    // }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods
    // in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.TURTLEBOT.get(), TurtlebotRenderer::new);
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL_BE.get(), PedestalBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.ROS2LINK_BE.get(), ROS2LinkBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.PEDESTAL_MENU.get(), PedestalScreen::new);
            event.register(ModMenuTypes.ROS2LINK_MENU.get(), ROS2LinkScreen::new);
            event.register(ModMenuTypes.ROBOT_MENU.get(), RobotScreen::new);
        }

        // Close conection when client closes world
        @SubscribeEvent
        public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            ROSManager.getInstance().reset();
        }
    }
}
