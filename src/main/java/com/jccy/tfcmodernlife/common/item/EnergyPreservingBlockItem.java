package com.jccy.tfcmodernlife.common.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class EnergyPreservingBlockItem extends BlockItem
{
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String ENERGY_TAG = "energy";

    public EnergyPreservingBlockItem(Block block, Properties properties)
    {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, level, tooltip, flag);
        final CompoundTag blockEntityTag = stack.getTagElement(BLOCK_ENTITY_TAG);
        int energy = 0;
        if (blockEntityTag != null && blockEntityTag.contains(ENERGY_TAG, Tag.TAG_INT))
        {
            energy = Math.max(0, blockEntityTag.getInt(ENERGY_TAG));
        }
        tooltip.add(Component.translatable("tfc_modern_life.tooltip.remaining_energy", energy).withStyle(ChatFormatting.GRAY));
    }
}
