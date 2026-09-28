package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Firmalife's own growing fruit tree branches (cocoa / fig) override {@code randomTick} with their own climate
 * check, so TFE's TFC-parent mixin does not reach them. Failing it resets the branch growth counter, stalling a
 * greenhouse tree.
 */
@Mixin(value = com.eerussianguy.firmalife.common.blocks.plant.FLGrowingFruitTreeBranchBlock.class, remap = false)
public abstract class TfeFLGrowingFruitTreeBranchBlockMixin
{
    @Redirect(
        method = {"randomTick", "m_213898_"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"
        ),
        require = 0
    )
    private float tfcml$useControlledAverageTemperature(Level level, BlockPos pos)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getTemperature(level, pos));
    }
}
