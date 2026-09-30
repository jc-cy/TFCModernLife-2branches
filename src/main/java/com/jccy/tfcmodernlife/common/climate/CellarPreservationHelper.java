package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;
import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.eerussianguy.firmalife.common.blockentities.FoodShelfBlockEntity;
import com.eerussianguy.firmalife.common.items.FLFoodTraits;
import com.jccy.tfcmodernlife.common.ModFoodTraits;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.Container;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.food.FoodTraits;
import net.dries007.tfc.common.component.mold.Vessel;
import net.dries007.tfc.util.climate.Climate;

public final class CellarPreservationHelper
{
    private static final float MAX_TOTAL_PRESERVATION_MULTIPLIER = 10f;

    private static final Set<Holder<FoodTrait>> CELLAR_TRAITS = createManagedCellarTraits();
    private static final Set<Object> SYNCING = Collections.newSetFromMap(new IdentityHashMap<>());

    private static Set<Holder<FoodTrait>> createManagedCellarTraits()
    {
        final Set<Holder<FoodTrait>> traits = new LinkedHashSet<>();
        traits.add(FLFoodTraits.SHELVED);
        traits.add(FLFoodTraits.SHELVED_2);
        traits.add(FLFoodTraits.SHELVED_3);
        traits.add(FLFoodTraits.HUNG);
        traits.add(FLFoodTraits.HUNG_2);
        traits.add(FLFoodTraits.HUNG_3);
        traits.addAll(ModFoodTraits.getCellarTraits());
        return Collections.unmodifiableSet(traits);
    }


    private CellarPreservationHelper() {}

    public static Set<DeferredHolder<FoodTrait, FoodTrait>> getPossibleTraits()
    {
        return new java.util.LinkedHashSet<>(ModFoodTraits.getCellarTraits());
    }

    public static void syncTrackedInventories(ClimateStationAccess station)
    {
        final boolean active = station instanceof BlockEntity blockEntity && ClimateStationRegistry.isActiveStation(blockEntity, station);
        syncTrackedInventories(station, active);
    }

    public static void syncTrackedInventories(ClimateStationAccess station, boolean active)
    {
        if (!(station instanceof BlockEntity blockEntity))
        {
            return;
        }
        final Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide() || station.tfcml$getClimateType() != ClimateType.CELLAR)
        {
            return;
        }

        final @Nullable Holder<FoodTrait> trait = active ? getCellarTrait(level, blockEntity.getBlockPos()) : null;
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

