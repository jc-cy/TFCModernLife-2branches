package com.jccy.tfcmodernlife.common.blockentity;

import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.block.ClimateControlMode;
import com.jccy.tfcmodernlife.common.block.ThermostaticAirConditionerBlock;
import com.jccy.tfcmodernlife.common.climate.CellarStructureData;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.GreenhouseStructureData;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import com.jccy.tfcmodernlife.common.container.ThermostaticAirConditionerContainer;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

public class ThermostaticAirConditionerBlockEntity extends ClimateControlBlockEntity
{
    public static final int MAX_MANUAL_ADJUSTMENT = 70;
    private static final int MIN_GREENHOUSE_SET_TEMPERATURE = -99;
    private static final int MAX_GREENHOUSE_SET_TEMPERATURE = 99;
    private static final int BASE_CELLAR_TEMPERATURE = 0;
    private static final String GREENHOUSE_SET_TEMPERATURE_MODE_KEY = "greenhouseSetTemperatureMode";
    @Nullable private ClimateStationAccess appliedStation;
    private boolean greenhouseSetTemperatureInitialized;

    public ThermostaticAirConditionerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlocks.THERMOSTATIC_AIR_CONDITIONER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected int calculateEnergyUse()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return 0;
        }
        final GreenhouseStructureData data = station.tfcml$getGreenhouseStructureData();
        if (data != null)
        {
            initializeGreenhouseSetTemperature(station);
            target = clampGreenhouseSetTemperature(target);
            return roundedEnergyUse(data.effectiveSpace(), data.tier().powerMultiplier(), Math.abs(getGreenhouseTargetAdjustment(station)));
        }

        final CellarStructureData cellarData = station.tfcml$getCellarStructureData();
        if (cellarData != null)
        {
            final float baseTemperature = getBaseCellarTemperature();
            target = GreenhouseTemperatureHelper.clampAirConditionerCellarAdjustment(station, baseTemperature, target);
            return roundedEnergyUse(
                cellarData.effectiveSpace(),
                cellarData.powerMultiplier(),
                GreenhouseTemperatureHelper.getAirConditionerCellarTemperatureDelta(station, baseTemperature, target)
            );
        }
        return 0;
    }

    @Override
    protected int clampTarget(int value)
    {
        final ClimateStationAccess station = findStation();
        if (station != null)
        {
            if (station.tfcml$getGreenhouseStructureData() != null)
            {
                return clampGreenhouseSetTemperature(value);
            }
            if (station.tfcml$getCellarStructureData() != null)
            {
                return GreenhouseTemperatureHelper.clampAirConditionerCellarAdjustment(station, getBaseCellarTemperature(), value);
            }
        }
        return Math.max(-GreenhouseTemperatureHelper.DEFAULT_MANUAL_RANGE, Math.min(GreenhouseTemperatureHelper.DEFAULT_MANUAL_RANGE, value));
    }

    @Override
    protected void applyClimateControl(boolean powered)
    {
        final ClimateStationAccess station = findStation();
        if (appliedStation != null && appliedStation != station)
        {
            clearStation(appliedStation);
            appliedStation = null;
        }
        if (station != null && powered)
        {
            if (station.tfcml$getGreenhouseStructureData() != null)
            {
                station.tfcml$setManualTemperatureAdjustmentTenths(getGreenhouseTargetAdjustmentTenths(station));
            }
            else if (station.tfcml$getCellarStructureData() != null)
            {
                station.tfcml$setCellarTemperature(target, true);
            }
            appliedStation = station;
        }
        else if (appliedStation != null)
        {
            clearStation(appliedStation);
            appliedStation = null;
        }
        else if (station != null)
        {
            clearStation(station);
        }
    }

    @Override
    protected void updateBlockState()
    {
        final Level level = getLevel();
        if (level == null)
        {
            return;
        }
        final float delta = running ? getControlTemperatureDelta() : 0f;
        final ClimateControlMode mode = delta > 0 ? ClimateControlMode.HEAT : delta < 0 ? ClimateControlMode.COLD : ClimateControlMode.IDLE;
        ThermostaticAirConditionerBlock.setMode(level, worldPosition, getBlockState(), running, mode);
    }

    @Override
    protected void onStructureRefreshed()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            initializeGreenhouseSetTemperature(station);
        }
    }

    @Override
    public void setTarget(int value)
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            greenhouseSetTemperatureInitialized = true;
        }
        super.setTarget(value);
    }

    @Override
    protected String getSerializedName()
    {
        return "thermostatic_air_conditioner";
    }

    @Override
    protected List<BlockPos> getStructureDetectionPositions()
    {
        return List.of(worldPosition, worldPosition.above());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        refreshStructure(true);
        return ThermostaticAirConditionerContainer.create(this, playerInventory, windowId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThermostaticAirConditionerBlockEntity entity)
    {
        if (state.getValue(ThermostaticAirConditionerBlock.HALF) == DoubleBlockHalf.LOWER)
        {
            entity.serverTick();
        }
    }

    public int getConnectedEffectiveSpace()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return 0;
        }
        final GreenhouseStructureData greenhouseData = station.tfcml$getGreenhouseStructureData();
        if (greenhouseData != null)
        {
            return greenhouseData.effectiveSpace();
        }
        final CellarStructureData cellarData = station.tfcml$getCellarStructureData();
        return cellarData != null ? cellarData.effectiveSpace() : 0;
    }

    public String getConnectedTierId()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return station.tfcml$getGreenhouseStructureData().tier().id();
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return station.tfcml$getCellarStructureData().tier().id();
        }
        return "none";
    }

    public float getIndoorTemperature()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return 0;
        }
        return station.tfcml$getGreenhouseStructureData() != null
            ? station.tfcml$getEffectiveTemperature()
            : station.tfcml$getEffectiveCellarTemperature(getBaseCellarTemperature());
    }

    public float getBaseTemperature()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return 0;
        }
        return station.tfcml$getGreenhouseStructureData() != null ? station.tfcml$getAutoTemperature() : getBaseCellarTemperature();
    }

    public float getPreservationMultiplier()
    {
        final ClimateStationAccess station = findStation();
        return station != null && station.tfcml$getCellarStructureData() != null
            ? GreenhouseTemperatureHelper.getCellarPreservationMultiplier(station.tfcml$getEffectiveCellarTemperature(getBaseCellarTemperature()))
            : 0f;
    }

    @Override
    protected int getDisplayEffectiveSpace()
    {
        return getConnectedEffectiveSpace();
    }

    @Override
    protected int getDisplayIndoorTemperature()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return GreenhouseTemperatureHelper.toTenths(target);
        }
        return GreenhouseTemperatureHelper.toTenths(getIndoorTemperature());
    }

    @Override
    protected int getDisplayBeforeTemperature()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return GreenhouseTemperatureHelper.toTenths(getIndoorTemperature());
        }
        return GreenhouseTemperatureHelper.toTenths(getBaseTemperature());
    }

    @Override
    protected int getDisplayBaseTemperature()
    {
        return GreenhouseTemperatureHelper.toTenths(getBaseTemperature());
    }

    @Override
    protected int getDisplayPreservationTenths()
    {
        return Math.round(getPreservationMultiplier() * 10f);
    }

    @Override
    protected int getDisplayMinimumTarget()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return MIN_GREENHOUSE_SET_TEMPERATURE;
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return GreenhouseTemperatureHelper.getMinimumAirConditionerCellarAdjustment(station, getBaseCellarTemperature());
        }
        final int max = getDisplayMaximumTarget();
        return -max;
    }

    @Override
    protected int getDisplayMaximumTarget()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return MAX_GREENHOUSE_SET_TEMPERATURE;
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return GreenhouseTemperatureHelper.DEFAULT_MANUAL_RANGE;
        }
        return GreenhouseTemperatureHelper.DEFAULT_MANUAL_RANGE;
    }

    @Override
    protected int getDisplayStructureType()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return STRUCTURE_GREENHOUSE;
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return STRUCTURE_CELLAR;
        }
        return STRUCTURE_NONE;
    }

    @Override
    protected int getDisplayTier()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return getGreenhouseDisplayTier(station.tfcml$getGreenhouseStructureData());
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            final CellarStructureData data = station.tfcml$getCellarStructureData();
            return data.mixedThermalWalls() ? 4 : data.tier().ordinal() + 1;
        }
        return 0;
    }

    @Override
    protected int getDisplayPowerMultiplier()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return Math.round(station.tfcml$getGreenhouseStructureData().tier().powerMultiplier() * 10f);
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return Math.round(station.tfcml$getCellarStructureData().powerMultiplier() * 10f);
        }
        return 0;
    }

    @Override
    protected int getDisplayBaseTemperatureDelta()
    {
        final ClimateStationAccess station = findStation();
        return station != null && station.tfcml$getGreenhouseStructureData() != null
            ? GreenhouseTemperatureHelper.toTenths(getGreenhouseBaseTemperatureDelta(station))
            : 0;
    }

    @Override
    protected int getDisplayControlRange()
    {
        final ClimateStationAccess station = findStation();
        return station != null && station.tfcml$getGreenhouseStructureData() != null
            ? GreenhouseTemperatureHelper.getGreenhouseManualRange(station)
            : Math.max(Math.abs(getDisplayMinimumTarget()), Math.abs(getDisplayMaximumTarget()));
    }

    @Override
    protected void clearAppliedClimateControl()
    {
        final ClimateStationAccess station = appliedStation != null ? appliedStation : findStation();
        if (station != null)
        {
            clearStation(station);
            appliedStation = null;
        }
    }

    @Nullable
    private ClimateStationAccess findStation()
    {
        return hasLocalStructureData() ? this : null;
    }

    private float getBaseCellarTemperature()
    {
        final Level level = getLevel();
        return level != null ? GreenhouseTemperatureHelper.getAmbientTemperature(level, worldPosition) : BASE_CELLAR_TEMPERATURE;
    }

    private float getGreenhouseBaseTemperatureDelta(ClimateStationAccess station)
    {
        final Level level = getLevel();
        if (level == null)
        {
            return 0;
        }
        return GreenhouseTemperatureHelper.getGreenhouseBaseTemperatureDelta(level, station);
    }

    private float getControlTemperatureDelta()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return target;
        }
        if (station.tfcml$getCellarStructureData() != null)
        {
            final float before = getBaseCellarTemperature();
            return GreenhouseTemperatureHelper.getAirConditionerCellarTemperature(station, before, target) - before;
        }
        return station.tfcml$getGreenhouseStructureData() != null ? getGreenhouseTargetAdjustment(station) : 0;
    }

    private float getGreenhouseTargetAdjustment(ClimateStationAccess station)
    {
        return GreenhouseTemperatureHelper.fromTenths(getGreenhouseTargetAdjustmentTenths(station));
    }

    private int getGreenhouseTargetAdjustmentTenths(ClimateStationAccess station)
    {
        return GreenhouseTemperatureHelper.getGreenhouseManualAdjustmentTowardTargetTenths(station, station.tfcml$getAutoTemperature(), target);
    }

    private static int clampGreenhouseSetTemperature(int value)
    {
        return Math.max(MIN_GREENHOUSE_SET_TEMPERATURE, Math.min(MAX_GREENHOUSE_SET_TEMPERATURE, value));
    }

    private void initializeGreenhouseSetTemperature(ClimateStationAccess station)
    {
        if (greenhouseSetTemperatureInitialized)
        {
            return;
        }
        target = clampGreenhouseSetTemperature(Math.round(station.tfcml$getAutoTemperature() + target));
        greenhouseSetTemperatureInitialized = true;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if (greenhouseSetTemperatureInitialized)
        {
            tag.putBoolean(GREENHOUSE_SET_TEMPERATURE_MODE_KEY, true);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        greenhouseSetTemperatureInitialized = tag.getBoolean(GREENHOUSE_SET_TEMPERATURE_MODE_KEY);
        final ClimateStationAccess station = findStation();
        if (!greenhouseSetTemperatureInitialized && station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            target = clampGreenhouseSetTemperature(Math.round(station.tfcml$getAutoTemperature() + target));
            greenhouseSetTemperatureInitialized = true;
        }
    }

    private static void clearStation(ClimateStationAccess station)
    {
        if (station.tfcml$getGreenhouseStructureData() != null)
        {
            station.tfcml$setManualTemperatureAdjustment(0);
        }
        if (station.tfcml$getCellarStructureData() != null)
        {
            station.tfcml$setCellarTemperatureSilently(BASE_CELLAR_TEMPERATURE, false);
        }
    }
}
