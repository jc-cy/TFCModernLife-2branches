package com.jccy.tfcmodernlife.common.climate;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public final class ClimateDebug
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = Boolean.getBoolean("tfcml.climateDebug");
    private static final boolean CELLAR_ENABLED = Boolean.getBoolean("tfcml.cellarDebug");
    public static final String PREFIX = "[TFCML-ClimateDebug]";
    public static final String CELLAR_PREFIX = "[TFCML-CellarDebug]";
    public static final String CELLAR_TEMP_PREFIX = "[TFCML-CellarTemp]";

    private ClimateDebug() {}

    public static void info(String message, Object... args)
    {
        if (ENABLED)
        {
            LOGGER.info(PREFIX + " " + message, args);
        }
    }

    public static boolean isCellarEnabled()
    {
        return CELLAR_ENABLED;
    }

    public static void cellarInfo(String message, Object... args)
    {
        if (CELLAR_ENABLED)
        {
            LOGGER.info(CELLAR_PREFIX + " " + message, args);
        }
    }

    public static void cellarTemp(String message, Object... args)
    {
        if (CELLAR_ENABLED)
        {
            LOGGER.info(CELLAR_TEMP_PREFIX + " " + message, args);
        }
    }

    public static String describe(BlockState state)
    {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString() + state.getValues();
    }

    public static String pos(BlockPos pos)
    {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
