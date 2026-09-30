package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TFCChestBlockEntity.class)
public abstract class TFCChestBlockEntityMixin
{
    @Inject(method = "createMenu", at = @At("HEAD"))
    private void tfcml$syncCellarTraitsOnOpen(int id, Inventory inventory, CallbackInfoReturnable<AbstractContainerMenu> cir)
    {
        CellarPreservationHelper.syncChestBlockEntity((TFCChestBlockEntity) (Object) this);
    }

    @Inject(method = "getInventoryHandler", at = @At("RETURN"), cancellable = true, require = 0)
    private void tfcml$wrapCellarItemHandler(@Nullable Direction side, CallbackInfoReturnable<IItemHandler> cir)
    {
        cir.setReturnValue(CellarPreservationHelper.wrapBlockEntityItemHandler((TFCChestBlockEntity) (Object) this, cir.getReturnValue()));
    }

    public void setAndUpdateSlots(int slot)
    {
        CellarPreservationHelper.syncChestBlockEntity((TFCChestBlockEntity) (Object) this);
    }
}
