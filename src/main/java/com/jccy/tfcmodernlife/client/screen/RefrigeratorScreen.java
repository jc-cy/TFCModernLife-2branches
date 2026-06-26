package com.jccy.tfcmodernlife.client.screen;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.container.RefrigeratorContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RefrigeratorScreen extends ClimateControlScreen<RefrigeratorBlockEntity, RefrigeratorContainer>
{
    private static final ResourceLocation BACKGROUND = new ResourceLocation(TFCModernLife.MOD_ID, "textures/gui/refrigerator.png");

    public RefrigeratorScreen(RefrigeratorContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
    }

    @Override
    protected ResourceLocation getBackground()
    {
        return BACKGROUND;
    }

    @Override
    protected boolean hasHeatIndicator()
    {
        return false;
    }

}
