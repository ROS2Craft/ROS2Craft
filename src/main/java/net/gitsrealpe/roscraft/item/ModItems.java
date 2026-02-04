package net.gitsrealpe.roscraft.item;

import net.gitsrealpe.roscraft.ROScraft;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ROScraft.MODID);

    public static final DeferredItem<Item> BISMUTH = ITEMS.register("bismuth",
            () -> new Item(new Item.Properties()));

    // can also use java.util.function.Supplier?, allows to code be neoforge proof?
    // useful for
    // intermodloaders maybe?
    // public static final Supplier<TurtlebotItem> TURTLEBOT =
    // ITEMS.register("turtlebot",
    // () -> new TurtlebotItem(new Item.Properties()));

    public static final DeferredItem<TurtlebotItem> TURTLEBOT = ITEMS.register("turtlebot",
            () -> new TurtlebotItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
