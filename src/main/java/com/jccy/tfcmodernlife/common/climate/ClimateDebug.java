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
    public static final String PREFIX = "[TFCML-ClimateDebug]";

    private ClimateDebug() {}

    public static void info(String message, Object... args)
    {
        if (ENABLED)
        {
            LOGGER.info(PREFIX + " " + message, args);
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
