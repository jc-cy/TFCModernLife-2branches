package com.jccy.tfcmodernlife.common.blockentity;

import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;
import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import com.jccy.tfcmodernlife.common.climate.CellarStructureData;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import com.jccy.tfcmodernlife.common.climate.ConfiguredCellarDetector;
import com.jccy.tfcmodernlife.common.climate.GreenhouseStructureData;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import com.jccy.tfcmodernlife.common.climate.MixedGreenhouseDetector;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClimateControlBlockEntity extends BlockEntity implements MenuProvider, ClimateStationAccess
{
    public static final int ENERGY_CAPACITY = 4_000_000;
    public static final int ENERGY_MAX_INPUT = 4096;
    public static final int START_ENERGY_THRESHOLD = ENERGY_CAPACITY / 4;
    private static final int MIN_ENERGY_USE = 20;
    private static final float ENERGY_USE_SCALE = 0.001f;
    public static final int DATA_TARGET = 0;
    public static final int DATA_ENERGY = 1;
    public static final int DATA_RUNNING = 2;
    public static final int DATA_ENERGY_PER_TICK = 3;
    public static final int DATA_EFFECTIVE_SPACE = 4;
    public static final int DATA_AFTER_TEMPERATURE = 5;
    public static final int DATA_PRESERVATION_TENTHS = 6;
    public static final int DATA_MIN_TARGET = 7;
    public static final int DATA_MAX_TARGET = 8;
    public static final int DATA_BEFORE_TEMPERATURE = 9;
    public static final int DATA_STRUCTURE_TYPE = 10;
    public static final int DATA_TIER = 11;
    public static final int DATA_POWER_MULTIPLIER = 12;
    public static final int DATA_BASE_TEMPERATURE_DELTA = 13;
    public static final int DATA_CONTROL_RANGE = 14;
    public static final int DATA_BASE_TEMPERATURE = 15;
    public static final int DATA_ENABLED = 16;
    public static final int DATA_COUNT = 17;
    private static final int NETWORK_DATA_ENERGY_HIGH = DATA_COUNT;
    private static final int NETWORK_DATA_COUNT = DATA_COUNT + 1;
    public static final int STRUCTURE_NONE = 0;
    public static final int STRUCTURE_GREENHOUSE = 1;
    public static final int STRUCTURE_CELLAR = 2;
    private static final int STRUCTURE_REFRESH_INTERVAL = 100;
    private static final String LOCAL_TYPE_KEY = "localClimateType";
    private static final String LOCAL_GREENHOUSE_STRUCTURE_KEY = "localGreenhouseStructure";
    private static final String LOCAL_CELLAR_STRUCTURE_KEY = "localCellarStructure";
    private static final String LOCAL_POSITIONS_KEY = "localClimatePositions";
    private static final String LOCAL_AUTO_TEMPERATURE_KEY = "localAutoTemperature";
    private static final String LOCAL_MANUAL_TEMPERATURE_KEY = "localManualTemperature";
    private static final String LOCAL_MANUAL_TEMPERATURE_TENTHS_KEY = "localManualTemperatureTenths";
    private static final String LOCAL_CELLAR_TEMPERATURE_KEY = "localCellarTemperature";
    private static final String LOCAL_CELLAR_HEATING_KEY = "localCellarHeating";
    private static final String LOCAL_LAST_AUTO_DAY_KEY = "localLastAutoDay";
    private static final String ENABLED_KEY = "enabled";

    private final InputOnlyEnergyStorage energyStorage = new InputOnlyEnergyStorage();
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energyStorage);

    protected int target;
    protected boolean running;
    protected int energyPerTick;
    protected boolean enabled;
    @Nullable private Set<BlockPos> localClimatePositions;
    @Nullable private ClimateType localClimateType;
    @Nullable private GreenhouseStructureData localGreenhouseStructureData;
    @Nullable private CellarStructureData localCellarStructureData;
    private float localAutoTemperature = GreenhouseTemperatureHelper.DEFAULT_TEMPERATURE;
    private int localManualTemperatureAdjustmentTenths;
    private int localCellarTemperature;
    private boolean localCellarHeating;
    private long localLastAutoUpdateDay = Long.MIN_VALUE;
    private long nextStructureRefreshTick;
    private final int[] syncedData = new int[DATA_COUNT];
    private int syncedEnergyLow;
    private int syncedEnergyHigh;
    private boolean removing;

    private final ContainerData syncData = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            if (isClientSideData())
            {
                return index >= 0 && index < syncedData.length ? syncedData[index] : 0;
            }
            return switch (index) {
                case DATA_TARGET -> target;
                case DATA_ENERGY -> energyStorage.getEnergyStored();
                case DATA_RUNNING -> running ? 1 : 0;
                case DATA_ENERGY_PER_TICK -> energyPerTick;
                case DATA_EFFECTIVE_SPACE -> getDisplayEffectiveSpace();
                case DATA_AFTER_TEMPERATURE -> getDisplayIndoorTemperature();
                case DATA_PRESERVATION_TENTHS -> getDisplayPreservationTenths();
                case DATA_MIN_TARGET -> getDisplayMinimumTarget();
                case DATA_MAX_TARGET -> getDisplayMaximumTarget();
                case DATA_BEFORE_TEMPERATURE -> getDisplayBeforeTemperature();
                case DATA_STRUCTURE_TYPE -> getDisplayStructureType();
                case DATA_TIER -> getDisplayTier();
                case DATA_POWER_MULTIPLIER -> getDisplayPowerMultiplier();
                case DATA_BASE_TEMPERATURE_DELTA -> getDisplayBaseTemperatureDelta();
                case DATA_CONTROL_RANGE -> getDisplayControlRange();
                case DATA_BASE_TEMPERATURE -> getDisplayBaseTemperature();
                case DATA_ENABLED -> enabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value)
        {
            if (index >= 0 && index < syncedData.length)
            {
                syncedData[index] = value;
            }
            if (isClientSideData())
            {
                return;
            }
            switch (index)
            {
                case DATA_TARGET -> target = clampTarget(value);
                case DATA_ENERGY -> energyStorage.setEnergy(value);
                case DATA_RUNNING -> running = value != 0;
                case DATA_ENERGY_PER_TICK -> energyPerTick = Math.max(0, value);
                case DATA_EFFECTIVE_SPACE, DATA_AFTER_TEMPERATURE, DATA_PRESERVATION_TENTHS, DATA_MIN_TARGET, DATA_MAX_TARGET,
                    DATA_BEFORE_TEMPERATURE, DATA_STRUCTURE_TYPE, DATA_TIER, DATA_POWER_MULTIPLIER, DATA_BASE_TEMPERATURE_DELTA,
                    DATA_CONTROL_RANGE, DATA_BASE_TEMPERATURE -> {
                }
                case DATA_ENABLED -> enabled = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount()
        {
            return DATA_COUNT;
        }
    };

    // Vanilla menu data values travel as 16-bit shorts, so split large FE values for multiplayer clients.
    private final ContainerData networkSyncData = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            if (index == DATA_ENERGY)
            {
                return syncData.get(DATA_ENERGY) & 0xFFFF;
            }
            if (index == NETWORK_DATA_ENERGY_HIGH)
            {
                return (syncData.get(DATA_ENERGY) >>> 16) & 0xFFFF;
            }
            return syncData.get(index);
        }

        @Override
        public void set(int index, int value)
        {
            if (index == DATA_ENERGY)
            {
                syncedEnergyLow = value & 0xFFFF;
                syncedData[DATA_ENERGY] = (syncedEnergyHigh << 16) | syncedEnergyLow;
                return;
            }
            if (index == NETWORK_DATA_ENERGY_HIGH)
            {
                syncedEnergyHigh = value & 0xFFFF;
                syncedData[DATA_ENERGY] = (syncedEnergyHigh << 16) | syncedEnergyLow;
                return;
            }
            syncData.set(index, value);
        }

        @Override
        public int getCount()
        {
            return NETWORK_DATA_COUNT;
        }
    };

    private boolean isClientSideData()
    {
        return level != null && level.isClientSide();
    }

    protected ClimateControlBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void serverTick()
    {
        refreshStructure(false);
        final int previousEnergyUse = energyPerTick;
        final boolean wasRunning = running;

        energyPerTick = enabled ? calculateEnergyUse() : 0;
        running = energyPerTick > 0 && hasEnoughEnergyToRun(energyPerTick);
        if (running)
        {
            energyStorage.consumeEnergy(energyPerTick);
            applyClimateControl(true);
            setChanged();
        }
        else
        {
            applyClimateControl(false);
        }

        if (wasRunning != running || previousEnergyUse != energyPerTick)
        {
            updateBlockState();
            setChanged();
        }
    }

    protected abstract int calculateEnergyUse();

    protected void applyClimateControl(boolean powered)
    {
    }

    protected abstract int clampTarget(int value);

    protected abstract void updateBlockState();

    protected ClimateType getPreferredClimateType()
    {
        return ClimateType.GREENHOUSE;
    }

    protected int getDisplayEffectiveSpace()
    {
        return 0;
    }

    protected int getDisplayIndoorTemperature()
    {
        return 0;
    }

    protected int getDisplayPreservationTenths()
    {
        return 0;
    }

    protected int getDisplayMinimumTarget()
    {
        return 0;
    }

    protected int getDisplayMaximumTarget()
    {
        return 0;
    }

    protected int getDisplayBeforeTemperature()
    {
        return 0;
    }

    protected int getDisplayStructureType()
    {
        return STRUCTURE_NONE;
    }

    protected int getDisplayTier()
    {
        return 0;
    }

    protected int getDisplayPowerMultiplier()
    {
        return 0;
    }

    protected int getDisplayBaseTemperatureDelta()
    {
        return 0;
    }

    protected int getDisplayControlRange()
    {
        return 0;
    }

    protected int getDisplayBaseTemperature()
    {
        return getDisplayBeforeTemperature();
    }

    protected int getGreenhouseDisplayTier(GreenhouseStructureData data)
    {
        final String key = data.displayNameKey();
        if (key.endsWith(".mixed"))
        {
            return 5;
        }
        if (key.endsWith(".custom"))
        {
            return 0;
        }
        return data.tier().ordinal() + 1;
    }

    private boolean hasEnoughEnergyToRun(int requiredEnergy)
    {
        final int stored = energyStorage.getEnergyStored();
        if (stored < requiredEnergy)
        {
            return false;
        }
        return running || stored > START_ENERGY_THRESHOLD;
    }

    protected int roundedEnergyUse(int effectiveSpace, float multiplier, float temperatureDelta)
    {
        if (effectiveSpace <= 0 || multiplier <= 0 || temperatureDelta <= 0)
        {
            return 0;
        }
        return Math.max(MIN_ENERGY_USE, Math.round(effectiveSpace * multiplier * temperatureDelta * ENERGY_USE_SCALE));
    }

    public void refreshStructure(boolean force)
    {
        final Level level = getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }

        final long gameTime = level.getGameTime();
        if (!force && gameTime < nextStructureRefreshTick)
        {
            registerLocalStructure();
            return;
        }
        nextStructureRefreshTick = gameTime + STRUCTURE_REFRESH_INTERVAL;

        final boolean found = getPreferredClimateType() == ClimateType.CELLAR
            ? refreshCellarStructure(level) || refreshGreenhouseStructure(level)
            : refreshGreenhouseStructure(level) || refreshCellarStructure(level);
        if (!found)
        {
            clearLocalStructure(level);
        }
        onStructureRefreshed();
    }

    protected void onStructureRefreshed()
    {
    }

    protected List<BlockPos> getStructureDetectionPositions()
    {
        return List.of(worldPosition);
    }

    public boolean hasLocalStructureData()
    {
        return localClimatePositions != null && localClimateType != null
            && (localGreenhouseStructureData != null || localCellarStructureData != null);
    }

    private boolean refreshGreenhouseStructure(Level level)
    {
        MixedGreenhouseDetector.Result result = null;
        for (BlockPos pos : getStructureDetectionPositions())
        {
            result = MixedGreenhouseDetector.detectFromDevice(level, pos);
            if (result != null)
            {
                break;
            }
        }
        if (result == null)
        {
            return false;
        }
        final Set<BlockPos> oldPositions = copyLocalClimatePositions();
        final ClimateType oldType = localClimateType;
        final Set<BlockPos> oldCellarPositions = getCellarPositionsForResync();
        final Set<BlockPos> positions = withControlSourcePositions(result.positions());
        localClimatePositions = positions;
        localClimateType = ClimateType.GREENHOUSE;
        localGreenhouseStructureData = result.structureData();
        localCellarStructureData = null;
        localCellarTemperature = 0;
        localCellarHeating = false;
        localManualTemperatureAdjustmentTenths = GreenhouseTemperatureHelper.clampGreenhouseManualAdjustmentTenths(this, localManualTemperatureAdjustmentTenths);
        tfcml$refreshAutoTemperature(true);
        updateClimateReceivers(level, positions, true, result.firmalifeTier(), ClimateType.GREENHOUSE);
        clearObsoleteClimateReceivers(level, oldPositions, positions, oldType);
        registerLocalStructure();
        resyncOldCellarPositions(level, oldCellarPositions);
        markClimateDataChanged();
        return true;
    }

    private boolean refreshCellarStructure(Level level)
    {
        ConfiguredCellarDetector.Result result = null;
        for (BlockPos pos : getStructureDetectionPositions())
        {
            result = ConfiguredCellarDetector.detectFromDevice(level, pos);
            if (result != null)
            {
                break;
            }
        }
        if (result == null)
        {
            return false;
        }
        final Set<BlockPos> oldPositions = copyLocalClimatePositions();
        final ClimateType oldType = localClimateType;
        final Set<BlockPos> positions = withControlSourcePositions(result.positions());
        localClimatePositions = positions;
        localClimateType = ClimateType.CELLAR;
        localGreenhouseStructureData = null;
        localManualTemperatureAdjustmentTenths = 0;
        localCellarStructureData = result.structureData();
        localCellarTemperature = clampCellarAdjustment(localCellarTemperature, localCellarHeating);
        updateClimateReceivers(level, positions, true, 0, ClimateType.CELLAR);
        clearObsoleteClimateReceivers(level, oldPositions, positions, oldType);
        registerLocalStructure();
        CellarPreservationHelper.syncTrackedInventories(this);
        markClimateDataChanged();
        return true;
    }

    private void clearLocalStructure(Level level)
    {
        final Set<BlockPos> oldPositions = copyLocalClimatePositions();
        final ClimateType oldType = localClimateType;
        final Set<BlockPos> oldCellarPositions = getCellarPositionsForResync();
        localClimatePositions = null;
        localClimateType = null;
        localGreenhouseStructureData = null;
        localCellarStructureData = null;
        localManualTemperatureAdjustmentTenths = 0;
        localCellarTemperature = 0;
        localCellarHeating = false;
        ClimateStationRegistry.unregister(this, this);
        if (!removing)
        {
            updateClimateReceivers(level, oldPositions, false, 0, oldType);
            resyncOldCellarPositions(level, oldCellarPositions);
        }
        markClimateDataChanged();
    }

    private void registerLocalStructure()
    {
        if (hasLocalStructureData())
        {
            ClimateStationRegistry.register(this, this);
        }
    }

    private Set<BlockPos> withControlSourcePositions(Set<BlockPos> positions)
    {
        final Set<BlockPos> merged = new HashSet<>(positions);
        merged.addAll(getStructureDetectionPositions());
        return merged;
    }

    @Nullable
    private Set<BlockPos> copyLocalClimatePositions()
    {
        return localClimatePositions != null ? Set.copyOf(localClimatePositions) : null;
    }

    @Nullable
    private Set<BlockPos> getCellarPositionsForResync()
    {
        return localClimateType == ClimateType.CELLAR && localClimatePositions != null ? Set.copyOf(localClimatePositions) : null;
    }

    private static void resyncOldCellarPositions(Level level, @Nullable Set<BlockPos> oldCellarPositions)
    {
        if (oldCellarPositions != null && !oldCellarPositions.isEmpty())
        {
            CellarPreservationHelper.syncBlockEntities(level, oldCellarPositions);
        }
    }

    private static void updateClimateReceivers(Level level, @Nullable Set<BlockPos> positions, boolean valid, int tier, ClimateType type)
    {
        if (positions == null || positions.isEmpty() || type == null)
        {
            return;
        }
        for (BlockPos pos : positions)
        {
            if (!valid && hasOtherClimateSource(level, pos, type))
            {
                continue;
            }
            final ClimateReceiver receiver = ClimateReceiver.get(level, pos);
            if (receiver != null)
            {
                receiver.setValid(level, pos, valid, tier, type);
            }
        }
    }

    private static boolean hasOtherClimateSource(Level level, BlockPos pos, ClimateType type)
    {
        return type == ClimateType.CELLAR
            ? ClimateStationRegistry.findControllingCellarStation(level, pos) != null
            : ClimateStationRegistry.findControllingGreenhouseStation(level, pos) != null;
    }

    private static void clearObsoleteClimateReceivers(Level level, @Nullable Set<BlockPos> oldPositions, Set<BlockPos> newPositions, @Nullable ClimateType oldType)
    {
        if (oldPositions == null || oldPositions.isEmpty() || oldType == null)
        {
            return;
        }
        final Set<BlockPos> obsoletePositions = new HashSet<>(oldPositions);
        obsoletePositions.removeAll(newPositions);
        updateClimateReceivers(level, obsoletePositions, false, 0, oldType);
    }

    public void setTarget(int value)
    {
        final int previousTarget = target;
        target = clampTarget(value);
        if (previousTarget != target)
        {
            updateBlockState();
        }
        setChanged();
    }

    public void setEnabled(boolean enabled)
    {
        if (this.enabled == enabled)
        {
            return;
        }
        this.enabled = enabled;
        if (!enabled)
        {
            energyPerTick = 0;
            running = false;
            applyClimateControl(false);
        }
        updateBlockState();
        setChanged();
    }

    public void toggleEnabled()
    {
        setEnabled(!enabled);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void adjustTarget(int delta)
    {
        setTarget(target + delta);
    }

    public int getTarget()
    {
        return target;
    }

    public boolean isRunning()
    {
        return running;
    }

    public int getEnergyPerTick()
    {
        return energyPerTick;
    }

    public EnergyStorage getEnergyStorage()
    {
        return energyStorage;
    }

    public ContainerData getSyncData()
    {
        return syncData;
    }

    public ContainerData getNetworkSyncData()
    {
        return networkSyncData;
    }

    public int getDisplayStructureTypeForTooltip()
    {
        return getDisplayStructureType();
    }

    public int getDisplayTierForTooltip()
    {
        return getDisplayTier();
    }

    public int getDisplayBaseTemperatureDeltaForTooltip()
    {
        return getDisplayBaseTemperatureDelta();
    }

    @Override
    @Nullable
    public Set<BlockPos> tfcml$getClimatePositions()
    {
        return localClimatePositions;
    }

    @Override
    public ClimateType tfcml$getClimateType()
    {
        return localClimateType != null ? localClimateType : getPreferredClimateType();
    }

    @Override
    public int tfcml$getGreenhouseTier()
    {
        return localGreenhouseStructureData != null ? localGreenhouseStructureData.firmalifeTier() : 0;
    }

    @Override
    public float tfcml$getAutoTemperature()
    {
        return localAutoTemperature;
    }

    @Override
    public int tfcml$getManualTemperatureAdjustment()
    {
        return Math.round(GreenhouseTemperatureHelper.fromTenths(localManualTemperatureAdjustmentTenths));
    }

    @Override
    public int tfcml$getManualTemperatureAdjustmentTenths()
    {
        return localManualTemperatureAdjustmentTenths;
    }

    @Override
    public float tfcml$getEffectiveTemperature()
    {
        return GreenhouseTemperatureHelper.clampTemperature(GreenhouseTemperatureHelper.fromTenths(
            GreenhouseTemperatureHelper.toTenths(localAutoTemperature) + localManualTemperatureAdjustmentTenths
        ));
    }

    @Override
    public void tfcml$setManualTemperatureAdjustment(int adjustment)
    {
        tfcml$setManualTemperatureAdjustmentTenths(GreenhouseTemperatureHelper.toTenths(adjustment));
    }

    @Override
    public void tfcml$setManualTemperatureAdjustmentTenths(int adjustmentTenths)
    {
        final int clamped = GreenhouseTemperatureHelper.clampGreenhouseManualAdjustmentTenths(this, adjustmentTenths);
        if (clamped != localManualTemperatureAdjustmentTenths)
        {
            localManualTemperatureAdjustmentTenths = clamped;
            markClimateDataChanged();
        }
    }

    @Override
    public void tfcml$refreshAutoTemperature(boolean force)
    {
        if (localClimateType != ClimateType.GREENHOUSE || localGreenhouseStructureData == null)
        {
            return;
        }
        final Level level = getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        final long currentDay = GreenhouseTemperatureHelper.getCurrentAutoUpdateDay();
        if (!force && currentDay == localLastAutoUpdateDay)
        {
            return;
        }
        localLastAutoUpdateDay = currentDay;
        localAutoTemperature = GreenhouseTemperatureHelper.calculateAutoTemperature(level, worldPosition, localGreenhouseStructureData.tier());
        markClimateDataChanged();
    }

    @Override
    public int tfcml$getCellarTemperature()
    {
        return localCellarTemperature;
    }

    @Override
    public void tfcml$setCellarTemperature(int temperature)
    {
        tfcml$setCellarTemperature(temperature, false);
    }

    @Override
    public void tfcml$setCellarTemperature(int temperature, boolean allowsHeating)
    {
        final int clamped = clampCellarAdjustment(temperature, allowsHeating);
        if (clamped != localCellarTemperature || allowsHeating != localCellarHeating)
        {
            localCellarTemperature = clamped;
            localCellarHeating = allowsHeating;
            if (!removing)
            {
                CellarPreservationHelper.syncTrackedInventories(this);
            }
            markClimateDataChanged();
        }
    }

    @Override
    public void tfcml$setCellarTemperatureSilently(int temperature, boolean allowsHeating)
    {
        final int clamped = clampCellarAdjustment(temperature, allowsHeating);
        if (clamped != localCellarTemperature || allowsHeating != localCellarHeating)
        {
            localCellarTemperature = clamped;
            localCellarHeating = allowsHeating;
            markClimateDataChanged();
        }
    }

    @Override
    public boolean tfcml$allowsCellarHeating()
    {
        return localCellarHeating;
    }

    @Override
    @Nullable
    public GreenhouseStructureData tfcml$getGreenhouseStructureData()
    {
        return localGreenhouseStructureData;
    }

    @Override
    public void tfcml$setGreenhouseStructureData(@Nullable GreenhouseStructureData data)
    {
        localGreenhouseStructureData = data;
        if (data != null)
        {
            localClimateType = ClimateType.GREENHOUSE;
            localManualTemperatureAdjustmentTenths = GreenhouseTemperatureHelper.clampGreenhouseManualAdjustmentTenths(this, localManualTemperatureAdjustmentTenths);
            localCellarHeating = false;
            tfcml$refreshAutoTemperature(true);
        }
        markClimateDataChanged();
    }

    @Override
    @Nullable
    public CellarStructureData tfcml$getCellarStructureData()
    {
        return localCellarStructureData;
    }

    @Override
    public void tfcml$setCellarStructureData(@Nullable CellarStructureData data)
    {
        localCellarStructureData = data;
        if (data != null)
        {
            localClimateType = ClimateType.CELLAR;
            localCellarTemperature = clampCellarAdjustment(localCellarTemperature, localCellarHeating);
            if (!removing)
            {
                CellarPreservationHelper.syncTrackedInventories(this);
            }
        }
        else
        {
            localCellarHeating = false;
        }
        markClimateDataChanged();
    }

    @Override
    public boolean tfcml$hasFavoriteGreenhouseType()
    {
        return false;
    }

    @Override
    public void tfcml$clearFavoriteClimateHints()
    {
    }

    public void saveEnergyToItem(ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            stack.getOrCreateTagElement("BlockEntityTag").put("energy", energyStorage.serializeNBT());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        tag.put("energy", energyStorage.serializeNBT());
        tag.putInt("target", target);
        tag.putBoolean(ENABLED_KEY, enabled);
        tag.putFloat(LOCAL_AUTO_TEMPERATURE_KEY, localAutoTemperature);
        tag.putInt(LOCAL_MANUAL_TEMPERATURE_TENTHS_KEY, localManualTemperatureAdjustmentTenths);
        tag.putInt(LOCAL_CELLAR_TEMPERATURE_KEY, localCellarTemperature);
        tag.putBoolean(LOCAL_CELLAR_HEATING_KEY, localCellarHeating);
        tag.putLong(LOCAL_LAST_AUTO_DAY_KEY, localLastAutoUpdateDay);
        if (localClimateType != null)
        {
            tag.putString(LOCAL_TYPE_KEY, localClimateType.name());
        }
        if (localClimatePositions != null)
        {
            tag.putLongArray(LOCAL_POSITIONS_KEY, localClimatePositions.stream().mapToLong(BlockPos::asLong).toArray());
        }
        if (localGreenhouseStructureData != null)
        {
            tag.put(LOCAL_GREENHOUSE_STRUCTURE_KEY, localGreenhouseStructureData.toTag());
        }
        if (localCellarStructureData != null)
        {
            tag.put(LOCAL_CELLAR_STRUCTURE_KEY, localCellarStructureData.toTag());
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        if (tag.contains("energy"))
        {
            energyStorage.deserializeNBT(tag.get("energy"));
        }
        final int loadedTarget = tag.getInt("target");
        enabled = tag.getBoolean(ENABLED_KEY);
        localAutoTemperature = tag.contains(LOCAL_AUTO_TEMPERATURE_KEY)
            ? GreenhouseTemperatureHelper.clampTemperature(tag.getFloat(LOCAL_AUTO_TEMPERATURE_KEY))
            : GreenhouseTemperatureHelper.DEFAULT_TEMPERATURE;
        localManualTemperatureAdjustmentTenths = tag.contains(LOCAL_MANUAL_TEMPERATURE_TENTHS_KEY)
            ? tag.getInt(LOCAL_MANUAL_TEMPERATURE_TENTHS_KEY)
            : GreenhouseTemperatureHelper.toTenths(tag.getInt(LOCAL_MANUAL_TEMPERATURE_KEY));
        localCellarTemperature = tag.getInt(LOCAL_CELLAR_TEMPERATURE_KEY);
        localCellarHeating = tag.getBoolean(LOCAL_CELLAR_HEATING_KEY);
        localLastAutoUpdateDay = tag.contains(LOCAL_LAST_AUTO_DAY_KEY) ? tag.getLong(LOCAL_LAST_AUTO_DAY_KEY) : Long.MIN_VALUE;
        localClimateType = tag.contains(LOCAL_TYPE_KEY) ? loadClimateType(tag.getString(LOCAL_TYPE_KEY)) : null;
        localClimatePositions = tag.contains(LOCAL_POSITIONS_KEY)
            ? java.util.Arrays.stream(tag.getLongArray(LOCAL_POSITIONS_KEY)).mapToObj(BlockPos::of).collect(java.util.stream.Collectors.toCollection(HashSet::new))
            : null;
        localGreenhouseStructureData = tag.contains(LOCAL_GREENHOUSE_STRUCTURE_KEY)
            ? GreenhouseStructureData.fromTag(tag.getCompound(LOCAL_GREENHOUSE_STRUCTURE_KEY))
            : null;
        localCellarStructureData = tag.contains(LOCAL_CELLAR_STRUCTURE_KEY)
            ? CellarStructureData.fromTag(tag.getCompound(LOCAL_CELLAR_STRUCTURE_KEY))
            : null;
        localManualTemperatureAdjustmentTenths = GreenhouseTemperatureHelper.clampGreenhouseManualAdjustmentTenths(this, localManualTemperatureAdjustmentTenths);
        localCellarTemperature = clampCellarAdjustment(localCellarTemperature, localCellarHeating);
        if (localClimateType != ClimateType.CELLAR)
        {
            localCellarHeating = false;
        }
        target = getLevel() != null ? clampTarget(loadedTarget) : loadedTarget;
        updateRegistryRegistration();
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        updateRegistryRegistration();
    }

    @Override
    public void onChunkUnloaded()
    {
        ClimateStationRegistry.unregister(this, this);
        super.onChunkUnloaded();
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void updateRegistryRegistration()
    {
        if (level == null)
        {
            return;
        }
        if (hasLocalStructureData())
        {
            ClimateStationRegistry.register(this, this);
        }
        else
        {
            ClimateStationRegistry.unregister(this, this);
        }
    }

    private void markClimateDataChanged()
    {
        setChanged();
        updateRegistryRegistration();
        if (level instanceof ServerLevel serverLevel)
        {
            final BlockState state = getBlockState();
            serverLevel.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    private static ClimateType loadClimateType(String name)
    {
        try
        {
            return ClimateType.valueOf(name);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }
    }

    private int clampCellarAdjustment(int adjustment, boolean allowsHeating)
    {
        final Level level = getLevel();
        if (level == null)
        {
            return allowsHeating
                ? GreenhouseTemperatureHelper.clampAirConditionerCellarTarget(this, adjustment)
                : Math.min(0, adjustment);
        }
        final float baseTemperature = level != null ? GreenhouseTemperatureHelper.getAmbientTemperature(level, worldPosition) : 0;
        return allowsHeating
            ? GreenhouseTemperatureHelper.clampAirConditionerCellarAdjustment(this, baseTemperature, adjustment)
            : GreenhouseTemperatureHelper.clampCellarCoolingAdjustment(this, baseTemperature, adjustment);
    }

    @Override
    public void setRemoved()
    {
        final Level level = getLevel();
        if (level != null && !level.isClientSide())
        {
            removing = true;
            clearAppliedClimateControl();
            ClimateStationRegistry.unregister(this, this);
        }
        super.setRemoved();
    }

    public void onRemovedByBlock()
    {
        final Level level = getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }
        clearAppliedClimateControl();
        ClimateStationRegistry.unregister(this, this);
    }

    protected void clearAppliedClimateControl()
    {
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("block.tfc_modern_life." + getSerializedName());
    }

    protected abstract String getSerializedName();

    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side)
    {
        if (cap == ForgeCapabilities.ENERGY)
        {
            return energyCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    private final class InputOnlyEnergyStorage extends EnergyStorage
    {
        private InputOnlyEnergyStorage()
        {
            super(ENERGY_CAPACITY, ENERGY_MAX_INPUT, 0, 0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            final int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate)
            {
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            return 0;
        }

        private void consumeEnergy(int amount)
        {
            energy = Math.max(0, energy - Math.max(0, amount));
        }

        private void setEnergy(int amount)
        {
            deserializeNBT(IntTag.valueOf(Math.max(0, Math.min(ENERGY_CAPACITY, amount))));
        }
    }
}
