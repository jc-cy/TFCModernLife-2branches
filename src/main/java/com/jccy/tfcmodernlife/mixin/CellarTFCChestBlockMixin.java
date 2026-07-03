package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.blocks.wood.TFCChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TFCChestBlock.class, remap = false)
public abstract class CellarTFCChestBlockMixin
{
    @Inject(method = {"getMenuProvider", "m_7246_"}, at = @At("HEAD"), require = 0)
    private void tfc_modern_life$syncCellarTraitsOnMenuProvider(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir)
    {
        if (level.isClientSide())
        {
            return;
        }
        tfc_modern_life$syncChestAt(level, pos);
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            tfc_modern_life$syncChestAt(level, pos.relative(direction));
        }
    }

    private void tfc_modern_life$syncChestAt(Level level, BlockPos pos)
    {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TFCChestBlockEntity chest)
        {
            CellarPreservationHelper.syncTFCChestBlockEntity(chest);
        }
    }
}
