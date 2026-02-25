package net.gitsrealpe.ros2craft.sound;

import java.util.function.Supplier;

import net.gitsrealpe.ros2craft.ROS2Craft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
            .create(BuiltInRegistries.SOUND_EVENT, ROS2Craft.MODID);

    public static final Supplier<SoundEvent> TURTLEBOT_AMBIENT = registerSoundEvent("turtlebot_ambient");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ROS2Craft.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
