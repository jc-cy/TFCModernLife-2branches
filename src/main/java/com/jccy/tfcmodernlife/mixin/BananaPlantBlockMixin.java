package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Banana plants use their own climate check before ticking (and die when it fails); use the controlled value. */
@Mixin(value = net.dries007.tfc.common.blocks.plant.fruit.BananaPlantBlock.class, remap = false)
public abstract class BananaPlantBlockMixin
{
    @Redirect(
        method = "randomTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getAverageTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"
        ),
        require = 0
    )
    private float tfcml$useControlledBananaTemperature(Level level, BlockPos pos)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getAverageTemperature(level, pos));
    }
}
