package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.recipe.GreenhouseDisassemblyRecipe;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class GreenhouseDisassemblyResultSlotMixin
{
    @Shadow @Final private CraftingContainer craftSlots;

    @Unique private @Nullable GreenhouseDisassemblyRecipe tfcModernLife$greenhouseDisassemblyRecipe;
    @Unique private int[] tfcModernLife$extraInputConsumption = new int[0];

    @Inject(method = "onTake", at = @At("HEAD"))
    private void tfcModernLife$captureGreenhouseDisassembly(Player player, ItemStack stack, CallbackInfo ci)
    {
        tfcModernLife$greenhouseDisassemblyRecipe = null;
        tfcModernLife$extraInputConsumption = new int[0];
        player.level().getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftSlots, player.level())
            .ifPresent(recipe -> {
                if (recipe instanceof GreenhouseDisassemblyRecipe disassembly)
                {
                    tfcModernLife$greenhouseDisassemblyRecipe = disassembly;
                    tfcModernLife$extraInputConsumption = disassembly.extraInputConsumption(craftSlots);
                }
            });
    }

    @Inject(method = "onTake", at = @At("TAIL"))
    private void tfcModernLife$consumeStackedGreenhouseInputs(Player player, ItemStack stack, CallbackInfo ci)
    {
        final GreenhouseDisassemblyRecipe recipe = tfcModernLife$greenhouseDisassemblyRecipe;
        if (recipe == null)
        {
            return;
        }

        for (int i = 0; i < craftSlots.getContainerSize() && i < tfcModernLife$extraInputConsumption.length; i++)
        {
            final int extraToConsume = tfcModernLife$extraInputConsumption[i];
            if (extraToConsume <= 0)
            {
                continue;
            }
            final ItemStack slotStack = craftSlots.getItem(i);
            if (!slotStack.isEmpty() && recipe.isInput(slotStack))
            {
                slotStack.shrink(extraToConsume);
                if (slotStack.isEmpty())
                {
                    craftSlots.setItem(i, ItemStack.EMPTY);
                }
            }
        }
        craftSlots.setChanged();
        tfcModernLife$greenhouseDisassemblyRecipe = null;
        tfcModernLife$extraInputConsumption = new int[0];
    }
}
