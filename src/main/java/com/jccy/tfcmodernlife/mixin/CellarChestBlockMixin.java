package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public abstract class CellarChestBlockMixin
{
    @Inject(method = "getMenuProvider", at = @At("HEAD"))
    private void tfc_modern_life$syncChestOnMenuProvider(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir)
    {
        if (level.isClientSide())
        {
            return;
        }
        tfc_modern_life$syncContainerAt(level, pos);
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            tfc_modern_life$syncContainerAt(level, pos.relative(direction));
        }
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void tfc_modern_life$sanitizeTFCChestBeforeDrop(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci)
    {
        if (!level.isClientSide() && state.getBlock() != newState.getBlock())
        {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Container container)
            {
                CellarPreservationHelper.sanitizeContainerBlockEntityForDrop(blockEntity, container);
            }
        }
    }

    private void tfc_modern_life$syncContainerAt(Level level, BlockPos pos)
    {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Container container)
        {
            CellarPreservationHelper.syncContainerBlockEntity(blockEntity, container);
        }
    }
}
