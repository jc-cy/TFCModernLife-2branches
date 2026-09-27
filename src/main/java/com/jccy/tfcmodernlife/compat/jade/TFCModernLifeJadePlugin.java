package com.jccy.tfcmodernlife.compat.jade;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.block.RefrigeratorBlock;
import com.jccy.tfcmodernlife.common.block.ThermostaticAirConditionerBlock;
import com.jccy.tfcmodernlife.common.blockentity.ClimateControlBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.climate.CellarStructureData;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import com.jccy.tfcmodernlife.common.climate.GreenhouseStructureData;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import com.eerussianguy.firmalife.common.blockentities.ClimateStationBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.eerussianguy.firmalife.common.blocks.greenhouse.ClimateStationBlock;
import net.dries007.tfc.common.blockentities.PlacedItemBlockEntity;
import net.dries007.tfc.common.blocks.devices.PlacedItemBlock;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/** Jade integration for the climate-control devices and cellar placement hints. */
@WailaPlugin
public final class TFCModernLifeJadePlugin implements IWailaPlugin
{
    private static final ResourceLocation AIR_CONDITIONER_UID = ResourceLocation.fromNamespaceAndPath(TFCModernLife.MOD_ID, "thermostatic_air_conditioner");
    private static final ResourceLocation REFRIGERATOR_UID = ResourceLocation.fromNamespaceAndPath(TFCModernLife.MOD_ID, "refrigerator");
    private static final ResourceLocation PLACED_ITEM_CELLAR_UID = ResourceLocation.fromNamespaceAndPath(TFCModernLife.MOD_ID, "placed_item_cellar");
    private static final ResourceLocation CLIMATE_STATION_UID = ResourceLocation.fromNamespaceAndPath(TFCModernLife.MOD_ID, "climate_station");
    private static final String ENERGY_PER_TICK = "EnergyPerTick";
    private static final String EFFECTIVE_SPACE = "EffectiveSpace";
    private static final String INDOOR_TEMPERATURE = "IndoorTemperature";
    private static final String PRESERVATION_MULTIPLIER = "PreservationMultiplier";
    private static final String STRUCTURE_TYPE = "StructureType";
    private static final String STRUCTURE_TIER = "StructureTier";
    private static final String BASE_TEMPERATURE_DELTA = "BaseTemperatureDelta";
    private static final String SET_TEMPERATURE = "SetTemperature";
    private static final String HAS_SET_TEMPERATURE = "HasSetTemperature";
    private static final String PLACED_ITEM_PRESERVATION_MULTIPLIER = "PlacedItemPreservationMultiplier";
    private static final String STATION_IS_STATION = "StationIsStation";
    private static final String STATION_ACTIVE = "StationActive";
    private static final String STATION_TYPE = "StationType";
    private static final String STATION_STRUCTURE = "StationStructure";
    private static final String STATION_BASE_DELTA = "StationBaseDelta";
    private static final String STATION_TEMPERATURE = "StationTemperature";
    private static final int STATION_TYPE_GREENHOUSE = 0;
    private static final int STATION_TYPE_CELLAR = 1;

