package com.jccy.tfcmodernlife.mixin;

import com.eerussianguy.firmalife.common.blockentities.ClimateStationBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.jccy.tfcmodernlife.common.climate.CellarStructureData;
import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.GreenhouseStructureData;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClimateStationBlockEntity.class, remap = false)
public abstract class ClimateStationBlockEntityMixin implements ClimateStationAccess
{
    @Shadow private Set<BlockPos> positions;
    @Shadow private ClimateType type;
    @Shadow @Nullable private ResourceLocation favoriteGreenhouseType;
    @Shadow private boolean favoriteIsCellar;

    @Unique private static final String TFCML_GREENHOUSE_TIER_KEY = "TFCModernLifeGreenhouseTier";
    @Unique private static final String TFCML_GREENHOUSE_STRUCTURE_KEY = "TFCModernLifeGreenhouseStructure";
    @Unique private static final String TFCML_CELLAR_STRUCTURE_KEY = "TFCModernLifeCellarStructure";
    @Unique private static final String TFCML_AUTO_TEMPERATURE_KEY = "TFCModernLifeAutoTemperature";
    @Unique private static final String TFCML_MANUAL_TEMPERATURE_KEY = "TFCModernLifeManualTemperature";
    @Unique private static final String TFCML_CELLAR_TEMPERATURE_KEY = "TFCModernLifeCellarTemperature";
    @Unique private static final String TFCML_CELLAR_HEATING_KEY = "TFCModernLifeCellarHeating";
    @Unique private static final String TFCML_LAST_AUTO_DAY_KEY = "TFCModernLifeLastAutoDay";

    @Unique private int tfcml$greenhouseTier = 0;
    @Unique private float tfcml$autoTemperature = GreenhouseTemperatureHelper.DEFAULT_TEMPERATURE;
    @Unique private int tfcml$manualTemperatureAdjustment = 0;
    @Unique private int tfcml$cellarTemperature = 0;
    @Unique private boolean tfcml$cellarHeating = false;
    @Unique private long tfcml$lastAutoUpdateDay = Long.MIN_VALUE;
    @Unique @Nullable private GreenhouseStructureData tfcml$greenhouseStructureData = null;
    @Unique @Nullable private CellarStructureData tfcml$cellarStructureData = null;

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void tfcml$loadClimateData(CompoundTag nbt, CallbackInfo ci)
    {
        tfcml$greenhouseTier = Math.max(0, nbt.getInt(TFCML_GREENHOUSE_TIER_KEY));
        tfcml$autoTemperature = nbt.contains(TFCML_AUTO_TEMPERATURE_KEY)
            ? GreenhouseTemperatureHelper.clampTemperature(nbt.getFloat(TFCML_AUTO_TEMPERATURE_KEY))
            : GreenhouseTemperatureHelper.DEFAULT_TEMPERATURE;
        tfcml$manualTemperatureAdjustment = 0;
        tfcml$cellarTemperature = nbt.getInt(TFCML_CELLAR_TEMPERATURE_KEY);
        tfcml$cellarHeating = nbt.getBoolean(TFCML_CELLAR_HEATING_KEY);
        tfcml$lastAutoUpdateDay = nbt.contains(TFCML_LAST_AUTO_DAY_KEY) ? nbt.getLong(TFCML_LAST_AUTO_DAY_KEY) : Long.MIN_VALUE;
        tfcml$greenhouseStructureData = nbt.contains(TFCML_GREENHOUSE_STRUCTURE_KEY)
            ? GreenhouseStructureData.fromTag(nbt.getCompound(TFCML_GREENHOUSE_STRUCTURE_KEY))
            : null;
        tfcml$cellarStructureData = nbt.contains(TFCML_CELLAR_STRUCTURE_KEY)
            ? CellarStructureData.fromTag(nbt.getCompound(TFCML_CELLAR_STRUCTURE_KEY))
            : null;
        tfcml$manualTemperatureAdjustment = GreenhouseTemperatureHelper.clampGreenhouseManualAdjustment(this, tfcml$manualTemperatureAdjustment);
        tfcml$cellarTemperature = tfcml$clampCellarAdjustment(tfcml$cellarTemperature, tfcml$cellarHeating);
        if (type != ClimateType.CELLAR)
        {
            tfcml$cellarHeating = false;
        }
    }

