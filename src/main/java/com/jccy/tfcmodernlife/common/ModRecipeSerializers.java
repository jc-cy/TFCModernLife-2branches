package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.recipe.GreenhouseDisassemblyRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TFCModernLife.MOD_ID);

    public static final RegistryObject<GreenhouseDisassemblyRecipe.Serializer> GREENHOUSE_DISASSEMBLY = RECIPE_SERIALIZERS.register(
        "greenhouse_disassembly",
        GreenhouseDisassemblyRecipe.Serializer::new
    );

    private ModRecipeSerializers() {}

    public static void register(IEventBus bus)
    {
        RECIPE_SERIALIZERS.register(bus);
    }
}
