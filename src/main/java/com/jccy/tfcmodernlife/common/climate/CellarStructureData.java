package com.jccy.tfcmodernlife.common.climate;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record CellarStructureData(
    int effectiveSpace,
    CellarTier tier,
    int totalShellBlocks,
    int thermalShellBlocks,
    boolean mixedThermalWalls,
    float minimumTemperature
)
{
    private static final String EFFECTIVE_SPACE_KEY = "EffectiveSpace";
    private static final String TIER_KEY = "Tier";
    private static final String TOTAL_SHELL_BLOCKS_KEY = "TotalShellBlocks";
    private static final String THERMAL_SHELL_BLOCKS_KEY = "ThermalShellBlocks";
    private static final String MIXED_THERMAL_WALLS_KEY = "MixedThermalWalls";
    private static final String MINIMUM_TEMPERATURE_KEY = "MinimumTemperature";

    public CellarStructureData(int effectiveSpace, CellarTier tier, int totalShellBlocks, int thermalShellBlocks)
    {
        this(
            effectiveSpace,
            tier,
            totalShellBlocks,
            thermalShellBlocks,
            false,
            tier.minimumTemperature()
        );
    }

    public int thermalCoveragePercent()
    {
        return totalShellBlocks <= 0 ? 0 : Math.round(thermalShellBlocks * 100f / totalShellBlocks);
    }

    public float powerMultiplier()
    {
        return tier.powerMultiplier();
    }

    public String displayNameKey()
    {
        return "screen.tfc_modern_life.cellar." + (mixedThermalWalls ? "mixed" : tier.id());
    }

    public CompoundTag toTag()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putInt(EFFECTIVE_SPACE_KEY, effectiveSpace);
        tag.putString(TIER_KEY, tier.id());
        tag.putInt(TOTAL_SHELL_BLOCKS_KEY, totalShellBlocks);
        tag.putInt(THERMAL_SHELL_BLOCKS_KEY, thermalShellBlocks);
        tag.putBoolean(MIXED_THERMAL_WALLS_KEY, mixedThermalWalls);
        tag.putFloat(MINIMUM_TEMPERATURE_KEY, minimumTemperature);
        return tag;
    }

    @Nullable
    public static CellarStructureData fromTag(@Nullable CompoundTag tag)
    {
        if (tag == null || !tag.contains(TIER_KEY))
        {
            return null;
        }
        final CellarTier tier = CellarTier.byId(tag.getString(TIER_KEY));
        return new CellarStructureData(
            tag.getInt(EFFECTIVE_SPACE_KEY),
            tier,
            tag.getInt(TOTAL_SHELL_BLOCKS_KEY),
            tag.getInt(THERMAL_SHELL_BLOCKS_KEY),
            tag.getBoolean(MIXED_THERMAL_WALLS_KEY),
            tag.contains(MINIMUM_TEMPERATURE_KEY) ? tag.getFloat(MINIMUM_TEMPERATURE_KEY) : tier.minimumTemperature()
        );
    }
}
