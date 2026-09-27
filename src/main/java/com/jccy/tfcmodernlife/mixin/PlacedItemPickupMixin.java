package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.blockentities.PlacedItemBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = PlacedItemBlockEntity.class, remap = false)
public abstract class PlacedItemPickupMixin
{
    @ModifyArg(
        method = "onRightClick(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;ZZ)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/items/ItemHandlerHelper;giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V"
        ),
        index = 1,
        require = 0
    )
    private ItemStack tfcml$sanitizePlacedItemPickup(ItemStack stack)
    {
        return CellarPreservationHelper.sanitizeTakenStack(stack);
    }
}
