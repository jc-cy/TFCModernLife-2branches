package com.jccy.tfcmodernlife.mixin;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Access to TFC's seasonal-plant internals so the controlled greenhouse can drive the seasonal lifecycle. */
@Mixin(value = SeasonalPlantBlock.class, remap = false)
public interface SeasonalPlantBlockAccessor
{
    @Accessor("climateRange")
    Supplier<ClimateRange> tfcml$getClimateRange();

    @Invoker("getLifecycleForCurrentMonth")
    Lifecycle tfcml$invokeGetLifecycleForCurrentMonth(Level level, BlockPos pos);
}
