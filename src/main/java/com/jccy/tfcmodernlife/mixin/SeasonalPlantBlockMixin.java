package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Seasonal plants (fruit tree leaves, banana plants, berry bushes, ...) inside a controlled greenhouse must
 * ignore the calendar season and use the controlled temperature, which is what the 1.20 build achieved through
 * its own mixins plus TFE. Both hooks are redirects of existing TFC calls, so nothing is re-implemented.
 */
@Mixin(value = SeasonalPlantBlock.class, remap = false)
public abstract class SeasonalPlantBlockMixin
{
    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/common/blocks/plant/fruit/SeasonalPlantBlock;getLifecycleForCurrentMonth(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/dries007/tfc/common/blocks/plant/fruit/Lifecycle;"
        ),
        require = 0
    )
    private Lifecycle tfcml$forceFruitingInsideGreenhouse(SeasonalPlantBlock self, Level level, BlockPos pos)
    {
        final Lifecycle original = ((SeasonalPlantBlockAccessor) self).tfcml$invokeGetLifecycleForCurrentMonth(level, pos);
        return GreenhouseTemperatureHelper.isControlledGreenhouse(level, pos) ? Lifecycle.FRUITING : original;
    }

    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getAverageTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"
        ),
        require = 0
    )
    private float tfcml$useControlledSeasonalTemperature(Level level, BlockPos pos)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getAverageTemperature(level, pos));
    }
}
