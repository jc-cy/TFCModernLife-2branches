package com.jccy.tfcmodernlife.common.container;

import com.jccy.tfcmodernlife.common.ModContainerTypes;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import net.minecraft.world.entity.player.Inventory;

public class ThermostaticAirConditionerContainer extends ClimateControlContainer<ThermostaticAirConditionerBlockEntity>
{
    public static ThermostaticAirConditionerContainer create(ThermostaticAirConditionerBlockEntity blockEntity, Inventory playerInventory, int windowId)
    {
        return new ThermostaticAirConditionerContainer(blockEntity, playerInventory, windowId);
    }

    private ThermostaticAirConditionerContainer(ThermostaticAirConditionerBlockEntity blockEntity, Inventory playerInventory, int windowId)
    {
        super(ModContainerTypes.THERMOSTATIC_AIR_CONDITIONER.get(), windowId, blockEntity, playerInventory, 1);
    }
}
