package com.jccy.tfcmodernlife.compat.lso;

import com.jccy.tfcmodernlife.TFCModernLife;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import sfiomn.legendarysurvivaloverhaul.api.temperature.ModifierBase;

public final class LegendarySurvivalOverhaulLoadedCompat
{
    private static final String LSO_MOD_ID = "legendarysurvivaloverhaul";
    private static final DeferredRegister<ModifierBase> TEMPERATURE_MODIFIERS = DeferredRegister.create(
        new ResourceLocation(LSO_MOD_ID, "temperature_modifiers"),
        TFCModernLife.MOD_ID
    );

    private LegendarySurvivalOverhaulLoadedCompat() {}

    public static void register(IEventBus modBus)
    {
        TEMPERATURE_MODIFIERS.register("climate_control", TFCMLTemperatureModifier::new);
        TEMPERATURE_MODIFIERS.register(modBus);
    }
}
