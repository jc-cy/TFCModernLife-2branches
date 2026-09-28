package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Firmalife grapes gate all growth on {@code ClimateRange#checkTemperature} alone (no hydration), so supplying the
 * greenhouse temperature is enough to grow them indoors. Same gap exists in 1.20.
 */
@Mixin(value = com.eerussianguy.firmalife.common.blockentities.GrapePlantBlockEntity.class, remap = false)
public abstract class GrapePlantBlockEntityMixin
{
    @Redirect(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getInstantTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"
        ),
        require = 0
    )
    private float tfcml$useControlledGrapeTemperature(Level level, BlockPos pos)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getInstantTemperature(level, pos));
    }
}
