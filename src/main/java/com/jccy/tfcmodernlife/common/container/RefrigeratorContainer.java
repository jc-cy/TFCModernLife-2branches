package com.jccy.tfcmodernlife.common.container;

import com.jccy.tfcmodernlife.common.ModContainerTypes;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import net.minecraft.world.entity.player.Inventory;

public class RefrigeratorContainer extends ClimateControlContainer<RefrigeratorBlockEntity>
{
    public static RefrigeratorContainer create(RefrigeratorBlockEntity blockEntity, Inventory playerInventory, int windowId)
    {
        return new RefrigeratorContainer(blockEntity, playerInventory, windowId);
    }

    private RefrigeratorContainer(RefrigeratorBlockEntity blockEntity, Inventory playerInventory, int windowId)
    {
        super(ModContainerTypes.REFRIGERATOR.get(), windowId, blockEntity, playerInventory, 1);
    }
}
