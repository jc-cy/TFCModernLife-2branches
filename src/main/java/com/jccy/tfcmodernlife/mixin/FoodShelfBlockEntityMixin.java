package com.jccy.tfcmodernlife.mixin;

import com.eerussianguy.firmalife.common.blockentities.FoodShelfBlockEntity;
import com.jccy.tfcmodernlife.common.ModFoodTraits;
import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.dries007.tfc.common.component.food.FoodTrait;
import java.util.Set;

@Mixin(FoodShelfBlockEntity.class)
public abstract class FoodShelfBlockEntityMixin
{
    @Inject(method = "getFoodTrait", at = @At("HEAD"), cancellable = true)
    private void tfcml$usePatchCellarTrait(CallbackInfoReturnable<Holder<FoodTrait>> cir)
    {
        final FoodShelfBlockEntity shelf = (FoodShelfBlockEntity) (Object) this;
        final Level level = shelf.getLevel();
        final var cellarTraits = ModFoodTraits.getCellarTraits();
        cir.setReturnValue(level != null
            ? CellarPreservationHelper.getCellarTrait(level, shelf.getBlockPos())
            : cellarTraits.get(cellarTraits.size() - 1));
    }

    @Inject(method = "getPossibleTraits", at = @At("HEAD"), cancellable = true)
    private void tfcml$useAllCellarTraits(CallbackInfoReturnable<Set<DeferredHolder<FoodTrait, FoodTrait>>> cir)
    {
        cir.setReturnValue(CellarPreservationHelper.getPossibleTraits());
    }

    @Inject(method = "updatePreservation", at = @At("HEAD"), cancellable = true)
    private void tfcml$normalizeShelfPreservation(boolean preserved, CallbackInfo ci)
    {
        CellarPreservationHelper.syncFoodShelfBlockEntity((FoodShelfBlockEntity) (Object) this, preserved);
        ci.cancel();
    }

    @Redirect(
        method = {"isItemValid", "use"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/dries007/tfc/common/component/food/FoodCapability;removeTrait(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"
        ),
        require = 0
    )
    private ItemStack tfcml$sanitizeAllCellarTraits(ItemStack stack, Holder<FoodTrait> ignored)
    {
        return CellarPreservationHelper.sanitizeTakenStack(stack);
    }
}
