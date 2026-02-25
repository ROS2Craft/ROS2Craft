package net.gitsrealpe.ros2craft.screen;

import net.gitsrealpe.ros2craft.ROS2Craft;
import net.gitsrealpe.ros2craft.screen.custom.PedestalMenu;
import net.gitsrealpe.ros2craft.screen.custom.ROS2LinkMenu;
import net.gitsrealpe.ros2craft.screen.custom.RobotMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
        public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU,
                        ROS2Craft.MODID);

        public static final DeferredHolder<MenuType<?>, MenuType<PedestalMenu>> PEDESTAL_MENU = registerMenuType(
                        "pedestal_menu", PedestalMenu::new);

        public static final DeferredHolder<MenuType<?>, MenuType<ROS2LinkMenu>> ROS2LINK_MENU = registerMenuType(
                        "ros2link_menu", ROS2LinkMenu::new);

        public static final DeferredHolder<MenuType<?>, MenuType<RobotMenu>> ROBOT_MENU = registerMenuType(
                        "robot_menu", RobotMenu::new);

        private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(
                        String name,
                        IContainerFactory<T> factory) {
                return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
        }

        public static void register(IEventBus eventBus) {
                MENUS.register(eventBus);
        }

}
