package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.recipe.GreenhouseSalvageRecipe;
import net.dries007.tfc.common.recipes.RecipeSerializerImpl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TFCModernLife.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GreenhouseSalvageRecipe>> GREENHOUSE_SALVAGE =
        RECIPE_SERIALIZERS.register("greenhouse_salvage", () -> new RecipeSerializerImpl<>(GreenhouseSalvageRecipe.CODEC, GreenhouseSalvageRecipe.STREAM_CODEC));

    private ModRecipeSerializers() {}
}
