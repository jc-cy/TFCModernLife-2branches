package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ClimateStationAccess
{
    Set<BlockPos> tfcml$getClimatePositions();

    ClimateType tfcml$getClimateType();

    int tfcml$getGreenhouseTier();

    float tfcml$getAutoTemperature();

    int tfcml$getManualTemperatureAdjustment();

    default int tfcml$getManualTemperatureAdjustmentTenths()
    {
        return GreenhouseTemperatureHelper.toTenths(tfcml$getManualTemperatureAdjustment());
    }

    float tfcml$getEffectiveTemperature();

    void tfcml$setManualTemperatureAdjustment(int adjustment);

    default void tfcml$setManualTemperatureAdjustmentTenths(int adjustmentTenths)
    {
        tfcml$setManualTemperatureAdjustment(Math.round(GreenhouseTemperatureHelper.fromTenths(adjustmentTenths)));
    }

    void tfcml$refreshAutoTemperature(boolean force);

    int tfcml$getCellarTemperature();

    void tfcml$setCellarTemperature(int temperature);

    default void tfcml$setCellarTemperature(int temperature, boolean allowsHeating)
    {
        tfcml$setCellarTemperature(temperature);
    }

    default void tfcml$setCellarTemperatureSilently(int temperature, boolean allowsHeating)
    {
        tfcml$setCellarTemperature(temperature, allowsHeating);
    }

    default boolean tfcml$allowsCellarHeating()
    {
        return false;
    }

    default float tfcml$getEffectiveCellarTemperature(float baseTemperature)
    {
        return tfcml$allowsCellarHeating()
            ? GreenhouseTemperatureHelper.getAirConditionerCellarTemperature(this, baseTemperature, tfcml$getCellarTemperature())
            : GreenhouseTemperatureHelper.getEffectiveCellarTemperature(this, baseTemperature, tfcml$getCellarTemperature());
    }

    @Nullable
    GreenhouseStructureData tfcml$getGreenhouseStructureData();

    void tfcml$setGreenhouseStructureData(@Nullable GreenhouseStructureData data);

    @Nullable
    CellarStructureData tfcml$getCellarStructureData();

    void tfcml$setCellarStructureData(@Nullable CellarStructureData data);

    boolean tfcml$hasFavoriteGreenhouseType();

    void tfcml$clearFavoriteClimateHints();
}