    @Override
    public void register(IWailaCommonRegistration registry)
    {
        registry.registerBlockDataProvider(ClimateControlComponentProvider.INSTANCE, ThermostaticAirConditionerBlockEntity.class);
        registry.registerBlockDataProvider(ClimateControlComponentProvider.INSTANCE, RefrigeratorBlockEntity.class);
        registry.registerBlockDataProvider(PlacedItemCellarComponentProvider.INSTANCE, PlacedItemBlockEntity.class);
        registry.registerBlockDataProvider(ClimateStationComponentProvider.INSTANCE, ClimateStationBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registry)
    {
        registry.registerBlockComponent(ClimateControlComponentProvider.INSTANCE, ThermostaticAirConditionerBlock.class);
        registry.registerBlockComponent(ClimateControlComponentProvider.INSTANCE, RefrigeratorBlock.class);
        registry.registerBlockComponent(PlacedItemCellarComponentProvider.INSTANCE, PlacedItemBlock.class);
        registry.registerBlockComponent(ClimateStationComponentProvider.INSTANCE, ClimateStationBlock.class);
        registry.addRayTraceCallback((hitResult, accessor, originalAccessor) -> redirectAirConditionerUpperHalf(registry, accessor));
    }

    private static Accessor<?> redirectAirConditionerUpperHalf(IWailaClientRegistration registry, Accessor<?> accessor)
    {
        if (!(accessor instanceof BlockAccessor blockAccessor))
        {
            return accessor;
        }
        final BlockState state = blockAccessor.getBlockState();
        if (!(state.getBlock() instanceof ThermostaticAirConditionerBlock)
            || !state.hasProperty(ThermostaticAirConditionerBlock.HALF)
            || state.getValue(ThermostaticAirConditionerBlock.HALF) != DoubleBlockHalf.UPPER)
        {
            return accessor;
        }

        final BlockPos lowerPos = blockAccessor.getPosition().below();
        final BlockState lowerState = blockAccessor.getLevel().getBlockState(lowerPos);
        if (!lowerState.is(state.getBlock())
            || !lowerState.hasProperty(ThermostaticAirConditionerBlock.HALF)
            || lowerState.getValue(ThermostaticAirConditionerBlock.HALF) != DoubleBlockHalf.LOWER)
        {
            return accessor;
        }

        final BlockHitResult hit = blockAccessor.getHitResult();
        final BlockHitResult lowerHit = new BlockHitResult(hit.getLocation(), hit.getDirection(), lowerPos, hit.isInside());
        return registry.blockAccessor()
            .from(blockAccessor)
            .hit(lowerHit)
            .blockState(lowerState)
            .blockEntity(() -> blockAccessor.getLevel().getBlockEntity(lowerPos))
            .build();
    }

    private static void addTemperatureLines(ITooltip tooltip, float temperature)
    {
        final MutableComponent heat = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temperature);
        if (heat != null)
        {
            tooltip.add(heat);
        }
        tooltip.add(Component.translatable("tfc_modern_life.tooltip.current_temperature", Math.round(temperature)));
    }

