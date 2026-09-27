package com.jccy.tfcmodernlife.common.container;

import com.jccy.tfcmodernlife.common.blockentity.ClimateControlBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ClimateControlContainer<T extends ClimateControlBlockEntity> extends AbstractContainerMenu
{
    public static final int BUTTON_TOGGLE_POWER = 4;
    private static final int PLAYER_INVENTORY_Y = 104;
    private static final int HOTBAR_Y = 162;
    protected final T blockEntity;
    private final int step;

    protected ClimateControlContainer(MenuType<?> type, int windowId, T blockEntity, Inventory playerInventory, int step)
    {
        super(type, windowId);
        this.blockEntity = blockEntity;
        this.step = step;
        addDataSlots(blockEntity.getNetworkSyncData());
        addPlayerInventorySlots(playerInventory);
    }

    public T getBlockEntity()
    {
        return blockEntity;
    }

    public ContainerData getSyncData()
    {
        return blockEntity.getSyncData();
    }

    @Override
    public boolean clickMenuButton(Player player, int id)
    {
        switch (id)
        {
            case 0 -> blockEntity.adjustTarget(-5 * step);
            case 1 -> blockEntity.adjustTarget(-step);
            case 2 -> blockEntity.adjustTarget(step);
            case 3 -> blockEntity.adjustTarget(5 * step);
            case BUTTON_TOGGLE_POWER -> blockEntity.toggleEnabled();
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        blockEntity.refreshStructure(true);
    }

    @Override
    public boolean stillValid(Player player)
    {
        return !blockEntity.isRemoved() && player.distanceToSqr(
            blockEntity.getBlockPos().getX() + 0.5D,
            blockEntity.getBlockPos().getY() + 0.5D,
            blockEntity.getBlockPos().getZ() + 0.5D
        ) <= 64.0D;
    }

    private void addPlayerInventorySlots(Inventory playerInventory)
    {
        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                addSlot(new Slot(playerInventory, 9 + row * 9 + col, 8 + col * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++)
        {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }
}
