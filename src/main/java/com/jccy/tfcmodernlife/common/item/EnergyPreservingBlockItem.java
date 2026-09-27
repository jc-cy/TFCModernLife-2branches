package com.jccy.tfcmodernlife.common.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

public class EnergyPreservingBlockItem extends BlockItem
{
    private static final String ENERGY_TAG = "energy";

    public EnergyPreservingBlockItem(Block block, Properties properties)
    {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, tooltip, flag);
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        final CompoundTag blockEntityTag = customData == null ? null : customData.copyTag();
        int energy = 0;
        if (blockEntityTag != null && blockEntityTag.contains(ENERGY_TAG, Tag.TAG_INT))
        {
            energy = Math.max(0, blockEntityTag.getInt(ENERGY_TAG));
        }
        tooltip.add(Component.translatable("tfc_modern_life.tooltip.remaining_energy", energy).withStyle(ChatFormatting.GRAY));
    }
}
