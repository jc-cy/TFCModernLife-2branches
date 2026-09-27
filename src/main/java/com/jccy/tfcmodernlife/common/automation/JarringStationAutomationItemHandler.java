package com.jccy.tfcmodernlife.common.automation;

import com.eerussianguy.firmalife.common.blockentities.JarringStationBlockEntity;
import com.jccy.tfcmodernlife.common.ModConfig;
import com.jccy.tfcmodernlife.common.compat.JamJarCompat;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public final class JarringStationAutomationItemHandler implements IItemHandlerModifiable
{
    private final JarringStationBlockEntity station;
    private final IItemHandlerModifiable delegate;

    public JarringStationAutomationItemHandler(JarringStationBlockEntity station, IItemHandlerModifiable delegate)
    {
        this.station = station;
        this.delegate = delegate;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            delegate.setStackInSlot(slot, stack);
            notifyJarringStation();
        }
        else if (JamJarCompat.isSupportedEmptyJar(stack))
        {
            delegate.setStackInSlot(slot, stack.copyWithCount(Math.min(stack.getCount(), Math.min(getSlotLimit(slot), stack.getMaxStackSize()))));
            notifyJarringStation();
        }
        else if (canInsert(stack))
        {
            delegate.setStackInSlot(slot, stack);
            notifyJarringStation();
        }
    }

    @Override
    public int getSlots()
    {
        return delegate.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return delegate.getStackInSlot(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        final ItemStack remainder = canInsert(stack)
            ? JamJarCompat.isSupportedEmptyJar(stack) ? insertKnownJar(slot, stack, simulate) : delegate.insertItem(slot, stack, simulate)
            : stack;
        if (!simulate && remainder.getCount() != stack.getCount())
        {
            notifyJarringStation();
        }
        return remainder;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        final ItemStack stack = delegate.getStackInSlot(slot);
        final ItemStack extracted = canExtract(stack) ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        if (!simulate && !extracted.isEmpty())
        {
            notifyJarringStation();
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return JamJarCompat.isSupportedEmptyJar(stack) || (canInsert(stack) && delegate.isItemValid(slot, stack));
    }

    private boolean canInsert(ItemStack stack)
    {
        return !stack.isEmpty()
            && (JamJarCompat.isSupportedEmptyJar(stack) || matches(stack, ModConfig.JARRING_STATION_INSERT_WHITELIST.get()));
    }

    private boolean canExtract(ItemStack stack)
    {
        return !stack.isEmpty()
            && !JamJarCompat.isSupportedEmptyJar(stack)
            && !matches(stack, ModConfig.JARRING_STATION_INSERT_WHITELIST.get())
            && !matches(stack, ModConfig.JARRING_STATION_EXTRACT_BLACKLIST.get());
    }

    private static boolean matches(ItemStack stack, List<? extends String> entries)
    {
        for (String rawEntry : entries)
        {
            final String entry = rawEntry.trim();
            if (entry.isEmpty())
            {
                continue;
            }
            if (entry.charAt(0) == '#')
            {
                final ResourceLocation id = ResourceLocation.tryParse(entry.substring(1));
                if (id != null && stack.is(TagKey.create(Registries.ITEM, id)))
                {
                    return true;
                }
            }
            else
            {
                final ResourceLocation id = ResourceLocation.tryParse(entry);
                final Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                if (item != null && stack.is(item))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack insertKnownJar(int slot, ItemStack stack, boolean simulate)
    {
        final ItemStack existing = delegate.getStackInSlot(slot);
        if (!existing.isEmpty())
        {
            return stack;
        }

        final int inserted = Math.min(stack.getCount(), Math.min(getSlotLimit(slot), stack.getMaxStackSize()));
        if (inserted <= 0)
        {
            return stack;
        }

        if (!simulate)
        {
            delegate.setStackInSlot(slot, stack.copyWithCount(inserted));
        }

        final ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    private void notifyJarringStation()
    {
        if (!JarringStationAutomationBridge.tryFillFromStation(station))
        {
            JarringStationAutomationBridge.syncStation(station);
        }
    }
}
