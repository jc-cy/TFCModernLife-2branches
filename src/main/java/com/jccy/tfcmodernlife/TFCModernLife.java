package com.jccy.tfcmodernlife;

import com.jccy.tfcmodernlife.client.ModClientEvents;
import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.ModConfig;
import com.jccy.tfcmodernlife.common.ModContainerTypes;
import com.jccy.tfcmodernlife.common.ModCreativeTab;
import com.jccy.tfcmodernlife.common.ModFoodTraits;
import com.jccy.tfcmodernlife.common.ModRecipeSerializers;
import com.jccy.tfcmodernlife.common.ModSounds;
import com.jccy.tfcmodernlife.common.climate.ClimateControlConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TFCModernLife.MOD_ID)
public final class TFCModernLife
{
    public static final String MOD_ID = "tfc_modern_life";

    public TFCModernLife()
    {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        ModFoodTraits.init();
        ModBlocks.register(modBus);
        ModContainerTypes.register(modBus);
        ModCreativeTab.register(modBus);
        ModRecipeSerializers.register(modBus);
        ModSounds.register(modBus);
        ClimateControlConfig.registerReloadListener(modBus);

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            ModClientEvents.register(modBus);
        }
    }
}
