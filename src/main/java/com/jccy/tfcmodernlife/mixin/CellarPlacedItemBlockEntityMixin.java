package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.PlacedItemBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = PlacedItemBlockEntity.class, remap = false)
public abstract class CellarPlacedItemBlockEntityMixin
{
    @ModifyArg(
        method = "onRightClick(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;ZZ)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/items/ItemHandlerHelper;giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false
        ),
        index = 1
    )
    private ItemStack tfc_modern_life$sanitizePlacedItemPickup(ItemStack stack)
    {
        return CellarPreservationHelper.sanitizeTakenStack(stack);
    }
}
