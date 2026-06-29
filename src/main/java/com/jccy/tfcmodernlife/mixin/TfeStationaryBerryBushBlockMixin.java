package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import com.jccy.tfcmodernlife.common.compat.TfeClimateCompat;
import net.dries007.tfc.common.blockentities.BerryBushBlockEntity;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.common.blocks.plant.fruit.StationaryBerryBushBlock;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StationaryBerryBushBlock.class, remap = false, priority = 900)
public abstract class TfeStationaryBerryBushBlockMixin
{
    @Shadow protected abstract BlockState growAndPropagate(Level level, BlockPos pos, net.minecraft.util.RandomSource random, BlockState state);

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true, require = 0)
    private void tfcml$runControlledGreenhouseUpdate(Level level, BlockPos pos, BlockState state, CallbackInfo ci)
    {
        if (!GreenhouseTemperatureHelper.isControlledGreenhouse(level, pos))
        {
            return;
        }

        if (level.getBlockEntity(pos) instanceof BerryBushBlockEntity bush)
        {
            final TfeSeasonalPlantBlockAccessor accessor = (TfeSeasonalPlantBlockAccessor) this;
            Lifecycle currentLifecycle = state.getValue(StationaryBerryBushBlock.LIFECYCLE);
            final Lifecycle expectedLifecycle = Lifecycle.FRUITING;
            if (!SeasonalPlantBlock.checkAndSetDormant(level, pos, state, currentLifecycle, expectedLifecycle))
            {
                long deltaTicks = Math.min(bush.getTicksSinceBushUpdate(), Calendars.SERVER.getCalendarTicksInYear());
                final long currentCalendarTick = Calendars.SERVER.getCalendarTicks();
                long nextCalendarTick = currentCalendarTick - deltaTicks;

                final var range = accessor.tfcml$getClimateRange().get();
                final int hydration = TfeClimateCompat.getFruitBushHydrationFromRootPos(level, pos.below());

                do
                {
                    nextCalendarTick = Math.min(nextCalendarTick + Calendars.SERVER.getCalendarTicksInMonth(), currentCalendarTick);

                    final float temperatureAtNextTick = GreenhouseTemperatureHelper.getControlledTemperature(
                        level,
                        pos,
                        Climate.getTemperature(level, pos, nextCalendarTick, Calendars.SERVER.getCalendarDaysInMonth())
                    );
                    final Lifecycle lifecycleAtNextTick = Lifecycle.FRUITING;
                    if (range.checkBoth(hydration, temperatureAtNextTick, false))
                    {
                        currentLifecycle = currentLifecycle.advanceTowards(lifecycleAtNextTick);
                    }
                    else
                    {
                        currentLifecycle = Lifecycle.DORMANT;
                    }
                }
                while (nextCalendarTick < currentCalendarTick);

                final BlockState newState = growAndPropagate(level, pos, level.getRandom(), state.setValue(StationaryBerryBushBlock.LIFECYCLE, currentLifecycle));

                if (state != newState)
                {
                    level.setBlock(pos, newState, 3);
                }
            }
        }

        ci.cancel();
    }
}
