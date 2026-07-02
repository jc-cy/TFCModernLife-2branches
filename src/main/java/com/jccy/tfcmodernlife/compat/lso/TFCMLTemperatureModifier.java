package com.jccy.tfcmodernlife.compat.lso;

import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import sfiomn.legendarysurvivaloverhaul.api.temperature.ModifierBase;
import sfiomn.legendarysurvivaloverhaul.common.integration.terrafirmacraft.TerraFirmaCraftUtil;
import sfiomn.legendarysurvivaloverhaul.config.Config;

public final class TFCMLTemperatureModifier extends ModifierBase
{
    private static final Vec3i[] LSO_TFC_SAMPLE_OFFSETS = {
        new Vec3i(0, 0, 0),
        new Vec3i(10, 0, 0),
        new Vec3i(-10, 0, 0),
        new Vec3i(0, 0, 10),
        new Vec3i(0, 0, -10)
    };

    @Override
    public float getWorldInfluence(@Nullable Player player, Level level, BlockPos pos)
    {
        if (!TerraFirmaCraftUtil.shouldUseTerraFirmaCraftTemp())
        {
            return 0.0f;
        }

        try
        {
            final ControlledTemperature controlledTemperature = getControlledTemperature(level, pos);
            if (controlledTemperature == null)
            {
                return 0.0f;
            }
            final double multiplier = Config.Baked.tfcTemperatureMultiplier;
            return (float) ((controlledTemperature.temperature() - getLSOTerraFirmaCraftTemperature(level, pos)) * multiplier);
        }
        catch (RuntimeException ignored)
        {
            return 0.0f;
        }
    }

    @Nullable
    private static ControlledTemperature getControlledTemperature(Level level, BlockPos pos)
    {
        for (BlockPos candidate : getCandidatePositions(pos))
        {
            final ClimateStationAccess cellar = ClimateStationRegistry.findControllingCellarStation(level, candidate);
            if (cellar != null)
            {
                final float baseTemperature = Climate.getTemperature(level, pos);
                return new ControlledTemperature(cellar.tfcml$getEffectiveCellarTemperature(baseTemperature));
            }
        }
        for (BlockPos candidate : getCandidatePositions(pos))
        {
            final ClimateStationAccess greenhouse = ClimateStationRegistry.findControllingGreenhouseStation(level, candidate);
            if (greenhouse != null)
            {
                return new ControlledTemperature(greenhouse.tfcml$getEffectiveTemperature());
            }
        }
        return null;
    }

    private static BlockPos[] getCandidatePositions(BlockPos pos)
    {
        return new BlockPos[] { pos, pos.above(), pos.below() };
    }

    private static float getLSOTerraFirmaCraftTemperature(Level level, BlockPos pos)
    {
        float temperature = 0.0f;
        for (Vec3i offset : LSO_TFC_SAMPLE_OFFSETS)
        {
            temperature += Climate.getTemperature(level, pos.offset(offset));
        }
        return temperature / LSO_TFC_SAMPLE_OFFSETS.length;
    }

    private record ControlledTemperature(float temperature) {}
}
