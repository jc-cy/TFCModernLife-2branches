package com.jccy.tfcmodernlife.mixin;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.ClimateRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = SeasonalPlantBlock.class, remap = false)
public interface TfeSeasonalPlantBlockAccessor
{
    @Accessor("climateRange")
    Supplier<ClimateRange> tfcml$getClimateRange();

    @Invoker("getLifecycleForMonth")
    Lifecycle tfcml$invokeGetLifecycleForMonth(Month month);
}
