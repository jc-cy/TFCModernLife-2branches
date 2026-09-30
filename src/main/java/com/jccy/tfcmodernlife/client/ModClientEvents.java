package com.jccy.tfcmodernlife.client;

import com.jccy.tfcmodernlife.client.particle.RefrigeratorColdMistParticle;
import com.jccy.tfcmodernlife.client.particle.SoupPotBubbleParticle;
import com.jccy.tfcmodernlife.client.particle.SoupPotSteamParticle;
import com.jccy.tfcmodernlife.client.screen.RefrigeratorScreen;
import com.jccy.tfcmodernlife.client.screen.ThermostaticAirConditionerScreen;
import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.ModContainerTypes;
import com.jccy.tfcmodernlife.common.ModParticles;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public final class ModClientEvents
{
    private ModClientEvents() {}

    public static void register(IEventBus modBus)
    {
        KitchenClientEvents.register(modBus);
        modBus.addListener(ModClientEvents::registerScreens);
        modBus.addListener(ModClientEvents::registerParticleProviders);
        modBus.addListener(ModClientEvents::clientSetup);
    }

    /**
     * 把 NeoForge 官方配置界面挂到本模组的容器上。不注册的话，模组列表里的"配置"按钮会一直是灰的、
     * 点了也打不开，而且不会有任何报错（NeoForge 只对注册了 {@code IConfigScreenFactory} 的模组启用该按钮）。
     */
    public static void registerConfigScreen(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * Render layers are the client-side half of the block model set-up: without these the transparent parts of the
     * six non-full-cube models are rendered in the solid layer (opaque glass / fan openings), while the log stays
     * silent. Mirrors the 1.20 client setup, which the 1.21 port had dropped.
     */
    private static void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELECTRIC_OVEN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELECTRIC_SOUP_POT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.THERMOSTATIC_AIR_CONDITIONER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.REFRIGERATOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STAINLESS_STEEL_REINFORCED_SEALED_BRICK_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STAINLESS_STEEL_REINFORCED_SEALED_BRICK_TRAPDOOR.get(), RenderType.cutout());
        });
    }

    private static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ModContainerTypes.REFRIGERATOR.get(), RefrigeratorScreen::new);
        event.register(ModContainerTypes.THERMOSTATIC_AIR_CONDITIONER.get(), ThermostaticAirConditionerScreen::new);
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ModParticles.REFRIGERATOR_COLD_MIST.get(), RefrigeratorColdMistParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SOUP_POT_STEAM.get(), SoupPotSteamParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SOUP_POT_BUBBLE.get(), SoupPotBubbleParticle.Provider::new);
    }
}
