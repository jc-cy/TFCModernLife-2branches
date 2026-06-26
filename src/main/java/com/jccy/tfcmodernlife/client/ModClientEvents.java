package com.jccy.tfcmodernlife.client;

import com.jccy.tfcmodernlife.client.render.ElectricSoupPotBlockEntityRenderer;
import com.jccy.tfcmodernlife.client.render.ElectricOvenBlockEntityRenderer;
import com.jccy.tfcmodernlife.client.screen.ElectricOvenScreen;
import com.jccy.tfcmodernlife.client.screen.ElectricSoupPotScreen;
import com.jccy.tfcmodernlife.client.screen.RefrigeratorScreen;
import com.jccy.tfcmodernlife.client.screen.ThermostaticAirConditionerScreen;
import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.ModContainerTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ModClientEvents
{
    private ModClientEvents() {}

    public static void register(IEventBus modBus)
    {
        modBus.addListener(ModClientEvents::clientSetup);
        modBus.addListener(ModClientEvents::registerRenderers);
    }

    private static void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register(ModContainerTypes.ELECTRIC_OVEN.get(), ElectricOvenScreen::new);
            MenuScreens.register(ModContainerTypes.ELECTRIC_SOUP_POT.get(), ElectricSoupPotScreen::new);
            MenuScreens.register(ModContainerTypes.THERMOSTATIC_AIR_CONDITIONER.get(), ThermostaticAirConditionerScreen::new);
            MenuScreens.register(ModContainerTypes.REFRIGERATOR.get(), RefrigeratorScreen::new);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELECTRIC_OVEN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELECTRIC_SOUP_POT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.THERMOSTATIC_AIR_CONDITIONER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.REFRIGERATOR.get(), RenderType.cutout());
        });
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        BlockEntityRenderers.register(ModBlocks.ELECTRIC_OVEN_BLOCK_ENTITY.get(), ElectricOvenBlockEntityRenderer::new);
        BlockEntityRenderers.register(ModBlocks.ELECTRIC_SOUP_POT_BLOCK_ENTITY.get(), ElectricSoupPotBlockEntityRenderer::new);
    }
}
