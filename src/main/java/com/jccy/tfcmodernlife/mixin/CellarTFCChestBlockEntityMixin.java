package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
}
