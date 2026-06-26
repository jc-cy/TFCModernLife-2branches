package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = net.dries007.tfc.common.blocks.crop.CropHelpers.class, remap = false)
public abstract class CropHelpersMixin
{
    @Redirect(
        method = "growthTickStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/calendar/ICalendar;J)F",
            ordinal = 0
        ),
        require = 0
    )
    private static float tfcml$useControlledTemperatureAtStart(Level level, BlockPos pos, ICalendar calendar, long tick)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getTemperature(level, pos, calendar, tick));
    }

    @Redirect(
        method = "growthTickStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/calendar/ICalendar;J)F",
            ordinal = 1
        ),
        require = 0
    )
    private static float tfcml$useControlledTemperatureAtEnd(Level level, BlockPos pos, ICalendar calendar, long tick)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getTemperature(level, pos, calendar, tick));
    }
}
