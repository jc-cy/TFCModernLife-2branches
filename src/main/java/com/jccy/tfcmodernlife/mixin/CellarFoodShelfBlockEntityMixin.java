package com.jccy.tfcmodernlife.mixin;

import com.eerussianguy.firmalife.common.blockentities.FoodShelfBlockEntity;
import com.jccy.tfcmodernlife.common.ModFoodTraits;
import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoodShelfBlockEntity.class, remap = false)
public abstract class CellarFoodShelfBlockEntityMixin
{
    @Inject(method = "getFoodTrait", at = @At("HEAD"), cancellable = true)
    private void tfc_modern_life$useCellarTrait(CallbackInfoReturnable<FoodTrait> cir)
    {
        final FoodShelfBlockEntity shelf = (FoodShelfBlockEntity) (Object) this;
        final Level level = shelf.getLevel();
        cir.setReturnValue(level != null ? CellarPreservationHelper.getCellarTrait(level, shelf.getBlockPos()) : ModFoodTraits.getCellarTraits().get(ModFoodTraits.getCellarTraits().size() - 1));
    }

    @Inject(method = "getPossibleTraits", at = @At("HEAD"), cancellable = true)
    private void tfc_modern_life$useAllCellarTraits(CallbackInfoReturnable<FoodTrait[]> cir)
    {
        cir.setReturnValue(CellarPreservationHelper.getPossibleTraits());
    }

    @Inject(method = "updatePreservation", at = @At("HEAD"), cancellable = true)
    private void tfc_modern_life$normalizeShelfPreservation(boolean preserved, CallbackInfo ci)
    {
        CellarPreservationHelper.syncFoodShelfBlockEntity((FoodShelfBlockEntity) (Object) this, preserved);
        ci.cancel();
    }

    @Redirect(
        method = {"isItemValid", "use"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/common/capabilities/food/FoodCapability;removeTrait(Lnet/minecraft/world/item/ItemStack;Lnet/dries007/tfc/common/capabilities/food/FoodTrait;)Lnet/minecraft/world/item/ItemStack;"
        ),
        require = 0
    )
    private ItemStack tfc_modern_life$sanitizeAllCellarTraits(ItemStack stack, FoodTrait ignored)
    {
        return CellarPreservationHelper.sanitizeTakenStack(stack);
    }
}
