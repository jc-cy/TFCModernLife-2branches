package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Firmalife grapes gate growth on {@code ClimateRange#checkTemperature} alone (no hydration), so supplying the
 * controlled greenhouse temperature is enough to grow them indoors.
 */
@Mixin(value = com.eerussianguy.firmalife.common.blockentities.GrapePlantBlockEntity.class, remap = false)
public abstract class TfeGrapePlantBlockEntityMixin
{
    @Redirect(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"
        ),
        require = 0
    )
    private float tfcml$useControlledGrapeTemperature(Level level, BlockPos pos)
    {
        return GreenhouseTemperatureHelper.getControlledTemperature(level, pos, Climate.getTemperature(level, pos));
    }

    /**
     * 受控温室 / 冷库内忽略自然月份：把当月视为 JULY，让葡萄在温室内不受季节限制结果。
     * Firmalife 2.1.x 同样只在 JUNE 开花、JULY 结果，其余月份只能长藤不能结果；
     * 反季节是本模组的既定设定（与 1.21 及一代 `温室地窖重置` 的实现逐字对应）。
     */
    @Redirect(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/util/calendar/ICalendar;getCalendarMonthOfYear()Lnet/dries007/tfc/util/calendar/Month;"
        ),
        require = 0
    )
    private Month tfcml$ignoreGrapeSeasonInsideGreenhouse(ICalendar calendar)
    {
        final BlockEntity self = (BlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level != null && GreenhouseTemperatureHelper.isControlledGreenhouse(level, self.getBlockPos()))
        {
            return Month.JULY;
        }
        return calendar.getCalendarMonthOfYear();
    }
}
