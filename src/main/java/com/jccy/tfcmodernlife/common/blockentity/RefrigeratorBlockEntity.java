package com.jccy.tfcmodernlife.common.blockentity;

import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.ModParticles;
import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.jccy.tfcmodernlife.common.block.RefrigeratorBlock;
import com.jccy.tfcmodernlife.common.climate.GreenhouseStructureData;
import com.jccy.tfcmodernlife.common.climate.CellarStructureData;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import com.jccy.tfcmodernlife.common.container.RefrigeratorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RefrigeratorBlockEntity extends ClimateControlBlockEntity
{
    public static final int MAX_CELLAR_SET_TEMPERATURE = 99;
    public static final int MIN_CELLAR_SET_TEMPERATURE = -99;
    private static final int MAX_GREENHOUSE_COOLING_TARGET = 0;
    private static final int MIN_GREENHOUSE_COOLING_TARGET = -6;
    private static final int BASE_CELLAR_TEMPERATURE = 0;
    private static final String CELLAR_SET_TEMPERATURE_MODE_KEY = "cellarSetTemperatureMode";
    private static final float COLD_MIST_SPAWN_CHANCE = 0.3f;
    @Nullable private ClimateStationAccess appliedStation;
    private boolean cellarSetTemperatureInitialized;

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
            return NO_CONTROL_NEEDED;
        }
        final CellarStructureData data = station.tfcml$getCellarStructureData();
        if (data != null)
        {
            final float baseTemperature = getBaseCellarTemperature();
            initializeCellarSetTemperature(station);
            target = clampCellarHardLimit(target);
            return roundedEnergyUse(
                data.effectiveSpace(),
                data.powerMultiplier(),
                GreenhouseTemperatureHelper.getCellarTemperatureDelta(station, baseTemperature, getCellarTargetAdjustment(station))
            );
        }

        final GreenhouseStructureData greenhouseData = station.tfcml$getGreenhouseStructureData();
        if (greenhouseData != null)
        {
            target = clampGreenhouseCoolingTarget(station, target);
            return roundedEnergyUse(greenhouseData.effectiveSpace(), greenhouseData.tier().powerMultiplier(), Math.abs(target));
        }
        return NO_CONTROL_NEEDED;
    }

    @Override
    protected int clampTarget(int value)
    {
        final ClimateStationAccess station = findStation();
        if (station != null)
        {
            if (station.tfcml$getCellarStructureData() != null)
            {
                return clampCellarSetTemperature(station, value);
            }
            if (station.tfcml$getGreenhouseStructureData() != null)
            {
                return clampGreenhouseCoolingTarget(station, value);
            }
        }
        return Math.max(MIN_GREENHOUSE_COOLING_TARGET, Math.min(MAX_GREENHOUSE_COOLING_TARGET, value));
    }

    @Override
    public void adjustTarget(int delta)
    {
        final ClimateStationAccess station = findStation();
        if (delta != 0 && station != null && station.tfcml$getCellarStructureData() != null && cellarSetTemperatureInitialized)
        {
            final int minTarget = getMinimumCellarSetTemperature(station);
            final int maxTarget = getMaximumCellarSetTemperature();
            if ((delta < 0 && target <= minTarget) || (delta > 0 && target >= maxTarget))
            {
                return;
            }
        }
        super.adjustTarget(delta);
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
                station.tfcml$setCellarTemperature(getCellarTargetAdjustment(station), false);
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

    public static void clientTick(Level level, BlockPos pos, BlockState state, RefrigeratorBlockEntity entity)
    {
        if (state.getValue(RefrigeratorBlock.ACTIVE))
        {
            entity.spawnColdMist(state);
        }
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
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            initializeCellarSetTemperature(station);
            return GreenhouseTemperatureHelper.toTenths(target);
        }
        return GreenhouseTemperatureHelper.toTenths(getIndoorTemperature());
    }

    @Override
    protected int getDisplayBeforeTemperature()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getCellarStructureData() != null)
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
            return clampGreenhouseCoolingTarget(station, Integer.MIN_VALUE);
        }
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            return getMinimumCellarSetTemperature(station);
        }
        return MIN_GREENHOUSE_COOLING_TARGET;
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
            return getMaximumCellarSetTemperature();
        }
        return MAX_GREENHOUSE_COOLING_TARGET;
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
            return getCellarDisplayTier(data);
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

    private int getCellarTargetAdjustment(ClimateStationAccess station)
    {
        return GreenhouseTemperatureHelper.getCellarCoolingAdjustmentTowardTarget(station, getBaseCellarTemperature(), target);
    }

    private int clampCellarSetTemperature(ClimateStationAccess station, int value)
    {
        return Math.max(getMinimumCellarSetTemperature(station), Math.min(getMaximumCellarSetTemperature(), value));
    }

    private static int clampCellarHardLimit(int value)
    {
        return Math.max(MIN_CELLAR_SET_TEMPERATURE, Math.min(MAX_CELLAR_SET_TEMPERATURE, value));
    }

    private int getMinimumCellarSetTemperature(ClimateStationAccess station)
    {
        final float baseTemperature = getBaseCellarTemperature();
        final float minimum = GreenhouseTemperatureHelper.getEffectiveCellarTemperature(
            station,
            baseTemperature,
            GreenhouseTemperatureHelper.getMinimumCellarCoolingAdjustment(station, baseTemperature)
        );
        return clampCellarTargetBound(minimum);
    }

    private int getMaximumCellarSetTemperature()
    {
        return clampCellarTargetBound(getBaseCellarTemperature());
    }

    private static int clampCellarTargetBound(float value)
    {
        return Math.max(MIN_CELLAR_SET_TEMPERATURE, Math.min(MAX_CELLAR_SET_TEMPERATURE, Math.round(value)));
    }

    private void initializeCellarSetTemperature(ClimateStationAccess station)
    {
        if (cellarSetTemperatureInitialized || station.tfcml$getCellarStructureData() == null)
        {
            return;
        }
        target = clampCellarSetTemperature(station, Math.round(getBaseCellarTemperature() + target));
        cellarSetTemperatureInitialized = true;
        setChanged();
    }

    private static int clampGreenhouseCoolingTarget(@Nullable ClimateStationAccess station, int value)
    {
        final int structureMinimum = -GreenhouseTemperatureHelper.getGreenhouseManualRange(station);
        return Math.max(Math.max(structureMinimum, MIN_GREENHOUSE_COOLING_TARGET), Math.min(MAX_GREENHOUSE_COOLING_TARGET, value));
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

    @Override
    protected void onStructureRefreshed()
    {
        final ClimateStationAccess station = findStation();
        if (station != null && station.tfcml$getCellarStructureData() != null)
        {
            initializeCellarSetTemperature(station);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        if (cellarSetTemperatureInitialized)
        {
            tag.putBoolean(CELLAR_SET_TEMPERATURE_MODE_KEY, true);
        }
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        final int loadedTarget = tag.getInt("target");
        cellarSetTemperatureInitialized = tag.getBoolean(CELLAR_SET_TEMPERATURE_MODE_KEY);
        final ClimateStationAccess station = findStation();
        if (!cellarSetTemperatureInitialized && getLevel() != null && station != null && station.tfcml$getCellarStructureData() != null)
        {
            target = clampCellarSetTemperature(station, Math.round(getBaseCellarTemperature() + loadedTarget));
            cellarSetTemperatureInitialized = true;
        }
        else if (cellarSetTemperatureInitialized && station != null && station.tfcml$getCellarStructureData() != null)
        {
            target = clampCellarHardLimit(loadedTarget);
        }
    }

    private void spawnColdMist(BlockState state)
    {
        final Level level = getLevel();
        if (level == null)
        {
            return;
        }
        final RandomSource random = level.getRandom();
        if (random.nextFloat() > COLD_MIST_SPAWN_CHANCE)
        {
            return;
        }

        final Direction facing = state.getValue(RefrigeratorBlock.FACING);
        final Direction side = facing.getClockWise();
        final double sideOffset = (random.nextDouble() - 0.5D) * 0.72D;
        final double forwardSpeed = 0.026D + random.nextDouble() * 0.014D;
        final double sideSpeed = (random.nextDouble() - 0.5D) * 0.008D;

        final double x = worldPosition.getX() + 0.5D + facing.getStepX() * 0.61D + side.getStepX() * sideOffset;
        final double y = worldPosition.getY() + 0.42D + random.nextDouble() * 0.36D;
        final double z = worldPosition.getZ() + 0.5D + facing.getStepZ() * 0.61D + side.getStepZ() * sideOffset;
        final double xSpeed = facing.getStepX() * forwardSpeed + side.getStepX() * sideSpeed;
        final double ySpeed = -0.002D - random.nextDouble() * 0.004D;
        final double zSpeed = facing.getStepZ() * forwardSpeed + side.getStepZ() * sideSpeed;

        level.addParticle(ModParticles.REFRIGERATOR_COLD_MIST.get(), x, y, z, xSpeed, ySpeed, zSpeed);
    }

}
