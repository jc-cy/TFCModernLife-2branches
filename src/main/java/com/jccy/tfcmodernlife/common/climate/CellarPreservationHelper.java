package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.blockentities.FoodShelfBlockEntity;
import com.eerussianguy.firmalife.common.items.FLFoodTraits;
import com.jccy.tfcmodernlife.common.ModFoodTraits;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.VesselLike;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public final class CellarPreservationHelper
{
    private static final float MAX_TOTAL_PRESERVATION_MULTIPLIER = 10f;
    private static final Set<Object> SYNCING = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<FoodTrait> MANAGED_CELLAR_TRAITS = createManagedCellarTraits();

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
        final BlockEntity target = level.getBlockEntity(pos);
        if (!canSyncBlockEntity(target))
        {
            return;
        }
        syncBlockEntity(target, getContextTrait(level, pos));
    }

    public static void syncInventoryBlockEntity(InventoryBlockEntity<?> inventory)
    {
        final Level level = inventory.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        final boolean whitelisted = shouldHandleInventory(inventory);
        final @Nullable FoodTrait trait = whitelisted ? getContextTrait(level, inventory.getBlockPos()) : null;
        logTemporaryLargeVesselEntry(inventory, whitelisted, trait);
        logCellarContainerDecision(inventory, whitelisted, trait);
        if (whitelisted)
        {
            syncInventoryBlockEntity(inventory, trait);
        }
    }

    public static void syncTFCChestBlockEntity(TFCChestBlockEntity chest)
    {
        final Level level = chest.getLevel();
        if (level == null || level.isClientSide() || !isWhitelistedContainerBlock(chest))
        {
            return;
        }
        syncContainer(chest, chest, getContextTrait(level, chest.getBlockPos()));
    }

    public static void syncFoodShelfBlockEntity(FoodShelfBlockEntity shelf)
    {
        final Level level = shelf.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        syncFoodShelfBlockEntity(shelf, getContextTrait(level, shelf.getBlockPos()));
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
        if (level == null || level.isClientSide() || !shouldHandleExternalContainer(blockEntity))
        {
            return;
        }
        syncContainer(blockEntity, container, getContextTrait(level, blockEntity.getBlockPos()));
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
        sanitizeStack(stack);
        return stack;
    }

    public static boolean sanitizeStack(ItemStack stack)
    {
        return removeTemporaryCellarTraits(stack);
    }

    public static void sanitizeCellarContainerSlot(Slot slot)
    {
        sanitizeCellarContainerSlot(slot.container, slot.getContainerSlot(), slot.getItem());
    }

    private static void sanitizeCellarContainerSlot(Container container, int containerSlot, ItemStack stack)
    {
        if (container instanceof CellarCompoundContainerAccess compound)
        {
            final Container first = compound.tfcml$getContainer1();
            final Container selected = containerSlot < first.getContainerSize() ? first : compound.tfcml$getContainer2();
            sanitizeCellarContainerStack(selected, stack);
            return;
        }
        sanitizeCellarContainerStack(container, stack);
    }

    private static void sanitizeCellarContainerStack(Container container, ItemStack stack)
    {
        if (!(container instanceof BlockEntity blockEntity))
        {
            return;
        }
        if (isActiveCellarContainer(blockEntity))
        {
            sanitizeStack(stack);
        }
    }

    public static void sanitizeInventoryForDrop(InventoryBlockEntity<?> inventory)
    {
        if (!isActiveCellarContainer(inventory))
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
            changed |= removeTemporaryCellarTraits(internal.getStackInSlot(slot));
        }
        if (changed)
        {
            inventory.setChanged();
        }
    }

    public static void sanitizeContainerBlockEntityForDrop(BlockEntity blockEntity, Container container)
    {
        if (!isActiveCellarContainer(blockEntity))
        {
            return;
        }
        sanitizeContainerForDrop(blockEntity, container);
    }

    public static IItemHandler wrapSidedInventory(InventoryBlockEntity<?> inventory, @Nullable IItemHandler handler)
    {
        return wrapBlockEntityItemHandler(inventory, handler);
    }

    public static IItemHandler wrapBlockEntityItemHandler(BlockEntity owner, @Nullable IItemHandler handler)
    {
        if (handler == null || !shouldHandleItemHandler(owner, handler) || isCellarWrapped(handler))
        {
            return handler;
        }
        return handler instanceof IItemHandlerModifiable modifiable
            ? new CellarInventoryModifiableWrapper(owner, modifiable)
            : new CellarInventoryWrapper(owner, handler);
    }

    public static boolean shouldHandleItemHandler(BlockEntity owner, @Nullable IItemHandler handler)
    {
        return handler != null && canWrapBlockEntityItemHandler(owner);
    }

    public static boolean canWrapBlockEntityItemHandler(BlockEntity owner)
    {
        return !(owner instanceof com.eerussianguy.firmalife.common.blockentities.ClimateReceiver)
            && !(owner instanceof ClimateStationAccess)
            && shouldHandleExternalContainer(owner);
    }

    public static float getAppliedCellarPreservationMultiplier(ItemStack stack)
    {
        for (FoodTrait trait : ModFoodTraits.getCellarTraits())
        {
            if (FoodCapability.hasTrait(stack, trait))
            {
                return ModFoodTraits.getCellarTraitMultiplier(trait);
            }
        }
        return 0f;
    }

    public static boolean isInCellar(Level level, BlockPos pos)
    {
        return !level.isClientSide() && ClimateStationRegistry.findControllingCellarStation(level, pos) != null;
    }

    public static FoodTrait getCellarTrait(Level level, BlockPos pos)
    {
        final ClimateStationAccess structure = ClimateStationRegistry.findControllingCellarStation(level, pos);
        final float baseTemperature = GreenhouseTemperatureHelper.getAmbientTemperature(level, pos);
        final ClimateStationAccess refrigeratorControl = structure != null
            ? ClimateStationRegistry.findRunningCellarRefrigerator(level, pos)
            : null;
        final float temperature;
        if (refrigeratorControl instanceof RefrigeratorBlockEntity refrigerator)
        {
            final float minimumTarget = Math.min(
                baseTemperature,
                GreenhouseTemperatureHelper.getCellarMinimumTemperature(refrigeratorControl)
            );
            temperature = Math.min(baseTemperature, Math.max(refrigerator.getTarget(), minimumTarget));
        }
        else if (structure != null)
        {
            temperature = structure.tfcml$getEffectiveCellarTemperature(baseTemperature);
        }
        else
        {
            temperature = baseTemperature;
        }
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
        return !(inventory instanceof com.eerussianguy.firmalife.common.blockentities.ClimateReceiver)
            && !(inventory instanceof ClimateStationAccess)
            && isWhitelistedContainerBlock(inventory);
    }

    private static @Nullable FoodTrait getContextTrait(Level level, BlockPos pos)
    {
        return ClimateStationRegistry.findControllingCellarStation(level, pos) != null ? getCellarTrait(level, pos) : null;
    }

    private static void syncBlockEntity(Level level, BlockPos pos, @Nullable FoodTrait trait)
    {
        syncBlockEntity(level.getBlockEntity(pos), trait);
    }

    private static void syncBlockEntity(@Nullable BlockEntity target, @Nullable FoodTrait trait)
    {
        if (target instanceof FoodShelfBlockEntity shelf)
        {
            syncFoodShelfBlockEntity(shelf, trait);
        }
        else if (target instanceof InventoryBlockEntity<?> inventory && shouldHandleInventory(inventory))
        {
            syncInventoryBlockEntity(inventory, trait);
        }
        else if (target instanceof TFCChestBlockEntity chest && isWhitelistedContainerBlock(chest))
        {
            syncContainer(chest, chest, trait);
        }
        else if (target instanceof Container container && shouldHandleExternalContainer(target))
        {
            syncContainer(target, container, trait);
        }
    }

    private static boolean canSyncBlockEntity(@Nullable BlockEntity target)
    {
        return target instanceof FoodShelfBlockEntity
            || (target instanceof InventoryBlockEntity<?> inventory && shouldHandleInventory(inventory))
            || (target instanceof TFCChestBlockEntity chest && isWhitelistedContainerBlock(chest))
            || (target instanceof Container && shouldHandleExternalContainer(target));
    }

    private static void syncInventoryBlockEntity(InventoryBlockEntity<?> inventory, @Nullable FoodTrait trait)
    {
        if (!SYNCING.add(inventory))
        {
            logTemporaryLargeVesselSkipped(inventory, "already-syncing");
            return;
        }
        boolean changed = false;
        try
        {
            final IItemHandlerModifiable internal = getInventoryHandler(inventory);
            if (internal == null)
            {
                logTemporaryLargeVesselSkipped(inventory, "missing-item-handler");
                return;
            }
            for (int slot = 0; slot < internal.getSlots(); slot++)
            {
                final ItemStack stack = internal.getStackInSlot(slot);
                final boolean slotChanged = normalizeStackAndNestedContainers(stack, trait, 0);
                if (slotChanged)
                {
                    logTemporaryLargeVesselSlot(inventory, slot, stack, trait);
                    changed = true;
                }
            }
        }
        finally
        {
            SYNCING.remove(inventory);
        }
        logTemporaryLargeVesselResult(inventory, changed);
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
                changed |= normalizeStackAndNestedContainers(internal.getStackInSlot(slot), trait, 0);
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
                changed |= normalizeStackAndNestedContainers(container.getItem(slot), trait, 0);
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

    private static boolean normalizeStackAndNestedContainers(ItemStack stack, @Nullable FoodTrait trait, int depth)
    {
        if (stack.isEmpty())
        {
            return false;
        }
        boolean changed = normalizeFoodStack(stack, trait);
        if (depth < 2 && ClimateControlConfig.isCellarNestedItemContainer(stack))
        {
            changed |= normalizeSmallVesselContents(stack, trait, depth + 1);
        }
        return changed;
    }

    private static boolean normalizeFoodStack(ItemStack stack, @Nullable FoodTrait trait)
    {
        if (FoodCapability.get(stack) == null)
        {
            return false;
        }
        final @Nullable FoodTrait effectiveTrait = getEffectiveCellarTrait(stack, trait);
        boolean changed = false;
        for (FoodTrait possible : MANAGED_CELLAR_TRAITS)
        {
            if (effectiveTrait != possible && FoodCapability.hasTrait(stack, possible))
            {
                FoodCapability.removeTrait(stack, possible);
                changed = true;
            }
        }
        if (effectiveTrait != null && !FoodCapability.hasTrait(stack, effectiveTrait))
        {
            FoodCapability.applyTrait(stack, effectiveTrait);
            changed = true;
        }
        return changed;
    }

    private static @Nullable FoodTrait getEffectiveCellarTrait(ItemStack stack, @Nullable FoodTrait trait)
    {
        if (trait == null)
        {
            return null;
        }
        final float existingMultiplier = getExistingContainerPreservationMultiplier(stack);
        if (!Float.isFinite(existingMultiplier) || existingMultiplier >= MAX_TOTAL_PRESERVATION_MULTIPLIER)
        {
            return null;
        }
        final float allowedCellarMultiplier = MAX_TOTAL_PRESERVATION_MULTIPLIER / existingMultiplier;
        final float desiredMultiplier = Math.min(ModFoodTraits.getCellarTraitMultiplier(trait), allowedCellarMultiplier);
        return ModFoodTraits.getCellarTraitAtMost(desiredMultiplier);
    }

    private static float getExistingContainerPreservationMultiplier(ItemStack stack)
    {
        return FoodCapability.hasTrait(stack, FoodTraits.PRESERVED) ? getTraitMultiplier(FoodTraits.PRESERVED) : 1f;
    }

    private static float getTraitMultiplier(FoodTrait trait)
    {
        final float decayModifier = trait.getDecayModifier();
        return decayModifier <= 0f ? Float.POSITIVE_INFINITY : 1f / decayModifier;
    }

    private static boolean normalizeSmallVesselContents(ItemStack stack, @Nullable FoodTrait trait, int depth)
    {
        final VesselLike vessel = VesselLike.get(stack);
        if (vessel == null || vessel.mode() != VesselLike.Mode.INVENTORY)
        {
            return false;
        }
        boolean changed = false;
        for (int slot = 0; slot < vessel.getSlots(); slot++)
        {
            final ItemStack contained = vessel.getStackInSlot(slot);
            if (contained.isEmpty())
            {
                continue;
            }
            final boolean slotChanged = normalizeStackAndNestedContainers(contained, trait, depth);
            if (slotChanged)
            {
                vessel.setStackInSlot(slot, contained);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean removeTemporaryCellarTraits(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }
        boolean changed = removeFoodTemporaryCellarTraits(stack);
        if (ClimateControlConfig.isCellarNestedItemContainer(stack))
        {
            changed |= removeSmallVesselTemporaryCellarTraits(stack, 1);
        }
        return changed;
    }

    private static boolean removeFoodTemporaryCellarTraits(ItemStack stack)
    {
        if (FoodCapability.get(stack) == null)
        {
            return false;
        }
        boolean changed = false;
        for (FoodTrait trait : MANAGED_CELLAR_TRAITS)
        {
            if (FoodCapability.hasTrait(stack, trait))
            {
                FoodCapability.removeTrait(stack, trait);
                changed = true;
            }
        }
        return changed;
    }

    private static boolean removeSmallVesselTemporaryCellarTraits(ItemStack stack, int depth)
    {
        if (depth > 2)
        {
            return false;
        }
        final VesselLike vessel = VesselLike.get(stack);
        if (vessel == null || vessel.mode() != VesselLike.Mode.INVENTORY)
        {
            return false;
        }
        boolean changed = false;
        for (int slot = 0; slot < vessel.getSlots(); slot++)
        {
            final ItemStack contained = vessel.getStackInSlot(slot);
            if (!contained.isEmpty() && removeTemporaryCellarTraits(contained))
            {
                vessel.setStackInSlot(slot, contained);
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
                changed |= removeTemporaryCellarTraits(container.getItem(slot));
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
        return isWhitelistedContainerBlock(blockEntity);
    }

    private static boolean isActiveCellarContainer(BlockEntity blockEntity)
    {
        final Level level = blockEntity.getLevel();
        return level != null
            && !level.isClientSide()
            && shouldHandleExternalContainer(blockEntity)
            && getContextTrait(level, blockEntity.getBlockPos()) != null;
    }

    private static boolean shouldApplyContainerInputTraits(BlockEntity owner)
    {
        final Level level = owner.getLevel();
        return level != null && !level.isClientSide() && shouldHandleExternalContainer(owner);
    }

    private static boolean isWhitelistedContainerBlock(BlockEntity blockEntity)
    {
        return ClimateControlConfig.isCellarPreservableContainer(blockEntity.getBlockState());
    }

    private static void logTemporaryLargeVesselEntry(BlockEntity blockEntity, boolean whitelisted, @Nullable FoodTrait trait)
    {
        if (!ClimateDebug.isCellarEnabled() || !isLikelyLargeVessel(blockEntity))
        {
            return;
        }
        final String blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).toString();
        ClimateDebug.cellarTemp(
            "entry block={} state={} class={} pos={} whitelisted={} tagFired={} inCellar={} traitMultiplier={}",
            blockId,
            blockEntity.getBlockState(),
            blockEntity.getClass().getName(),
            ClimateDebug.pos(blockEntity.getBlockPos()),
            whitelisted,
            blockEntity.getBlockState().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, new net.minecraft.resources.ResourceLocation("tfc", "fired_large_vessels"))),
            trait != null,
            trait != null ? getTraitMultiplier(trait) : 0f
        );
    }

    private static void logTemporaryLargeVesselSkipped(BlockEntity blockEntity, String reason)
    {
        if (ClimateDebug.isCellarEnabled() && isLikelyLargeVessel(blockEntity))
        {
            ClimateDebug.cellarTemp("skip pos={} reason={}", ClimateDebug.pos(blockEntity.getBlockPos()), reason);
        }
    }

    private static void logTemporaryLargeVesselSlot(BlockEntity blockEntity, int slot, ItemStack stack, @Nullable FoodTrait trait)
    {
        if (ClimateDebug.isCellarEnabled() && isLikelyLargeVessel(blockEntity))
        {
            ClimateDebug.cellarTemp(
                "slot pos={} slot={} item={} cellarTraitMultiplier={} preservedTrait={} appliedCellarMultiplier={}",
                ClimateDebug.pos(blockEntity.getBlockPos()),
                slot,
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                trait != null ? getTraitMultiplier(trait) : 0f,
                FoodCapability.hasTrait(stack, FoodTraits.PRESERVED),
                getAppliedCellarPreservationMultiplier(stack)
            );
        }
    }

    private static void logTemporaryLargeVesselResult(BlockEntity blockEntity, boolean changed)
    {
        if (ClimateDebug.isCellarEnabled() && isLikelyLargeVessel(blockEntity))
        {
            ClimateDebug.cellarTemp("result pos={} changed={}", ClimateDebug.pos(blockEntity.getBlockPos()), changed);
        }
    }

    private static void logCellarContainerDecision(BlockEntity blockEntity, boolean whitelisted, @Nullable FoodTrait trait)
    {
        if (!ClimateDebug.isCellarEnabled() || !isLikelyLargeVessel(blockEntity))
        {
            return;
        }
        final String blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).toString();
        ClimateDebug.cellarInfo(
            "container={} class={} pos={} whitelisted={} inCellar={} traitMultiplier={}",
            blockId,
            blockEntity.getClass().getName(),
            ClimateDebug.pos(blockEntity.getBlockPos()),
            whitelisted,
            trait != null,
            trait != null ? getTraitMultiplier(trait) : 0f
        );
    }

    private static boolean isLikelyLargeVessel(BlockEntity blockEntity)
    {
        final String blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).toString();
        return blockId.contains("large_vessel") || blockEntity.getClass().getName().contains("LargeVessel");
    }

    private static @Nullable IItemHandlerModifiable getInventoryHandler(InventoryBlockEntity<?> inventory)
    {
        return inventory.getCapability(Capabilities.ITEM).resolve()
            .filter(IItemHandlerModifiable.class::isInstance)
            .map(IItemHandlerModifiable.class::cast)
            .orElse(null);
    }

    private static Set<FoodTrait> createManagedCellarTraits()
    {
        final Set<FoodTrait> traits = new LinkedHashSet<>();
        traits.add(FLFoodTraits.SHELVED);
        traits.add(FLFoodTraits.SHELVED_2);
        traits.add(FLFoodTraits.SHELVED_3);
        traits.add(FLFoodTraits.HUNG);
        traits.add(FLFoodTraits.HUNG_2);
        traits.add(FLFoodTraits.HUNG_3);
        traits.addAll(ModFoodTraits.getCellarTraits());
        return Collections.unmodifiableSet(traits);
    }

    private static boolean isCellarWrapped(IItemHandler handler)
    {
        return handler instanceof CellarInventoryWrapper || handler instanceof CellarInventoryModifiableWrapper;
    }

    private record CellarInventoryWrapper(BlockEntity owner, IItemHandler delegate) implements IItemHandler
    {
        @Override public int getSlots() { return delegate.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }
        @Override public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return delegate.isItemValid(slot, stack); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty() || simulate || !shouldApplyContainerInputTraits(owner))
            {
                return delegate.insertItem(slot, stack, simulate);
            }
            final ItemStack copy = stack.copy();
            normalizeStackAndNestedContainers(copy, getTraitForOwner(owner), 0);
            return delegate.insertItem(slot, copy, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            final ItemStack extracted = delegate.extractItem(slot, amount, simulate);
            return simulate ? extracted : sanitizeTakenStack(extracted);
        }
    }

    private record CellarInventoryModifiableWrapper(BlockEntity owner, IItemHandlerModifiable delegate) implements IItemHandlerModifiable
    {
        @Override public int getSlots() { return delegate.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }
        @Override public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return delegate.isItemValid(slot, stack); }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            if (stack.isEmpty() || !shouldApplyContainerInputTraits(owner))
            {
                delegate.setStackInSlot(slot, stack);
                return;
            }
            final ItemStack copy = stack.copy();
            normalizeStackAndNestedContainers(copy, getTraitForOwner(owner), 0);
            delegate.setStackInSlot(slot, copy);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty() || simulate || !shouldApplyContainerInputTraits(owner))
            {
                return delegate.insertItem(slot, stack, simulate);
            }
            final ItemStack copy = stack.copy();
            normalizeStackAndNestedContainers(copy, getTraitForOwner(owner), 0);
            return delegate.insertItem(slot, copy, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            final ItemStack extracted = delegate.extractItem(slot, amount, simulate);
            return simulate ? extracted : sanitizeTakenStack(extracted);
        }
    }

    private static @Nullable FoodTrait getTraitForOwner(BlockEntity owner)
    {
        final Level level = owner.getLevel();
        if (level == null || level.isClientSide() || !shouldHandleExternalContainer(owner))
        {
            return null;
        }
        return getContextTrait(level, owner.getBlockPos());
    }
}
