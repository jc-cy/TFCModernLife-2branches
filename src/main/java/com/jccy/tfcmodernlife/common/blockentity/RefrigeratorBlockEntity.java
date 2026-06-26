package com.jccy.tfcmodernlife.common.blockentity;

import com.jccy.tfcmodernlife.common.ModBlocks;
import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.jccy.tfcmodernlife.common.climate.GreenhouseStructureData;
import com.jccy.tfcmodernlife.common.block.RefrigeratorBlock;
import com.jccy.tfcmodernlife.common.climate.CellarStructureData;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import com.jccy.tfcmodernlife.common.container.RefrigeratorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RefrigeratorBlockEntity extends ClimateControlBlockEntity
{
    public static final int MAX_TARGET_TEMPERATURE = 0;
    public static final int MIN_TARGET_TEMPERATURE = -6;
    private static final int BASE_CELLAR_TEMPERATURE = 0;
    @Nullable private ClimateStationAccess appliedStation;

    public RefrigeratorBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlocks.REFRIGERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected ClimateType getPreferredClimateType()
    {
        return ClimateType.CELLAR;
    }

    @Override
    protected int calculateEnergyUse()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return 0;
        }
        final CellarStructureData data = station.tfcml$getCellarStructureData();
        if (data != null)
        {
            final float baseTemperature = getBaseCellarTemperature();
            target = GreenhouseTemperatureHelper.clampCellarCoolingAdjustment(station, baseTemperature, target);
            return roundedEnergyUse(
                data.effectiveSpace(),
                data.powerMultiplier(),
                GreenhouseTemperatureHelper.getCellarTemperatureDelta(station, baseTemperature, target)
            );
        }

        final GreenhouseStructureData greenhouseData = station.tfcml$getGreenhouseStructureData();
        if (greenhouseData != null)
        {
            target = clampGreenhouseCoolingTarget(station, target);
            return roundedEnergyUse(greenhouseData.effectiveSpace(), greenhouseData.tier().powerMultiplier(), Math.abs(target));
        }
        return 0;
    }

    @Override
    protected int clampTarget(int value)
    {
        final ClimateStationAccess station = findStation();
        if (station != null)
        {
            if (station.tfcml$getCellarStructureData() != null)
            {
                return GreenhouseTemperatureHelper.clampCellarCoolingAdjustment(station, getBaseCellarTemperature(), value);
            }
            if (station.tfcml$getGreenhouseStructureData() != null)
            {
                return clampGreenhouseCoolingTarget(station, value);
            }
        }
        return Math.max(MIN_TARGET_TEMPERATURE, Math.min(MAX_TARGET_TEMPERATURE, value));
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
            if (station.tfcml$getCellarStructureData() != null)
            {
                station.tfcml$setCellarTemperature(target, false);
            }
            else if (station.tfcml$getGreenhouseStructureData() != null)
            {
                station.tfcml$setManualTemperatureAdjustment(target);
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
        if (level != null)
        {
            RefrigeratorBlock.setActive(level, worldPosition, getBlockState(), running);
        }
    }

    @Override
    protected String getSerializedName()
    {
        return "refrigerator";
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        refreshStructure(true);
        return RefrigeratorContainer.create(this, playerInventory, windowId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RefrigeratorBlockEntity entity)
    {
        entity.serverTick();
    }

    public int getConnectedEffectiveSpace()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return 0;
        }
        final CellarStructureData cellarData = station.tfcml$getCellarStructureData();
        if (cellarData != null)
        {
            return cellarData.effectiveSpace();
        }
        final GreenhouseStructureData greenhouseData = station.tfcml$getGreenhouseStructureData();
        return greenhouseData != null ? greenhouseData.effectiveSpace() : 0;
    }

    public String getConnectedTierId()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return station.tfcml$getCellarStructureData().tier().id();
        }
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return station.tfcml$getGreenhouseStructureData().tier().id();
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
        return station.tfcml$getCellarStructureData() != null
            ? station.tfcml$getEffectiveCellarTemperature(getBaseCellarTemperature())
            : station.tfcml$getEffectiveTemperature();
    }

    public float getBaseTemperature()
    {
        final ClimateStationAccess station = findStation();
        if (station == null)
        {
            return 0;
        }
        return station.tfcml$getCellarStructureData() != null ? getBaseCellarTemperature() : station.tfcml$getAutoTemperature();
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
        return GreenhouseTemperatureHelper.toTenths(getIndoorTemperature());
    }

    @Override
    protected int getDisplayBeforeTemperature()
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
            return clampGreenhouseCoolingTarget(station, Integer.MIN_VALUE);
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return GreenhouseTemperatureHelper.getMinimumCellarCoolingAdjustment(station, getBaseCellarTemperature());
        }
        return MIN_TARGET_TEMPERATURE;
    }

    @Override
    protected int getDisplayMaximumTarget()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return 0;
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return MAX_TARGET_TEMPERATURE;
        }
        return MAX_TARGET_TEMPERATURE;
    }

    @Override
    protected int getDisplayStructureType()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return STRUCTURE_CELLAR;
        }
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return STRUCTURE_GREENHOUSE;
        }
        return STRUCTURE_NONE;
    }

    @Override
    protected int getDisplayTier()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            final CellarStructureData data = station.tfcml$getCellarStructureData();
            return data.mixedThermalWalls() ? 4 : data.tier().ordinal() + 1;
        }
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return getGreenhouseDisplayTier(station.tfcml$getGreenhouseStructureData());
        }
        return 0;
    }

    @Override
    protected int getDisplayBaseTemperatureDelta()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            final Level level = getLevel();
            if (level == null)
            {
                return 0;
            }
            return GreenhouseTemperatureHelper.toTenths(GreenhouseTemperatureHelper.getGreenhouseBaseTemperatureDelta(level, station));
        }
        return 0;
    }

    @Override
    protected int getDisplayPowerMultiplier()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return Math.round(station.tfcml$getCellarStructureData().powerMultiplier() * 10f);
        }
        if (station != null && station.tfcml$getGreenhouseStructureData() != null)
        {
            return Math.round(station.tfcml$getGreenhouseStructureData().tier().powerMultiplier() * 10f);
        }
        return 0;
    }

    @Override
    protected void clearAppliedClimateControl()
    {
        final ClimateStationAccess station = appliedStation != null ? appliedStation : target != 0 ? findStation() : null;
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

    private static int clampGreenhouseCoolingTarget(@Nullable ClimateStationAccess station, int value)
    {
        final int structureMinimum = -GreenhouseTemperatureHelper.getGreenhouseManualRange(station);
        return Math.max(Math.max(structureMinimum, MIN_TARGET_TEMPERATURE), Math.min(MAX_TARGET_TEMPERATURE, value));
    }

    private static void clearStation(ClimateStationAccess station)
    {
        if (station.tfcml$getCellarStructureData() != null)
        {
            station.tfcml$setCellarTemperatureSilently(BASE_CELLAR_TEMPERATURE, false);
        }
        if (station.tfcml$getGreenhouseStructureData() != null)
        {
            station.tfcml$setManualTemperatureAdjustment(0);
        }
    }

}
