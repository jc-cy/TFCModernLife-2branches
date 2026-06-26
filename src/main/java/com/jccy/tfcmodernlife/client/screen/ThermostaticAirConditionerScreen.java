package com.jccy.tfcmodernlife.client.screen;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.container.ThermostaticAirConditionerContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ThermostaticAirConditionerScreen extends ClimateControlScreen<ThermostaticAirConditionerBlockEntity, ThermostaticAirConditionerContainer>
{
    private static final ResourceLocation BACKGROUND = new ResourceLocation(TFCModernLife.MOD_ID, "textures/gui/thermostatic_air_conditioner.png");

    public ThermostaticAirConditionerScreen(ThermostaticAirConditionerContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
    }

    @Override
    protected ResourceLocation getBackground()
    {
        return BACKGROUND;
    }

}