    @Inject(method = {"saveAdditional", "m_183515_"}, at = @At("TAIL"), require = 0)
    private void tfcml$saveClimateData(CompoundTag nbt, CallbackInfo ci)
    {
        nbt.putInt(TFCML_GREENHOUSE_TIER_KEY, tfcml$greenhouseTier);
        nbt.putFloat(TFCML_AUTO_TEMPERATURE_KEY, tfcml$autoTemperature);
        nbt.putInt(TFCML_MANUAL_TEMPERATURE_KEY, 0);
        nbt.putInt(TFCML_CELLAR_TEMPERATURE_KEY, tfcml$cellarTemperature);
        nbt.putBoolean(TFCML_CELLAR_HEATING_KEY, tfcml$cellarHeating);
        nbt.putLong(TFCML_LAST_AUTO_DAY_KEY, tfcml$lastAutoUpdateDay);
        if (tfcml$greenhouseStructureData != null)
        {
            nbt.put(TFCML_GREENHOUSE_STRUCTURE_KEY, tfcml$greenhouseStructureData.toTag());
        }
        if (tfcml$cellarStructureData != null)
        {
            nbt.put(TFCML_CELLAR_STRUCTURE_KEY, tfcml$cellarStructureData.toTag());
        }
    }

    @Inject(method = "setFavorite", at = @At("HEAD"), cancellable = true)
    private void tfcml$acceptConfiguredCellarWall(ItemStack held, CallbackInfoReturnable<Boolean> cir)
    {
        if (!(held.getItem() instanceof BlockItem blockItem))
        {
            return;
        }
        if (!com.jccy.tfcmodernlife.common.climate.ClimateControlConfig.isCellarThermalWall(blockItem.getBlock().defaultBlockState()))
        {
            return;
        }

        favoriteGreenhouseType = null;
        favoriteIsCellar = true;
        tfcml$markForSyncIfServer();
        cir.setReturnValue(true);
    }

    @Inject(method = "updateValidity", at = @At("HEAD"))
    private void tfcml$cacheGreenhouseTier(boolean valid, int tier, CallbackInfo ci)
    {
        tfcml$greenhouseTier = valid ? Math.max(0, tier) : 0;
        if (!valid)
        {
            tfcml$greenhouseStructureData = null;
            tfcml$cellarStructureData = null;
            tfcml$manualTemperatureAdjustment = 0;
            tfcml$cellarTemperature = 0;
            tfcml$cellarHeating = false;
        }
        tfcml$refreshAutoTemperature(true);
    }

    @Inject(method = "setType", at = @At("TAIL"))
    private void tfcml$clearUnusedStructureData(ClimateType climateType, CallbackInfo ci)
    {
        if (climateType != ClimateType.GREENHOUSE)
        {
            tfcml$greenhouseStructureData = null;
            tfcml$manualTemperatureAdjustment = 0;
        }
        if (climateType != ClimateType.CELLAR)
        {
            tfcml$cellarStructureData = null;
            tfcml$cellarTemperature = 0;
            tfcml$cellarHeating = false;
        }
        if (climateType == ClimateType.GREENHOUSE)
        {
            tfcml$refreshAutoTemperature(true);
        }
        tfcml$markForSyncIfServer();
    }

    @Override
    public Set<BlockPos> tfcml$getClimatePositions()
    {
        return positions;
    }

    @Override
    public ClimateType tfcml$getClimateType()
    {
        return type;
    }

    @Override
    public int tfcml$getGreenhouseTier()
    {
        return tfcml$greenhouseTier;
    }

    @Override
    public float tfcml$getAutoTemperature()
    {
        return tfcml$autoTemperature;
    }

    @Override
    public int tfcml$getManualTemperatureAdjustment()
    {
        return 0;
    }

    @Override
    public int tfcml$getManualTemperatureAdjustmentTenths()
    {
        return 0;
    }

    @Override
    public float tfcml$getEffectiveTemperature()
    {
        return tfcml$autoTemperature;
    }

    @Override
    public void tfcml$setManualTemperatureAdjustment(int adjustment)
    {
        if (tfcml$manualTemperatureAdjustment != 0)
        {
            tfcml$manualTemperatureAdjustment = 0;
            tfcml$markForSyncIfServer();
        }
    }

    @Override
    public void tfcml$refreshAutoTemperature(boolean force)
    {
        if (type != ClimateType.GREENHOUSE || tfcml$greenhouseStructureData == null)
        {
            return;
        }
        final ClimateStationBlockEntity self = (ClimateStationBlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level == null || level.isClientSide())
        {
            return;
        }

        final long currentDay = GreenhouseTemperatureHelper.getCurrentAutoUpdateDay();
        if (!force && currentDay == tfcml$lastAutoUpdateDay)
        {
            return;
        }
        tfcml$lastAutoUpdateDay = currentDay;
        tfcml$autoTemperature = GreenhouseTemperatureHelper.calculateAutoTemperature(level, self.getBlockPos(), tfcml$greenhouseStructureData.tier());
        self.markForSync();
    }

