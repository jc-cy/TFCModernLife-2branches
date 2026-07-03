package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import java.util.ArrayList;
import java.util.List;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class ModFoodTraits
{
    public static final FoodTrait ELECTRIC_OVEN_BAKED = FoodTrait.register(
        new ResourceLocation(TFCModernLife.MOD_ID, "electric_oven_baked"),
        new FoodTrait(() -> 0.9f, "tfc_modern_life.tooltip.food_trait.electric_oven_baked")
    );

    private static final List<CellarTraitDefinition> CELLAR_DEFINITIONS = List.of(
        new CellarTraitDefinition("cellar_10x", 10f),
        new CellarTraitDefinition("cellar_8_5x", 8.5f),
        new CellarTraitDefinition("cellar_7x", 7f),
        new CellarTraitDefinition("cellar_6x", 6f),
        new CellarTraitDefinition("cellar_5x", 5f),
        new CellarTraitDefinition("cellar_4_5x", 4.5f),
        new CellarTraitDefinition("cellar_4x", 4f),
        new CellarTraitDefinition("cellar_3_5x", 3.5f),
        new CellarTraitDefinition("cellar_3x", 3f),
        new CellarTraitDefinition("cellar_2_5x", 2.5f),
        new CellarTraitDefinition("cellar_2x", 2f)
    );
    private static final List<FoodTrait> CELLAR_TRAITS = registerCellarTraits();

    private ModFoodTraits() {}

    public static void init() {}

    public static List<FoodTrait> getCellarTraits()
    {
        return CELLAR_TRAITS;
    }

    public static FoodTrait getCellarTraitForMultiplier(float multiplier)
    {
        for (int index = 0; index < CELLAR_DEFINITIONS.size(); index++)
        {
            if (Math.abs(CELLAR_DEFINITIONS.get(index).multiplier() - multiplier) < 0.001f)
            {
                return CELLAR_TRAITS.get(index);
            }
        }
        return CELLAR_TRAITS.get(CELLAR_TRAITS.size() - 1);
    }

    public static @Nullable FoodTrait getCellarTraitAtMost(float multiplier)
    {
        for (int index = 0; index < CELLAR_DEFINITIONS.size(); index++)
        {
            if (CELLAR_DEFINITIONS.get(index).multiplier() <= multiplier + 0.001f)
            {
                return CELLAR_TRAITS.get(index);
            }
        }
        return null;
    }

    public static float getCellarTraitMultiplier(FoodTrait trait)
    {
        final int index = CELLAR_TRAITS.indexOf(trait);
        return index >= 0 ? CELLAR_DEFINITIONS.get(index).multiplier() : 1f;
    }

    public static boolean isCellarTrait(FoodTrait trait)
    {
        return CELLAR_TRAITS.contains(trait);
    }

    public static String getCellarTraitMultiplierText(FoodTrait trait)
    {
        final int index = CELLAR_TRAITS.indexOf(trait);
        return index >= 0 ? com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper.formatFactor(CELLAR_DEFINITIONS.get(index).multiplier()) : "1";
    }

    private static List<FoodTrait> registerCellarTraits()
    {
        final List<FoodTrait> traits = new ArrayList<>(CELLAR_DEFINITIONS.size());
        for (CellarTraitDefinition definition : CELLAR_DEFINITIONS)
        {
            traits.add(FoodTrait.register(
                new ResourceLocation(TFCModernLife.MOD_ID, definition.name()),
                new FoodTrait(() -> 1f / definition.multiplier(), "tfc_modern_life.tooltip.food_trait.cellar")
            ));
        }
        return List.copyOf(traits);
    }

    private record CellarTraitDefinition(String name, float multiplier) {}
}
