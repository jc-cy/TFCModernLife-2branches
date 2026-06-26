package com.jccy.tfcmodernlife.compat.jei;

import com.jccy.tfcmodernlife.common.recipe.GreenhouseDisassemblyRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.dries007.tfc.compat.jei.JEIIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record GreenhouseDisassemblyExtension(GreenhouseDisassemblyRecipe recipe) implements ICraftingCategoryExtension
{
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper helper, IFocusGroup focuses)
    {
        helper.createAndSetInputs(builder, JEIIntegration.ITEM_STACK, recipe.getJeiInputs(), 0, 0);
        helper.createAndSetOutputs(builder, JEIIntegration.ITEM_STACK, recipe.getJeiOutputs());
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return recipe.getId();
    }

    @Override
    public int getWidth()
    {
        return 0;
    }

    @Override
    public int getHeight()
    {
        return 0;
    }
}
