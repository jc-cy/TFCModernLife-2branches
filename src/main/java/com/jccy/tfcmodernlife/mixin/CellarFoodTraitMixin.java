package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.ModFoodTraits;
import java.util.List;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FoodTrait.class, remap = false)
public abstract class CellarFoodTraitMixin
{
    @Inject(method = "addTooltipInfo", at = @At("HEAD"), cancellable = true)
    private void tfc_modern_life$showCellarMultiplier(ItemStack stack, List<Component> tooltip, CallbackInfo ci)
    {
        final FoodTrait trait = (FoodTrait) (Object) this;
        if (!ModFoodTraits.isCellarTrait(trait))
        {
            return;
        }

        tooltip.add(Component.translatable(
            "tfc_modern_life.tooltip.food_trait.cellar_multiplier",
            ModFoodTraits.getCellarTraitMultiplierText(trait)
        ));
        ci.cancel();
    }
}
