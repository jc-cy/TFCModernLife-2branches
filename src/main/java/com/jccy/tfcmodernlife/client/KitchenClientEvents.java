package com.jccy.tfcmodernlife.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.ModContainerTypes;
import com.jccy.tfcmodernlife.client.render.ElectricOvenBlockEntityRenderer;
import com.jccy.tfcmodernlife.client.render.ElectricSoupPotBlockEntityRenderer;
import com.jccy.tfcmodernlife.client.screen.ElectricOvenScreen;
import com.jccy.tfcmodernlife.client.screen.ElectricSoupPotScreen;

public final class KitchenClientEvents
{
    public static void register(IEventBus modBus)
    {
        modBus.addListener(KitchenClientEvents::registerScreens);
        modBus.addListener(KitchenClientEvents::registerRenderers);
    }

    private static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(ModContainerTypes.ELECTRIC_OVEN.get(), ElectricOvenScreen::new);
        event.register(ModContainerTypes.ELECTRIC_SOUP_POT.get(), ElectricSoupPotScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(ModBlocks.ELECTRIC_OVEN_BLOCK_ENTITY.get(), ElectricOvenBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.ELECTRIC_SOUP_POT_BLOCK_ENTITY.get(), ElectricSoupPotBlockEntityRenderer::new);
    }
}
