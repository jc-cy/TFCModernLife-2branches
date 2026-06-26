package com.jccy.tfcmodernlife.common.climate;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record GreenhouseStructureData(
    String displayNameKey,
    int effectiveSpace,
    int firmalifeTier,
    GreenhouseTier tier,
    int totalShellBlocks,
    int thermalShellBlocks,
    boolean mixedThermalWalls
)
{
    private static final String DISPLAY_NAME_KEY = "DisplayNameKey";
    private static final String EFFECTIVE_SPACE_KEY = "EffectiveSpace";
    private static final String FIRMALIFE_TIER_KEY = "FirmalifeTier";
    private static final String TIER_KEY = "Tier";
    private static final String TOTAL_SHELL_BLOCKS_KEY = "TotalShellBlocks";
    private static final String THERMAL_SHELL_BLOCKS_KEY = "ThermalShellBlocks";
    private static final String MIXED_THERMAL_WALLS_KEY = "MixedThermalWalls";

    public int thermalCoveragePercent()
    {
        return totalShellBlocks <= 0 ? 0 : Math.round(thermalShellBlocks * 100f / totalShellBlocks);
    }

    public CompoundTag toTag()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putString(DISPLAY_NAME_KEY, displayNameKey);
        tag.putInt(EFFECTIVE_SPACE_KEY, effectiveSpace);
        tag.putInt(FIRMALIFE_TIER_KEY, firmalifeTier);
        tag.putString(TIER_KEY, tier.id());
        tag.putInt(TOTAL_SHELL_BLOCKS_KEY, totalShellBlocks);
        tag.putInt(THERMAL_SHELL_BLOCKS_KEY, thermalShellBlocks);
        tag.putBoolean(MIXED_THERMAL_WALLS_KEY, mixedThermalWalls);
        return tag;
    }

    @Nullable
    public static GreenhouseStructureData fromTag(@Nullable CompoundTag tag)
    {
        if (tag == null || !tag.contains(DISPLAY_NAME_KEY))
        {
            return null;
        }
        return new GreenhouseStructureData(
            tag.getString(DISPLAY_NAME_KEY),
            tag.getInt(EFFECTIVE_SPACE_KEY),
            tag.getInt(FIRMALIFE_TIER_KEY),
            GreenhouseTier.byId(tag.getString(TIER_KEY)),
            tag.getInt(TOTAL_SHELL_BLOCKS_KEY),
            tag.getInt(THERMAL_SHELL_BLOCKS_KEY),
            tag.getBoolean(MIXED_THERMAL_WALLS_KEY)
        );
    }
}
