package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class CellarAbstractContainerMenuMixin
{
    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract ItemStack getCarried();

    @Inject(method = "doClick", at = @At("HEAD"))
    private void tfc_modern_life$sanitizeCellarSlotBeforeDirectTransfer(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci)
    {
        if ((clickType == ClickType.SWAP || clickType == ClickType.THROW) && slotId >= 0 && slotId < slots.size())
        {
            CellarPreservationHelper.sanitizeCellarContainerSlot(slots.get(slotId));
        }
    }

    @Inject(method = "moveItemStackTo", at = @At("RETURN"))
    private void tfc_modern_life$sanitizePlayerInventoryDestination(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, CallbackInfoReturnable<Boolean> cir)
    {
        if (!cir.getReturnValueZ())
        {
            return;
        }
        for (int index = startIndex; index < endIndex; index++)
        {
            final Slot slot = slots.get(index);
            if (slot.container instanceof Inventory)
            {
                if (CellarPreservationHelper.sanitizeStack(slot.getItem()))
                {
                    slot.setChanged();
                }
            }
        }
    }

    @Inject(method = "clicked", at = @At("HEAD"))
    private void tfc_modern_life$sanitizeCarriedBeforeClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci)
    {
        CellarPreservationHelper.sanitizeStack(getCarried());
    }

    @Inject(method = "clicked", at = @At("TAIL"))
    private void tfc_modern_life$sanitizeCarriedAfterClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci)
    {
        CellarPreservationHelper.sanitizeStack(getCarried());
    }
}
