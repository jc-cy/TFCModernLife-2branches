package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;

@Mixin(InventoryBlockEntity.class)
public abstract class CellarInventoryBlockEntityMixin
{
    @Inject(method = "setAndUpdateSlots", at = @At("TAIL"))
    private void tfcml$syncCellarTraitsOnSlotChange(int slot, CallbackInfo ci)
    {
        CellarPreservationHelper.syncInventoryBlockEntity((InventoryBlockEntity<?>) (Object) this);
    }

    @Inject(method = "ejectInventory", at = @At("HEAD"))
    private void tfcml$sanitizeDroppedCellarItems(CallbackInfo ci)
    {
        CellarPreservationHelper.sanitizeInventoryForDrop((InventoryBlockEntity<?>) (Object) this);
    }

    @Inject(method = "getSidedInventory", at = @At("RETURN"), cancellable = true)
    private void tfcml$wrapCellarSidedInventory(@Nullable Direction context, CallbackInfoReturnable<IItemHandler> cir)
    {
        cir.setReturnValue(CellarPreservationHelper.wrapSidedInventory((InventoryBlockEntity<?>) (Object) this, cir.getReturnValue()));
    }
}
