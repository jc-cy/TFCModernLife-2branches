package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.container.ISlotCallback;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CallbackSlot.class, remap = false)
public abstract class CellarCallbackSlotMixin
{
    @Shadow @Final private ISlotCallback callback;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void tfc_modern_life$removeCellarTraitsOnTake(Player player, ItemStack stack, CallbackInfo ci)
    {
        if (callback instanceof InventoryBlockEntity<?> inventory)
        {
            CellarPreservationHelper.sanitizeTakenStack(inventory, stack);
        }
    }
}