    @Override
    public int tfcml$getCellarTemperature()
    {
        return tfcml$cellarTemperature;
    }

    @Override
    public void tfcml$setCellarTemperature(int temperature)
    {
        tfcml$setCellarTemperature(temperature, false);
    }

    @Override
    public void tfcml$setCellarTemperature(int temperature, boolean allowsHeating)
    {
        final int clamped = tfcml$clampCellarAdjustment(temperature, allowsHeating);
        if (clamped != tfcml$cellarTemperature || allowsHeating != tfcml$cellarHeating)
        {
            tfcml$cellarTemperature = clamped;
            tfcml$cellarHeating = allowsHeating;
            CellarPreservationHelper.syncTrackedInventories(this);
            tfcml$markForSyncIfServer();
        }
    }

    @Override
    public void tfcml$setCellarTemperatureSilently(int temperature, boolean allowsHeating)
    {
        final int clamped = tfcml$clampCellarAdjustment(temperature, allowsHeating);
        if (clamped != tfcml$cellarTemperature || allowsHeating != tfcml$cellarHeating)
        {
            tfcml$cellarTemperature = clamped;
            tfcml$cellarHeating = allowsHeating;
            tfcml$markForSyncIfServer();
        }
    }

    @Override
    public boolean tfcml$allowsCellarHeating()
    {
        return tfcml$cellarHeating;
    }

    @Override
    @Nullable
    public GreenhouseStructureData tfcml$getGreenhouseStructureData()
    {
        return tfcml$greenhouseStructureData;
    }

    @Override
    public void tfcml$setGreenhouseStructureData(@Nullable GreenhouseStructureData data)
    {
        tfcml$greenhouseStructureData = data;
        if (data != null)
        {
            tfcml$cellarHeating = false;
        }
        tfcml$manualTemperatureAdjustment = GreenhouseTemperatureHelper.clampGreenhouseManualAdjustment(this, tfcml$manualTemperatureAdjustment);
        tfcml$refreshAutoTemperature(true);
        tfcml$markForSyncIfServer();
    }

    @Override
    @Nullable
    public CellarStructureData tfcml$getCellarStructureData()
    {
        return tfcml$cellarStructureData;
    }

    @Override
    public void tfcml$setCellarStructureData(@Nullable CellarStructureData data)
    {
        tfcml$cellarStructureData = data;
        tfcml$cellarTemperature = tfcml$clampCellarAdjustment(tfcml$cellarTemperature, tfcml$cellarHeating);
        if (data == null)
        {
            tfcml$cellarHeating = false;
        }
        CellarPreservationHelper.syncTrackedInventories(this);
        tfcml$markForSyncIfServer();
    }

    @Override
    public boolean tfcml$hasFavoriteGreenhouseType()
    {
        return favoriteGreenhouseType != null;
    }

    @Override
    public void tfcml$clearFavoriteClimateHints()
    {
        if (favoriteGreenhouseType == null && !favoriteIsCellar)
        {
            return;
        }
        favoriteGreenhouseType = null;
        favoriteIsCellar = false;
        tfcml$markForSyncIfServer();
    }

    @Unique
    private void tfcml$markForSyncIfServer()
    {
        final ClimateStationBlockEntity self = (ClimateStationBlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level != null && !level.isClientSide())
        {
            self.markForSync();
        }
    }

    @Unique
    private int tfcml$clampCellarAdjustment(int adjustment, boolean allowsHeating)
    {
        final ClimateStationBlockEntity self = (ClimateStationBlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level == null)
        {
            return allowsHeating
                ? GreenhouseTemperatureHelper.clampAirConditionerCellarTarget(this, adjustment)
                : Math.min(0, adjustment);
        }
        final float baseTemperature = level != null ? GreenhouseTemperatureHelper.getAmbientTemperature(level, self.getBlockPos()) : 0;
        return allowsHeating
            ? GreenhouseTemperatureHelper.clampAirConditionerCellarAdjustment(this, baseTemperature, adjustment)
            : GreenhouseTemperatureHelper.clampCellarCoolingAdjustment(this, baseTemperature, adjustment);
    }
}