    /**
     * Restores the 1.20 climate-station tooltip through the 1.21 tooltip route: Firmalife's own Jade integration
     * shows the greenhouse type and size, and this provider appends the mod's controlled temperature / cellar
     * detection on top of it (Jade and, via the same data provider registration, other tooltip consumers).
     */
    private enum ClimateStationComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config)
        {
            final CompoundTag data = access.getServerData();
            if (data.isEmpty() || !data.getBoolean(STATION_IS_STATION))
            {
                return;
            }
            if (!data.getBoolean(STATION_ACTIVE))
            {
                tooltip.add(Component.translatable("screen.tfc_modern_life.climate_station.inactive").withStyle(ChatFormatting.RED));
                return;
            }

            if (data.getInt(STATION_TYPE) == STATION_TYPE_GREENHOUSE)
            {
                final String structureKey = data.getString(STATION_STRUCTURE);
                if (!structureKey.isEmpty())
                {
                    tooltip.add(Component.translatable("tfc_modern_life.jade.greenhouse_with_base_temperature",
                        Component.translatable(structureKey),
                        GreenhouseTemperatureHelper.formatSignedTemperatureDeltaTenths(data.getInt(STATION_BASE_DELTA))));
                }
            }
            else
            {
                tooltip.add(Component.translatable("tfc_modern_life.tooltip.cellar_detected"));
            }
            tooltip.add(Component.translatable("tfc_modern_life.jade.effective_temperature",
                GreenhouseTemperatureHelper.formatTemperatureTenths(data.getInt(STATION_TEMPERATURE))));
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor access)
        {
            if (!(access.getBlockEntity() instanceof ClimateStationBlockEntity station)
                || !(station instanceof ClimateStationAccess stationAccess))
            {
                return;
            }
            data.putBoolean(STATION_IS_STATION, true);

            final boolean active = ClimateStationRegistry.isActiveStation(station, stationAccess);
            data.putBoolean(STATION_ACTIVE, active);
            if (!active)
            {
                return;
            }

            final Level level = station.getLevel();
            final GreenhouseStructureData greenhouseData = stationAccess.tfcml$getGreenhouseStructureData();
            final CellarStructureData cellarData = stationAccess.tfcml$getCellarStructureData();
            if (stationAccess.tfcml$getClimateType() == ClimateType.GREENHOUSE && greenhouseData != null)
            {
                data.putInt(STATION_TYPE, STATION_TYPE_GREENHOUSE);
                data.putString(STATION_STRUCTURE, greenhouseData.displayNameKey());
                data.putInt(STATION_BASE_DELTA, GreenhouseTemperatureHelper.toTenths(
                    level != null ? GreenhouseTemperatureHelper.getGreenhouseBaseTemperatureDelta(level, stationAccess) : 0f));
                data.putInt(STATION_TEMPERATURE, GreenhouseTemperatureHelper.toTenths(stationAccess.tfcml$getEffectiveTemperature()));
            }
            else if (stationAccess.tfcml$getClimateType() == ClimateType.CELLAR && cellarData != null)
            {
                data.putInt(STATION_TYPE, STATION_TYPE_CELLAR);
                final float baseTemperature = level != null
                    ? GreenhouseTemperatureHelper.getAmbientTemperature(level, station.getBlockPos())
                    : 0f;
                data.putInt(STATION_TEMPERATURE, GreenhouseTemperatureHelper.toTenths(
                    stationAccess.tfcml$getEffectiveCellarTemperature(baseTemperature)));
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return CLIMATE_STATION_UID;
        }
    }

    private enum ClimateControlComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config)
        {
            final CompoundTag data = access.getServerData();
            final ClimateControlBlockEntity control = access.getBlockEntity() instanceof ClimateControlBlockEntity value ? value : null;
            if (data.isEmpty() && control == null)
            {
                return;
            }

            final int structureType = data.isEmpty() ? control.getDisplayStructureTypeForTooltip() : data.getInt(STRUCTURE_TYPE);
            final int structureTier = data.isEmpty() ? control.getDisplayTierForTooltip() : data.getInt(STRUCTURE_TIER);
            final int baseDelta = data.isEmpty() ? control.getDisplayBaseTemperatureDeltaForTooltip() : data.getInt(BASE_TEMPERATURE_DELTA);
            final int energyPerTick = data.isEmpty() ? control.getEnergyPerTick() : data.getInt(ENERGY_PER_TICK);
            final int effectiveSpace;
            final int indoorTemperature;
            final int setTemperature;
            final boolean hasSetTemperature;
            final float preservationMultiplier;

            if (!data.isEmpty())
            {
                effectiveSpace = data.getInt(EFFECTIVE_SPACE);
                indoorTemperature = data.getInt(INDOOR_TEMPERATURE);
                setTemperature = data.getInt(SET_TEMPERATURE);
                hasSetTemperature = data.getBoolean(HAS_SET_TEMPERATURE);
                preservationMultiplier = data.getFloat(PRESERVATION_MULTIPLIER);
            }
            else if (control instanceof ThermostaticAirConditionerBlockEntity airConditioner)
            {
                effectiveSpace = airConditioner.getConnectedEffectiveSpace();
                indoorTemperature = GreenhouseTemperatureHelper.toTenths(airConditioner.getIndoorTemperature());
                hasSetTemperature = structureType == ClimateControlBlockEntity.STRUCTURE_GREENHOUSE
                    || structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR;
                setTemperature = hasSetTemperature ? GreenhouseTemperatureHelper.toTenths(airConditioner.getTarget()) : 0;
                preservationMultiplier = airConditioner.getPreservationMultiplier();
            }
            else if (control instanceof RefrigeratorBlockEntity refrigerator)
            {
                effectiveSpace = refrigerator.getConnectedEffectiveSpace();
                indoorTemperature = GreenhouseTemperatureHelper.toTenths(refrigerator.getIndoorTemperature());
                hasSetTemperature = structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR;
                setTemperature = hasSetTemperature ? GreenhouseTemperatureHelper.toTenths(refrigerator.getTarget()) : 0;
                preservationMultiplier = refrigerator.getPreservationMultiplier();
            }
            else
            {
                return;
            }

            if (structureType == ClimateControlBlockEntity.STRUCTURE_GREENHOUSE)
            {
                tooltip.add(Component.translatable("tfc_modern_life.jade.greenhouse_with_base_temperature",
                    getStructureName(structureType, structureTier),
                    GreenhouseTemperatureHelper.formatSignedTemperatureDeltaTenths(baseDelta)));
            }
            else if (structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR)
            {
                tooltip.add(getStructureName(structureType, structureTier));
            }
            tooltip.add(Component.translatable("tfc_modern_life.jade.effective_space", effectiveSpace));
            if (effectiveSpace > 0)
            {
                if (hasSetTemperature)
                {
                    tooltip.add(Component.translatable("tfc_modern_life.jade.set_temperature",
                        GreenhouseTemperatureHelper.formatTemperatureTenths(setTemperature)));
                }
                tooltip.add(Component.translatable("tfc_modern_life.jade.indoor_temperature",
                    GreenhouseTemperatureHelper.formatTemperatureTenths(indoorTemperature)));
            }
            if (preservationMultiplier > 0f)
            {
                tooltip.add(Component.translatable("tfc_modern_life.jade.preservation", preservationMultiplier));
            }
            tooltip.add(Component.translatable("tfc_modern_life.jade.energy_per_tick", energyPerTick));
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor access)
        {
            if (!(access.getBlockEntity() instanceof ClimateControlBlockEntity control))
            {
                return;
            }
            data.putInt(ENERGY_PER_TICK, control.getEnergyPerTick());
            data.putInt(STRUCTURE_TYPE, control.getDisplayStructureTypeForTooltip());
            data.putInt(STRUCTURE_TIER, control.getDisplayTierForTooltip());
            data.putInt(BASE_TEMPERATURE_DELTA, control.getDisplayBaseTemperatureDeltaForTooltip());
            if (control instanceof ThermostaticAirConditionerBlockEntity airConditioner)
            {
                data.putInt(EFFECTIVE_SPACE, airConditioner.getConnectedEffectiveSpace());
                data.putInt(INDOOR_TEMPERATURE, GreenhouseTemperatureHelper.toTenths(airConditioner.getIndoorTemperature()));
                final int structureType = control.getDisplayStructureTypeForTooltip();
                final boolean hasSetTemperature = structureType == ClimateControlBlockEntity.STRUCTURE_GREENHOUSE
                    || structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR;
                data.putBoolean(HAS_SET_TEMPERATURE, hasSetTemperature);
                data.putInt(SET_TEMPERATURE, hasSetTemperature ? GreenhouseTemperatureHelper.toTenths(airConditioner.getTarget()) : 0);
                data.putFloat(PRESERVATION_MULTIPLIER, airConditioner.getPreservationMultiplier());
            }
            else if (control instanceof RefrigeratorBlockEntity refrigerator)
            {
                data.putInt(EFFECTIVE_SPACE, refrigerator.getConnectedEffectiveSpace());
                data.putInt(INDOOR_TEMPERATURE, GreenhouseTemperatureHelper.toTenths(refrigerator.getIndoorTemperature()));
                final boolean hasSetTemperature = control.getDisplayStructureTypeForTooltip() == ClimateControlBlockEntity.STRUCTURE_CELLAR;
                data.putBoolean(HAS_SET_TEMPERATURE, hasSetTemperature);
                data.putInt(SET_TEMPERATURE, hasSetTemperature ? GreenhouseTemperatureHelper.toTenths(refrigerator.getTarget()) : 0);
                data.putFloat(PRESERVATION_MULTIPLIER, refrigerator.getPreservationMultiplier());
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return AIR_CONDITIONER_UID;
        }

        private static Component getStructureName(int structureType, int tier)
        {
            if (structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR)
            {
                final String tierId = switch (tier)
                {
                    case 1 -> "sealed_brick";
                    case 2 -> "stainless_steel_reinforced";
                    case 3 -> "mixed";
                    default -> "custom";
                };
                return Component.translatable("screen.tfc_modern_life.cellar." + tierId);
            }
            final String tierId = switch (tier)
            {
                case 1 -> "wood";
                case 2 -> "copper";
                case 3 -> "iron";
                case 4 -> "stainless_steel";
                case 5 -> "mixed";
                default -> "custom";
            };
            return Component.translatable("screen.tfc_modern_life.greenhouse." + tierId);
        }
    }

    private enum PlacedItemCellarComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config)
        {
            final CompoundTag data = access.getServerData();
            if (!data.isEmpty() && data.contains(PLACED_ITEM_PRESERVATION_MULTIPLIER))
            {
                tooltip.add(Component.translatable("tfc_modern_life.jade.placed_item_preservation",
                    data.getFloat(PLACED_ITEM_PRESERVATION_MULTIPLIER)));
            }
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor access)
        {
            if (!(access.getBlockEntity() instanceof PlacedItemBlockEntity)
                || ClimateStationRegistry.findControllingCellarStation(access.getLevel(), access.getPosition()) == null)
            {
                return;
            }
            final var station = ClimateStationRegistry.findControllingCellarStation(access.getLevel(), access.getPosition());
            final float baseTemperature = GreenhouseTemperatureHelper.getAmbientTemperature(access.getLevel(), access.getPosition());
            final float multiplier = GreenhouseTemperatureHelper.getCellarPreservationMultiplier(
                station.tfcml$getEffectiveCellarTemperature(baseTemperature));
            if (multiplier > 0f)
            {
                data.putFloat(PLACED_ITEM_PRESERVATION_MULTIPLIER, multiplier);
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return PLACED_ITEM_CELLAR_UID;
        }
    }
}
