package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.blockentities.FoodShelfBlockEntity;
import com.eerussianguy.firmalife.common.items.FLFoodTraits;
import com.jccy.tfcmodernlife.common.ModFoodTraits;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public final class CellarPreservationHelper
{
    private static final Set<Object> SYNCING = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final String IE_WOODEN_CRATE_BLOCK_ENTITY = "blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity";

    private CellarPreservationHelper() {}

    public static FoodTrait[] getPossibleTraits()
    {
        return ModFoodTraits.getCellarTraits().toArray(FoodTrait[]::new);
    }

    public static void syncTrackedInventories(ClimateStationAccess station)
    {
        if (!(station instanceof BlockEntity blockEntity))
        {
            return;
        }
        final Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide() || station.tfcml$getCellarStructureData() == null)
        {
            return;
        }
        final FoodTrait trait = getCellarTrait(level, blockEntity.getBlockPos());
        for (BlockPos pos : station.tfcml$getClimatePositions())
        {
            syncBlockEntity(level, pos, trait);
        }
    }

    public static void syncBlockEntities(Level level, Iterable<BlockPos> positions)
    {
        if (level.isClientSide())
        {
            return;
        }
        for (BlockPos pos : positions)
        {
            syncBlockEntity(level, pos);
        }
    }

    public static void syncBlockEntity(Level level, BlockPos pos)
    {
        if (level.isClientSide())
        {
            return;
        }
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, pos);
        final @Nullable FoodTrait trait = station != null ? getCellarTrait(level, pos) : null;
        syncBlockEntity(level, pos, trait);
    }

    public static void syncInventoryBlockEntity(InventoryBlockEntity<?> inventory)
    {
        if (!shouldHandleInventory(inventory))
        {
            return;
        }
        final Level level = inventory.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, inventory.getBlockPos());
        final @Nullable FoodTrait trait = station != null ? getCellarTrait(level, inventory.getBlockPos()) : null;
        syncInventoryBlockEntity(inventory, trait);
    }

    public static void syncTFCChestBlockEntity(TFCChestBlockEntity chest)
    {
        final Level level = chest.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, chest.getBlockPos());
        final @Nullable FoodTrait trait = station != null ? getCellarTrait(level, chest.getBlockPos()) : null;
        syncContainer(chest, chest, trait);
    }

    public static void syncFoodShelfBlockEntity(FoodShelfBlockEntity shelf)
    {
        final Level level = shelf.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, shelf.getBlockPos());
        final @Nullable FoodTrait trait = station != null ? getCellarTrait(level, shelf.getBlockPos()) : null;
        syncFoodShelfBlockEntity(shelf, trait);
    }

    public static void syncFoodShelfBlockEntity(FoodShelfBlockEntity shelf, boolean preserved)
    {
        final Level level = shelf.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        syncFoodShelfBlockEntity(shelf, preserved ? getCellarTrait(level, shelf.getBlockPos()) : null);
    }

    public static void syncExternalContainer(Container container)
    {
        if (container instanceof BlockEntity blockEntity && shouldHandleExternalContainer(blockEntity))
        {
            syncContainerBlockEntity(blockEntity, container);
        }
    }

    public static void syncContainerBlockEntity(BlockEntity blockEntity, Container container)
    {
        final Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, blockEntity.getBlockPos());
        final @Nullable FoodTrait trait = station != null ? getCellarTrait(level, blockEntity.getBlockPos()) : null;
        syncContainer(blockEntity, container, trait);
    }

    public static ItemStack sanitizeTakenStack(InventoryBlockEntity<?> inventory, ItemStack stack)
    {
        if (shouldHandleInventory(inventory))
        {
            sanitizeTakenStack(stack);
        }
        return stack;
    }

    public static ItemStack sanitizeTakenStack(ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            removeCellarTraits(stack);
        }
        return stack;
    }

    public static void sanitizeInventoryForDrop(InventoryBlockEntity<?> inventory)
    {
        if (!shouldHandleInventory(inventory))
        {
            return;
        }
        final IItemHandlerModifiable internal = getInventoryHandler(inventory);
        if (internal == null)
        {
            return;
        }
        boolean changed = false;
        for (int slot = 0; slot < internal.getSlots(); slot++)
        {
            changed |= removeCellarTraits(internal.getStackInSlot(slot));
        }
        if (changed)
        {
            inventory.setChanged();
        }
    }

    public static void sanitizeContainerBlockEntityForDrop(BlockEntity blockEntity, Container container)
    {
        sanitizeContainerForDrop(blockEntity, container);
    }

    public static IItemHandler wrapSidedInventory(InventoryBlockEntity<?> inventory, @Nullable IItemHandler handler)
    {
        if (handler == null || !shouldHandleInventory(inventory) || handler instanceof CellarInventoryWrapper || handler instanceof CellarInventoryModifiableWrapper)
        {
            return handler;
        }
        return handler instanceof IItemHandlerModifiable modifiable
            ? new CellarInventoryModifiableWrapper(inventory, modifiable)
            : new CellarInventoryWrapper(inventory, handler);
    }

    public static FoodTrait getCellarTrait(Level level, BlockPos pos)
    {
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, pos);
        final float baseTemperature = GreenhouseTemperatureHelper.getAmbientTemperature(level, pos);
        final float temperature = station != null
            ? station.tfcml$getEffectiveCellarTemperature(baseTemperature)
            : baseTemperature;
        final float multiplier = GreenhouseTemperatureHelper.getCellarPreservationMultiplier(temperature);
        return ModFoodTraits.getCellarTraitForMultiplier(multiplier);
    }

    public static float getCellarPreservationMultiplier(Level level, BlockPos pos)
    {
        final float decayModifier = getCellarTrait(level, pos).getDecayModifier();
        return decayModifier <= 0f ? Float.POSITIVE_INFINITY : 1f / decayModifier;
    }

    public static boolean shouldHandleInventory(InventoryBlockEntity<?> inventory)
    {
        return !(inventory instanceof com.eerussianguy.firmalife.common.blockentities.ClimateReceiver) && !(inventory instanceof ClimateStationAccess);
    }

    private static void syncBlockEntity(Level level, BlockPos pos, @Nullable FoodTrait trait)
    {
        final BlockEntity target = level.getBlockEntity(pos);
        if (target instanceof FoodShelfBlockEntity shelf)
        {
            syncFoodShelfBlockEntity(shelf, trait);
        }
        else if (target instanceof InventoryBlockEntity<?> inventory && shouldHandleInventory(inventory))
        {
            syncInventoryBlockEntity(inventory, trait);
        }
        else if (target instanceof TFCChestBlockEntity chest)
        {
            syncContainer(chest, chest, trait);
        }
        else if (target instanceof Container container && shouldHandleExternalContainer(target))
        {
            syncContainer(target, container, trait);
        }
    }

    private static void syncInventoryBlockEntity(InventoryBlockEntity<?> inventory, @Nullable FoodTrait trait)
    {
        if (!SYNCING.add(inventory))
        {
            return;
        }
        boolean changed = false;
        try
        {
            final IItemHandlerModifiable internal = getInventoryHandler(inventory);
            if (internal == null)
            {
                return;
            }
            for (int slot = 0; slot < internal.getSlots(); slot++)
            {
                changed |= normalizeCellarTraits(internal.getStackInSlot(slot), trait);
            }
        }
        finally
        {
            SYNCING.remove(inventory);
        }
        if (changed)
        {
            inventory.setChanged();
        }
    }

    private static void syncFoodShelfBlockEntity(FoodShelfBlockEntity shelf, @Nullable FoodTrait trait)
    {
        if (!SYNCING.add(shelf))
        {
            return;
        }
        boolean changed = false;
        try
        {
            final IItemHandlerModifiable internal = getInventoryHandler(shelf);
            if (internal == null)
            {
                return;
            }
            for (int slot = 0; slot < internal.getSlots(); slot++)
            {
                changed |= normalizeCellarTraits(internal.getStackInSlot(slot), trait);
            }
        }
        finally
        {
            SYNCING.remove(shelf);
        }
        if (changed)
        {
            shelf.setChanged();
            shelf.markForSync();
        }
    }

    private static void syncContainer(Object owner, Container container, @Nullable FoodTrait trait)
    {
        if (!SYNCING.add(owner))
        {
            return;
        }
        boolean changed = false;
        try
        {
            for (int slot = 0; slot < container.getContainerSize(); slot++)
            {
                changed |= normalizeCellarTraits(container.getItem(slot), trait);
            }
        }
        finally
        {
            SYNCING.remove(owner);
        }
        if (changed && owner instanceof BlockEntity blockEntity)
        {
            blockEntity.setChanged();
        }
    }

    private static boolean normalizeCellarTraits(ItemStack stack, @Nullable FoodTrait trait)
    {
        if (stack.isEmpty() || FoodCapability.get(stack) == null)
        {
            return false;
        }
        boolean changed = false;
        for (FoodTrait possible : getManagedCellarTraits())
        {
            if (trait != possible && FoodCapability.hasTrait(stack, possible))
            {
                FoodCapability.removeTrait(stack, possible);
                changed = true;
            }
        }
        if (trait != null && !FoodCapability.hasTrait(stack, trait))
        {
            FoodCapability.applyTrait(stack, trait);
            changed = true;
        }
        return changed;
    }

    private static boolean removeCellarTraits(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }
        boolean changed = false;
        for (FoodTrait trait : getManagedCellarTraits())
        {
            if (FoodCapability.hasTrait(stack, trait))
            {
                FoodCapability.removeTrait(stack, trait);
                changed = true;
            }
        }
        return changed;
    }

    private static void sanitizeContainerForDrop(Object owner, Container container)
    {
        if (!SYNCING.add(owner))
        {
            return;
        }
        boolean changed = false;
        try
        {
            for (int slot = 0; slot < container.getContainerSize(); slot++)
            {
                changed |= removeCellarTraits(container.getItem(slot));
            }
        }
        finally
        {
            SYNCING.remove(owner);
        }
        if (changed && owner instanceof BlockEntity blockEntity)
        {
            blockEntity.setChanged();
        }
    }

    private static boolean shouldHandleExternalContainer(BlockEntity blockEntity)
    {
        return IE_WOODEN_CRATE_BLOCK_ENTITY.equals(blockEntity.getClass().getName());
    }

    private static @Nullable IItemHandlerModifiable getInventoryHandler(InventoryBlockEntity<?> inventory)
    {
        return inventory.getCapability(Capabilities.ITEM).resolve()
            .filter(IItemHandlerModifiable.class::isInstance)
            .map(IItemHandlerModifiable.class::cast)
            .orElse(null);
    }

    private static Set<FoodTrait> getManagedCellarTraits()
    {
        final Set<FoodTrait> traits = new LinkedHashSet<>();
        traits.add(FLFoodTraits.SHELVED);
        traits.add(FLFoodTraits.SHELVED_2);
        traits.add(FLFoodTraits.SHELVED_3);
        traits.add(FLFoodTraits.HUNG);
        traits.add(FLFoodTraits.HUNG_2);
        traits.add(FLFoodTraits.HUNG_3);
        traits.addAll(ModFoodTraits.getCellarTraits());
        return traits;
    }

    private record CellarInventoryWrapper(InventoryBlockEntity<?> owner, IItemHandler delegate) implements IItemHandler
    {
        @Override public int getSlots() { return delegate.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }
        @Override public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return delegate.isItemValid(slot, stack); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            final Level level = owner.getLevel();
            if (level == null || level.isClientSide() || ClimateStationRegistry.findControllingCellarStation(level, owner.getBlockPos()) == null || stack.isEmpty())
            {
                return delegate.insertItem(slot, stack, simulate);
            }
            final ItemStack copy = stack.copy();
            normalizeCellarTraits(copy, getCellarTrait(level, owner.getBlockPos()));
            return delegate.insertItem(slot, copy, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return sanitizeTakenStack(delegate.extractItem(slot, amount, simulate));
        }
    }

    private record CellarInventoryModifiableWrapper(InventoryBlockEntity<?> owner, IItemHandlerModifiable delegate) implements IItemHandlerModifiable
    {
        @Override public int getSlots() { return delegate.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }
        @Override public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return delegate.isItemValid(slot, stack); }
        @Override public void setStackInSlot(int slot, ItemStack stack) { delegate.setStackInSlot(slot, stack); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            final Level level = owner.getLevel();
            if (level == null || level.isClientSide() || ClimateStationRegistry.findControllingCellarStation(level, owner.getBlockPos()) == null || stack.isEmpty())
            {
                return delegate.insertItem(slot, stack, simulate);
            }
            final ItemStack copy = stack.copy();
            normalizeCellarTraits(copy, getCellarTrait(level, owner.getBlockPos()));
            return delegate.insertItem(slot, copy, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return sanitizeTakenStack(delegate.extractItem(slot, amount, simulate));
        }
    }
}
