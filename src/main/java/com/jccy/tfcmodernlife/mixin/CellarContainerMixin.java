package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.common.container.ISlotCallback;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Container.class, remap = false)
public abstract class CellarContainerMixin
{
    @Shadow protected ISlotCallback callback;

    @Inject(method = "setCarried", at = @At("HEAD"))
    private void tfc_modern_life$removeCellarTraitsFromCarried(ItemStack stack, CallbackInfo ci)
    {
        if (callback instanceof InventoryBlockEntity<?> inventory)
        {
            CellarPreservationHelper.sanitizeTakenStack(inventory, stack);
        }
    }
}
