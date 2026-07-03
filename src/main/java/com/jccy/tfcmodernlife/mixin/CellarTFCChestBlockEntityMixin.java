package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TFCChestBlockEntity.class, remap = false)
public abstract class CellarTFCChestBlockEntityMixin
{
    @Inject(method = {"createMenu", "m_6555_"}, at = @At("HEAD"), require = 0)
    private void tfc_modern_life$syncCellarTraitsOnOpen(int id, Inventory inventory, CallbackInfoReturnable<AbstractContainerMenu> cir)
    {
        CellarPreservationHelper.syncTFCChestBlockEntity((TFCChestBlockEntity) (Object) this);
    }

    @Inject(method = "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", at = @At("RETURN"), cancellable = true)
    private <T> void tfc_modern_life$wrapCellarChestCapability(Capability<T> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir)
    {
        if (cap == Capabilities.ITEM)
        {
            final BlockEntity owner = (BlockEntity) (Object) this;
            final LazyOptional<IItemHandler> handlers = cir.getReturnValue().cast();
            cir.setReturnValue(handlers
                .lazyMap(handler -> CellarPreservationHelper.wrapBlockEntityItemHandler(owner, handler))
                .cast());
        }
    }
}