        final Set<BlockPos> uniquePositions = new HashSet<>();
        for (BlockPos pos : positions)
        {
            if (uniquePositions.add(pos))
            {
                syncBlockEntity(level, pos);
            }
        }
    }

    public static void syncBlockEntity(Level level, BlockPos pos)
    {
        if (level.isClientSide())
        {
            return;
        }

        final BlockEntity target = level.getBlockEntity(pos);
        if (target instanceof FoodShelfBlockEntity shelf)
        {
            syncFoodShelfBlockEntity(shelf, true);
        }
        else if (target instanceof InventoryBlockEntity<?> inventory && shouldHandleInventory(inventory))
        {
            syncInventoryBlockEntity(inventory);
        }
        else if (target instanceof ChestBlockEntity chest)
        {
            syncChestBlockEntity(chest);
        }
        else if (target instanceof Container container && shouldHandleExternalContainer(target))
        {
            syncContainerBlockEntity(target, container);
        }
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
        final @Nullable Holder<FoodTrait> trait = station != null ? getCellarTrait(level, inventory.getBlockPos()) : null;
        syncInventoryBlockEntity(inventory, trait);
    }

    public static void syncFoodShelfBlockEntity(FoodShelfBlockEntity shelf, boolean preserved)
    {
        final Level level = shelf.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, shelf.getBlockPos());
        syncFoodShelfBlockEntity(shelf, preserved && station != null ? getCellarTrait(level, shelf.getBlockPos()) : null);
    }

    public static void syncChestBlockEntity(ChestBlockEntity chest)
    {
        final Level level = chest.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }

        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, chest.getBlockPos());
        final @Nullable Holder<FoodTrait> trait = station != null ? getCellarTrait(level, chest.getBlockPos()) : null;
        syncChestBlockEntity(chest, trait);
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
        final @Nullable Holder<FoodTrait> trait = station != null ? getCellarTrait(level, blockEntity.getBlockPos()) : null;
        syncContainer(blockEntity, container, trait);
    }

    public static ItemStack sanitizeTakenStack(InventoryBlockEntity<?> inventory, ItemStack stack)
    {
        if (!shouldHandleInventory(inventory) || stack.isEmpty())
        {
            return stack;
        }
        sanitizeTakenStack(stack);
        return stack;
    }

    public static ItemStack sanitizeTakenStack(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return stack;
        }
        removeCellarTraits(stack);
        return stack;
    }

    public static void sanitizeInventoryForDrop(InventoryBlockEntity<?> inventory)
    {
        if (!shouldHandleInventory(inventory))
        {
            return;
        }

        boolean changed = false;
        final var internal = inventory.getInventory();
        for (int slot = 0; slot < internal.getSlots(); slot++)
        {
            final ItemStack stack = internal.getStackInSlot(slot);
            if (stack.isEmpty())
            {
                continue;
            }
            boolean hadTrait = false;
            for (Holder<FoodTrait> trait : CELLAR_TRAITS)
            {
                hadTrait |= FoodCapability.hasTrait(stack, trait);
            }
            removeCellarTraits(stack);
            changed |= hadTrait;
        }

        if (changed)
        {
            inventory.setChanged();
        }
    }

    public static void sanitizeChestForDrop(ChestBlockEntity chest)
    {
        sanitizeContainerForDrop(chest, chest);
    }

    public static void sanitizeContainerBlockEntityForDrop(BlockEntity blockEntity, Container container)
    {
        sanitizeContainerForDrop(blockEntity, container);
    }

    public static IItemHandler wrapSidedInventory(InventoryBlockEntity<?> inventory, @Nullable IItemHandler handler)
    {
        return wrapBlockEntityItemHandler(inventory, handler);
    }

    public static IItemHandler wrapBlockEntityItemHandler(BlockEntity owner, @Nullable IItemHandler handler)
    {
        if (handler == null || !canWrapBlockEntityItemHandler(owner))
        {
            return handler;
        }
        if (handler instanceof CellarBlockEntityWrapper || handler instanceof CellarBlockEntityModifiableWrapper)
        {
            return handler;
        }
        return handler instanceof IItemHandlerModifiable modifiable
            ? new CellarBlockEntityModifiableWrapper(owner, modifiable)
            : new CellarBlockEntityWrapper(owner, handler);
    }

    public static boolean canWrapBlockEntityItemHandler(BlockEntity owner)
    {
        return !(owner instanceof ClimateReceiver)
            && !(owner instanceof ClimateStationAccess)
            && shouldHandleExternalContainer(owner);
    }

    public static Holder<FoodTrait> getCellarTrait(Level level, BlockPos pos)
    {
        final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, pos);
        final float baseTemperature = GreenhouseTemperatureHelper.getAmbientTemperature(level, pos);
        // A running refrigerator is the controlling station here (see ClimateStationRegistry#stationPriority),
        // so its absolute setpoint is already folded into the effective cellar temperature. Never warm the
        // reading back up to the tier minimum when the ambient temperature is already colder than it.
        final float temperature = station == null
            ? baseTemperature
            : station.tfcml$getEffectiveCellarTemperature(baseTemperature);
        return ModFoodTraits.getCellarTraitForMultiplier(GreenhouseTemperatureHelper.getCellarPreservationMultiplier(temperature));
    }

    public static float getCellarDecayModifier(Level level, BlockPos pos)
    {
        return getCellarTrait(level, pos).value().getDecayModifier();
    }

    public static float getCellarPreservationMultiplier(Level level, BlockPos pos)
    {
        final float decayModifier = getCellarDecayModifier(level, pos);
        return decayModifier <= 0f ? Float.POSITIVE_INFINITY : 1f / decayModifier;
    }

    public static int getCellarDecayPercent(Level level, BlockPos pos)
    {
        final float decayModifier = getCellarDecayModifier(level, pos);
        return decayModifier <= 0f ? 0 : Math.round(decayModifier * 100f);
    }

    public static boolean shouldHandleInventory(InventoryBlockEntity<?> inventory)
    {
        return !(inventory instanceof ClimateReceiver)
            && !(inventory instanceof ClimateStationAccess)
            && shouldHandleExternalContainer(inventory);
    }

    private static boolean shouldHandleExternalContainer(BlockEntity blockEntity)
    {
        return ClimateControlConfig.isCellarPreservableContainer(blockEntity.getBlockState());
    }

    public static boolean isActiveCellarContainer(BlockEntity blockEntity)
    {
        final Level level = blockEntity.getLevel();
        return level != null
            && !level.isClientSide()
            && shouldHandleExternalContainer(blockEntity)
            && ClimateStationRegistry.findControllingCellarStation(level, blockEntity.getBlockPos()) != null;
    }

    public static boolean isActiveCellarContainer(@Nullable Container container)
    {
        if (container instanceof CellarCompoundContainerAccess compound)
        {
            return isActiveCellarContainer(compound.tfcml$getContainer1())
                || isActiveCellarContainer(compound.tfcml$getContainer2());
        }
        return container instanceof BlockEntity blockEntity && isActiveCellarContainer(blockEntity);
    }

    /**
     * Applies the current cellar preservation trait to a stack that automation (hoppers, pipes, ...) is about to
     * insert into a whitelisted container inside a working cellar.
     */
    public static ItemStack tagInsertedStack(BlockEntity owner, ItemStack stack)
    {
        if (stack.isEmpty() || !isActiveCellarContainer(owner))
        {
            return stack;
        }
        final Level level = owner.getLevel();
        if (level == null || level.isClientSide())
        {
            return stack;
        }
        normalizeCellarTraits(stack, getCellarTrait(level, owner.getBlockPos()));
        return stack;
    }

    /**
     * Removes the temporary cellar preservation traits from a stack that automation is about to pull out of a
     * whitelisted container inside a working cellar, so the multiplier cannot leave the cellar.
     */
    public static ItemStack sanitizeExtractedStack(BlockEntity owner, ItemStack stack)
    {
        if (stack.isEmpty() || !isActiveCellarContainer(owner))
        {
            return stack;
        }
        removeCellarTraits(stack);
        return stack;
    }

    private static void syncInventoryBlockEntity(InventoryBlockEntity<?> inventory, @Nullable Holder<FoodTrait> trait)
    {
        if (!SYNCING.add(inventory))
        {
            return;
        }

        boolean changed = false;
        try
        {
            final var internal = inventory.getInventory();
            for (int slot = 0; slot < internal.getSlots(); slot++)
            {
                final ItemStack stack = internal.getStackInSlot(slot);
                if (stack.isEmpty())
                {
                    continue;
                }
                changed |= normalizeCellarTraits(stack, trait);
            }
        }
        finally
        {
            SYNCING.remove(inventory);
        }

        if (changed)
        {
            markChanged(inventory);
        }
    }

    private static void syncChestBlockEntity(ChestBlockEntity chest, @Nullable Holder<FoodTrait> trait)
    {
        // 与 1.20 的 isWhitelistedContainerBlock 判断对齐: 只有 cellarPreservableContainers 里登记的箱子才吃地窖保鲜。
        // 这条判断放在这里能同时覆盖 "气象站推送"、"开箱 / 槽位变化" 和 "双联箱另一半" 三条入口，不必在调用点重复写。
        if (!shouldHandleExternalContainer(chest))
        {
            return;
        }
        syncContainer(chest, chest, trait);
        final var state = chest.getBlockState();
        if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE)
        {
            final BlockEntity partner = chest.getLevel().getBlockEntity(chest.getBlockPos().relative(ChestBlock.getConnectedDirection(state)));
            if (partner instanceof ChestBlockEntity other)
            {
                syncContainer(other, other, trait);
            }
        }
    }

    private static void syncFoodShelfBlockEntity(FoodShelfBlockEntity shelf, @Nullable Holder<FoodTrait> trait)
    {
        if (!SYNCING.add(shelf))
        {
            return;
        }

        boolean changed = false;
        try
        {
            final var internal = shelf.getInventory();
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
            shelf.markForSync();
        }
    }

    private static void syncBlockEntity(Level level, BlockPos pos, @Nullable Holder<FoodTrait> trait)
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
        else if (target instanceof ChestBlockEntity chest)
        {
            syncChestBlockEntity(chest, trait);
        }
        else if (target instanceof Container container && shouldHandleExternalContainer(target))
        {
            syncContainer(target, container, trait);
        }
    }

    private static void syncContainer(Object owner, Container container, @Nullable Holder<FoodTrait> trait)
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
                final ItemStack stack = container.getItem(slot);
                if (stack.isEmpty())
                {
                    continue;
                }
                changed |= normalizeCellarTraits(stack, trait);
            }
        }
        finally
        {
            SYNCING.remove(owner);
        }

        if (changed)
        {
            markChanged(owner);
        }
    }

    /**
     * 内容改写后通知客户端：TFC 方块实体走 markForSync（内部会 setChanged），原版箱子这类只有 setChanged。
     */
    private static void markChanged(Object owner)
    {
        if (owner instanceof TFCBlockEntity tfc)
        {
            tfc.markForSync();
        }
        else if (owner instanceof BlockEntity blockEntity)
        {
            blockEntity.setChanged();
        }
    }

    private static boolean normalizeCellarTraits(ItemStack stack, @Nullable Holder<FoodTrait> trait)
    {
        return normalizeStackAndNestedContainers(stack, trait, 0);
    }

    private static boolean normalizeStackAndNestedContainers(ItemStack stack, @Nullable Holder<FoodTrait> trait, int depth)
    {
        if (stack.isEmpty())
        {
            return false;
        }

        boolean changed = normalizeFoodStack(stack, trait);
        if (depth < 2 && ClimateControlConfig.isCellarNestedItemContainer(stack))
        {
            changed |= normalizeNestedContainerContents(stack, trait, depth + 1);
        }
        return changed;
    }

    private static boolean normalizeFoodStack(ItemStack stack, @Nullable Holder<FoodTrait> trait)
    {
        if (FoodCapability.get(stack) == null)
        {
            return false;
        }

        final @Nullable Holder<FoodTrait> effectiveTrait = getEffectiveCellarTrait(stack, trait);
        boolean changed = false;
        for (Holder<FoodTrait> possible : CELLAR_TRAITS)
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

    private static boolean normalizeNestedContainerContents(ItemStack stack, @Nullable Holder<FoodTrait> trait, int depth)
    {
        final @Nullable Vessel vessel = Vessel.get(stack);
        if (vessel == null || !vessel.isInventory())
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
            if (normalizeStackAndNestedContainers(contained, trait, depth))
            {
                vessel.setStackInSlot(slot, contained);
                changed = true;
            }
        }
        return changed;
    }

    @Nullable
    private static Holder<FoodTrait> getEffectiveCellarTrait(ItemStack stack, @Nullable Holder<FoodTrait> trait)
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
        final float desiredMultiplier = Math.min(ModFoodTraits.getCellarTraitMultiplier(trait.value()), allowedCellarMultiplier);
        return ModFoodTraits.getCellarTraitAtMost(desiredMultiplier);
    }

    private static float getExistingContainerPreservationMultiplier(ItemStack stack)
    {
        return FoodCapability.hasTrait(stack, FoodTraits.PRESERVED) ? getTraitMultiplier(FoodTraits.PRESERVED.value()) : 1f;
    }

    private static float getTraitMultiplier(FoodTrait trait)
    {
        final float decayModifier = trait.getDecayModifier();
        return decayModifier <= 0f ? Float.POSITIVE_INFINITY : 1f / decayModifier;
    }

    private static void removeCellarTraits(ItemStack stack)
    {
        removeCellarTraits(stack, 0);
    }

    private static void removeCellarTraits(ItemStack stack, int depth)
    {
        if (stack.isEmpty())
        {
            return;
        }
        for (Holder<FoodTrait> trait : CELLAR_TRAITS)
        {
            FoodCapability.removeTrait(stack, trait);
        }
        if (depth < 2 && ClimateControlConfig.isCellarNestedItemContainer(stack))
        {
            final @Nullable Vessel vessel = Vessel.get(stack);
            if (vessel != null && vessel.isInventory())
            {
                for (int slot = 0; slot < vessel.getSlots(); slot++)
                {
                    final ItemStack contained = vessel.getStackInSlot(slot);
                    if (contained.isEmpty())
                    {
                        continue;
                    }
                    removeCellarTraits(contained, depth + 1);
                    vessel.setStackInSlot(slot, contained);
                }
            }
        }
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
                final ItemStack stack = container.getItem(slot);
                if (stack.isEmpty())
                {
                    continue;
                }
                final boolean hadTrait = hasCellarTrait(stack);
                removeCellarTraits(stack);
                changed |= hadTrait;
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

    private static boolean hasCellarTrait(ItemStack stack)
    {
        for (Holder<FoodTrait> trait : CELLAR_TRAITS)
        {
            if (FoodCapability.hasTrait(stack, trait))
            {
                return true;
            }
        }
        return false;
    }

    private record CellarBlockEntityWrapper(BlockEntity owner, IItemHandler delegate) implements IItemHandler
    {
        @Override
        public int getSlots()
        {
            return delegate.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            final Level level = owner.getLevel();
            if (level == null || level.isClientSide())
            {
                return delegate.insertItem(slot, stack, simulate);
            }

            final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, owner.getBlockPos());
            if (station == null || stack.isEmpty())
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
            final ItemStack extracted = delegate.extractItem(slot, amount, simulate);
            if (!extracted.isEmpty())
            {
                sanitizeTakenStack(extracted);
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
            return delegate.isItemValid(slot, stack);
        }
    }

    private record CellarBlockEntityModifiableWrapper(BlockEntity owner, IItemHandlerModifiable delegate) implements IItemHandlerModifiable
    {
        @Override
        public int getSlots()
        {
            return delegate.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            final Level level = owner.getLevel();
            if (level == null || level.isClientSide())
            {
                return delegate.insertItem(slot, stack, simulate);
            }

            final ClimateStationAccess station = ClimateStationRegistry.findControllingCellarStation(level, owner.getBlockPos());
            if (station == null || stack.isEmpty())
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
            final ItemStack extracted = delegate.extractItem(slot, amount, simulate);
            if (!extracted.isEmpty())
            {
                sanitizeTakenStack(extracted);
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
            return delegate.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            delegate.setStackInSlot(slot, stack);
        }
    }
}
