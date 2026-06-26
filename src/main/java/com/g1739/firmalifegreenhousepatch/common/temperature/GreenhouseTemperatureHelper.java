package com.g1739.firmalifegreenhousepatch.common.temperature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class GreenhouseTemperatureHelper
{
    private GreenhouseTemperatureHelper() {}

    public static boolean isControlledGreenhouse(Level level, BlockPos pos)
    {
        return com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper.isControlledGreenhouse(level, pos);
    }
}
