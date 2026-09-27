package com.jccy.tfcmodernlife.compat.jei;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.recipe.GreenhouseSalvageRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class TFCModernLifeGreenhouseJeiPlugin implements IModPlugin
{
    @Override
    public ResourceLocation getPluginUid()
    {
        return ResourceLocation.fromNamespaceAndPath(TFCModernLife.MOD_ID, "greenhouse_jei");
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registry)
    {
        registry.getCraftingCategory().addExtension(GreenhouseSalvageRecipe.class, GreenhouseSalvageCraftingExtension.INSTANCE);
    }
}
