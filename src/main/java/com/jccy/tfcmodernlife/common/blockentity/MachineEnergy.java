package com.jccy.tfcmodernlife.common.blockentity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Keeps a machine's buffered FE in the item form of its block, so breaking and re-placing a machine keeps the charge
 * (the 1.20 addon stored the same value in {@code BlockEntityTag}; 1.21 moved it to the item's custom data).
 */
public final class MachineEnergy
{
    private static final String ENERGY_KEY = "energy";

    private MachineEnergy() {}

    public static void saveToItem(ItemStack stack, int energy)
    {
        if (stack.isEmpty())
        {
            return;
        }
        final CompoundTag customData = read(stack);
        customData.putInt(ENERGY_KEY, energy);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
    }

    /**
     * @return the stored charge, or {@code -1} when the item does not carry one.
     */
    public static int readFromItem(ItemStack stack)
    {
        final CompoundTag customData = read(stack);
        return customData.contains(ENERGY_KEY) ? customData.getInt(ENERGY_KEY) : -1;
    }

    private static CompoundTag read(ItemStack stack)
    {
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? new CompoundTag() : customData.copyTag();
    }
}
