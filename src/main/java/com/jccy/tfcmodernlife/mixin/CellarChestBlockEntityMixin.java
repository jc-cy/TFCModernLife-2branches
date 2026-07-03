package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlockEntity.class)
public abstract class CellarChestBlockEntityMixin
{
    @Inject(method = "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", at = @At("RETURN"), cancellable = true, remap = false)
    private <T> void tfc_modern_life$wrapCellarItemHandler(Capability<T> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir)
    {
        final BlockEntity owner = (BlockEntity) (Object) this;
        if (cap == Capabilities.ITEM && CellarPreservationHelper.canWrapBlockEntityItemHandler(owner))
        {
            final LazyOptional<IItemHandler> handlers = cir.getReturnValue().cast();
            cir.setReturnValue(handlers
                .lazyMap(handler -> CellarPreservationHelper.wrapBlockEntityItemHandler(owner, handler))
                .cast());
        }
    }
}
