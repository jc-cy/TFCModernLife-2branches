package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import java.util.Locale;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class GreenhouseTemperatureHelper
{
    public static final int DEFAULT_TEMPERATURE = 20;
    public static final int DEFAULT_MANUAL_RANGE = 10;
    public static final float MIN_TEMPERATURE = -2048f;
    public static final float MAX_TEMPERATURE = 2048f;
    public static final int TEMPERATURE_SCALE = 10;
    private static final long AUTO_UPDATE_HOUR = 8L;

    private GreenhouseTemperatureHelper() {}

    public static float getAmbientTemperature(Level level, BlockPos pos)
    {
        return Climate.getTemperature(level, pos);
    }

    public static float getAverageTemperature(Level level, BlockPos pos)
    {
        return Climate.getAverageTemperature(level, pos);
    }

    public static float clampTemperature(float temperature)
    {
        return Mth.clamp(temperature, MIN_TEMPERATURE, MAX_TEMPERATURE);
    }

    public static float calculateAutoTemperature(Level level, BlockPos stationPos, GreenhouseTier tier)
    {
        final float current = getAmbientTemperature(level, stationPos);
        final float average = getAverageTemperature(level, stationPos);
        return moveToward(current, average, tier.autoRange());
    }

    public static float getGreenhouseBaseTemperatureDelta(Level level, ClimateStationAccess station)
    {
        if (station.tfcml$getGreenhouseStructureData() == null || !(station instanceof BlockEntity blockEntity))
        {
            return 0;
        }
        return station.tfcml$getAutoTemperature() - getAmbientTemperature(level, blockEntity.getBlockPos());
    }

    public static String formatSignedTemperatureDelta(float delta)
    {
        return delta > 0 ? "+" + formatTemperature(delta) : formatTemperature(delta);
    }

    public static String formatSignedTemperatureDeltaTenths(int deltaTenths)
    {
        return formatSignedTemperatureDelta(fromTenths(deltaTenths));
    }

    public static String formatTemperature(float temperature)
    {
        return String.format(Locale.ROOT, "%.1f", temperature);
    }

    public static String formatTemperatureTenths(int temperatureTenths)
    {
        return formatTemperature(fromTenths(temperatureTenths));
    }

    public static int toTenths(float temperature)
    {
        return Math.round(temperature * TEMPERATURE_SCALE);
    }

    public static float fromTenths(int temperatureTenths)
    {
        return temperatureTenths / (float) TEMPERATURE_SCALE;
    }

    public static int roundForPanel(int temperatureTenths)
    {
        return Math.round(fromTenths(temperatureTenths));
    }

    public static float moveToward(float current, float target, int maxDelta)
    {
        if (current < target)
        {
            return Math.min(target, current + Math.max(0, maxDelta));
        }
        if (current > target)
        {
            return Math.max(target, current - Math.max(0, maxDelta));
        }
        return current;
    }

    public static long getCurrentAutoUpdateDay()
    {
        final long calendarTicks = Calendars.SERVER.getCalendarTicks();
        final long day = Math.floorDiv(calendarTicks, ICalendar.TICKS_IN_DAY);
        final long time = Math.floorMod(calendarTicks, ICalendar.TICKS_IN_DAY);
        return time >= AUTO_UPDATE_HOUR * ICalendar.TICKS_IN_HOUR ? day : day - 1L;
    }

    public static float getControlledTemperature(Level level, BlockPos pos, float fallbackTemperature)
    {
        return getControlledTemperature(level, pos, ClimateType.GREENHOUSE, fallbackTemperature);
    }

    public static float getControlledTemperature(Level level, BlockPos pos, ClimateType climateType, float fallbackTemperature)
    {
        final ClimateStationAccess station = climateType == ClimateType.CELLAR
            ? ClimateStationRegistry.findControllingCellarStation(level, pos)
            : ClimateStationRegistry.findControllingGreenhouseStation(level, pos);
        if (station == null)
        {
            return fallbackTemperature;
        }
        return climateType == ClimateType.CELLAR
            ? station.tfcml$getEffectiveCellarTemperature(fallbackTemperature)
            : station.tfcml$getEffectiveTemperature();
    }

    public static boolean isControlledGreenhouse(Level level, BlockPos pos)
    {
        return ClimateStationRegistry.findControllingGreenhouseStation(level, pos) != null;
    }

    public static int getGreenhouseManualRange(@Nullable ClimateStationAccess station)
    {
        final GreenhouseStructureData data = station != null ? station.tfcml$getGreenhouseStructureData() : null;
        return data != null ? data.tier().manualRange() : 0;
    }

    public static int clampGreenhouseManualAdjustment(@Nullable ClimateStationAccess station, int adjustment)
    {
        final int range = getGreenhouseManualRange(station);
        return Mth.clamp(adjustment, -range, range);
    }

    public static int clampAirConditionerCellarTarget(@Nullable ClimateStationAccess station, int temperature)
    {
        return Mth.clamp(temperature, -DEFAULT_MANUAL_RANGE, DEFAULT_MANUAL_RANGE);
    }

    public static int clampCoolingGreenhouseManualAdjustment(@Nullable ClimateStationAccess station, int adjustment)
    {
        final int range = getGreenhouseManualRange(station);
        return Mth.clamp(adjustment, -range, 0);
    }

    public static float getCellarMinimumTemperature(@Nullable ClimateStationAccess station)
    {
        final CellarStructureData data = station != null ? station.tfcml$getCellarStructureData() : null;
        return data != null ? data.minimumTemperature() : CellarTier.SEALED_BRICK.minimumTemperature();
    }

    public static int getMinimumCellarCoolingAdjustment(@Nullable ClimateStationAccess station, float baseTemperature)
    {
        final float minimumTemperature = getCellarMinimumTemperature(station);
        return baseTemperature <= minimumTemperature ? 0 : Math.min(0, (int) Math.ceil(minimumTemperature - baseTemperature));
    }

    public static int getMinimumAirConditionerCellarAdjustment(@Nullable ClimateStationAccess station, float baseTemperature)
    {
        final float minimumTemperature = getCellarMinimumTemperature(station);
        return baseTemperature <= minimumTemperature ? 0 : Math.max(-DEFAULT_MANUAL_RANGE, (int) Math.ceil(minimumTemperature - baseTemperature));
    }

    public static int clampCellarCoolingAdjustment(@Nullable ClimateStationAccess station, float baseTemperature, int adjustment)
    {
        return Mth.clamp(adjustment, getMinimumCellarCoolingAdjustment(station, baseTemperature), 0);
    }

    public static int clampAirConditionerCellarAdjustment(@Nullable ClimateStationAccess station, float baseTemperature, int adjustment)
    {
        return Mth.clamp(adjustment, getMinimumAirConditionerCellarAdjustment(station, baseTemperature), DEFAULT_MANUAL_RANGE);
    }

    public static float getEffectiveCellarTemperature(@Nullable ClimateStationAccess station, float baseTemperature, int adjustment)
    {
        return baseTemperature + clampCellarCoolingAdjustment(station, baseTemperature, adjustment);
    }

    public static float getCellarTemperatureDelta(@Nullable ClimateStationAccess station, float baseTemperature, int adjustment)
    {
        return Math.abs(getEffectiveCellarTemperature(station, baseTemperature, adjustment) - baseTemperature);
    }

    public static float getAirConditionerCellarTemperature(@Nullable ClimateStationAccess station, float baseTemperature, int adjustment)
    {
        return baseTemperature + clampAirConditionerCellarAdjustment(station, baseTemperature, adjustment);
    }

    public static float getAirConditionerCellarTemperatureDelta(@Nullable ClimateStationAccess station, float baseTemperature, int adjustment)
    {
        return Math.abs(getAirConditionerCellarTemperature(station, baseTemperature, adjustment) - baseTemperature);
    }

    public static int getEffectiveSpace(@Nullable ClimateStationAccess station)
    {
        if (station == null)
        {
            return 0;
        }
        final SetSize size = new SetSize(station.tfcml$getClimatePositions() == null ? 0 : station.tfcml$getClimatePositions().size());
        return size.value();
    }

    public static float getCellarPreservationMultiplier(float temperature)
    {
        if (temperature <= -30)
        {
            return 10f;
        }
        if (temperature <= -27)
        {
            return 8.5f;
        }
        if (temperature <= -24)
        {
            return 7f;
        }
        if (temperature <= -21)
        {
            return 6f;
        }
        if (temperature <= -18)
        {
            return 5f;
        }
        if (temperature <= -15)
        {
            return 4.5f;
        }
        if (temperature <= -12)
        {
            return 4f;
        }
        if (temperature <= -9)
        {
            return 3.5f;
        }
        if (temperature <= -6)
        {
            return 3f;
        }
        if (temperature < 0)
        {
            return 2.5f;
        }
        return 2f;
    }

    public static String formatFactor(float factor)
    {
        if (Float.isInfinite(factor))
        {
            return "infinite";
        }
        if (Math.abs(factor - Math.round(factor)) < 0.0001f)
        {
            return Integer.toString(Math.round(factor));
        }
        if (Math.abs(factor * 10f - Math.round(factor * 10f)) < 0.0001f)
        {
            return String.format(Locale.ROOT, "%.1f", factor);
        }
        return String.format(Locale.ROOT, "%.2f", factor);
    }

    private record SetSize(int value) {}
}
