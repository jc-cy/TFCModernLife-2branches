package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 1.20 evaluated every bush variant's climate in the shared {@code IBushBlock#randomTick}. 1.21 inlined that check
 * into each class, and {@link net.dries007.tfc.common.blocks.plant.fruit.SpreadingCaneBlock} overrides
 * {@code randomTick} with its own copy, so the {@link SpreadingBushBlockMixin} parent hook does not reach it.
 */
@Mixin(value = net.dries007.tfc.common.blocks.plant.fruit.SpreadingCaneBlock.class, remap = false)
public abstract class SpreadingCaneBlockMixin
{
    @Redirect(
        method = "randomTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getAverageTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"
        ),
        require = 0
    )
    private float tfcml$useControlledAverageTemperature(Level level, BlockPos pos)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getAverageTemperature(level, pos));
    }
}
