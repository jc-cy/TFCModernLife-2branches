package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import java.util.List;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.food.FoodTraits;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFoodTraits
{
    public static final DeferredRegister<FoodTrait> TRAITS = DeferredRegister.create(FoodTraits.KEY, TFCModernLife.MOD_ID);
    public static final DeferredHolder<FoodTrait, FoodTrait> ELECTRIC_OVEN_BAKED = TRAITS.register("electric_oven_baked",
        () -> new FoodTrait(() -> 0.9d, "tfc_modern_life.tooltip.food_trait.electric_oven_baked"));

    private static final List<CellarTraitDefinition> DEFINITIONS = List.of(
        new CellarTraitDefinition("cellar_10x", 10f), new CellarTraitDefinition("cellar_8_5x", 8.5f),
        new CellarTraitDefinition("cellar_7x", 7f), new CellarTraitDefinition("cellar_6x", 6f),
        new CellarTraitDefinition("cellar_5x", 5f), new CellarTraitDefinition("cellar_4_5x", 4.5f),
        new CellarTraitDefinition("cellar_4x", 4f), new CellarTraitDefinition("cellar_3_5x", 3.5f),
        new CellarTraitDefinition("cellar_3x", 3f), new CellarTraitDefinition("cellar_2_5x", 2.5f),
        new CellarTraitDefinition("cellar_2x", 2f));
    private static final List<DeferredHolder<FoodTrait, FoodTrait>> CELLAR_TRAITS = DEFINITIONS.stream()
        .map(definition -> TRAITS.register(definition.name(), () -> new FoodTrait(() -> 1d / definition.multiplier(), "tfc_modern_life.tooltip.food_trait.cellar")))
        .toList();

    private ModFoodTraits() {}

    public static void register(net.neoforged.bus.api.IEventBus bus) { TRAITS.register(bus); }
    public static List<DeferredHolder<FoodTrait, FoodTrait>> getCellarTraits() { return CELLAR_TRAITS; }

    public static float getCellarTraitMultiplier(FoodTrait trait)
    {
        for (int i = 0; i < CELLAR_TRAITS.size(); i++) if (CELLAR_TRAITS.get(i).isBound() && CELLAR_TRAITS.get(i).value() == trait) return DEFINITIONS.get(i).multiplier();
        return 1f;
    }

    public static DeferredHolder<FoodTrait, FoodTrait> getCellarTraitForMultiplier(float multiplier)
    {
        for (int i = 0; i < DEFINITIONS.size(); i++) if (Math.abs(DEFINITIONS.get(i).multiplier() - multiplier) < 0.001f) return CELLAR_TRAITS.get(i);
        return CELLAR_TRAITS.get(CELLAR_TRAITS.size() - 1);
    }

    public static DeferredHolder<FoodTrait, FoodTrait> getCellarTraitAtMost(float multiplier)
    {
        for (int i = 0; i < DEFINITIONS.size(); i++) if (DEFINITIONS.get(i).multiplier() <= multiplier + 0.001f) return CELLAR_TRAITS.get(i);
        return null;
    }

    public static boolean isCellarTrait(FoodTrait trait)
    {
        return CELLAR_TRAITS.stream().anyMatch(holder -> holder.isBound() && holder.value() == trait);
    }

    public static String getCellarTraitMultiplierText(FoodTrait trait)
    {
        return GreenhouseTemperatureHelper.formatFactor(getCellarTraitMultiplier(trait));
    }

    private record CellarTraitDefinition(String name, float multiplier) {}
}
