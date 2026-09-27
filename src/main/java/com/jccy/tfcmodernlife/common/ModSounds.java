package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Shared registration facade for the kitchen sound events. */
public final class ModSounds
{
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, TFCModernLife.MOD_ID);
    public static final Supplier<SoundEvent> ELECTRIC_OVEN_OPEN = register("block.electric_oven.open");
    public static final Supplier<SoundEvent> ELECTRIC_OVEN_CLOSE = register("block.electric_oven.close");
    public static final Supplier<SoundEvent> ELECTRIC_SOUP_POT_OPEN = register("block.electric_soup_pot.open");
    public static final Supplier<SoundEvent> ELECTRIC_SOUP_POT_CLOSE = register("block.electric_soup_pot.close");

    private ModSounds() {}

    private static Supplier<SoundEvent> register(String name)
    {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TFCModernLife.MOD_ID, name)));
    }

    public static void register(IEventBus bus)
    {
        SOUND_EVENTS.register(bus);
    }
}
