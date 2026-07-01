package com.jccy.tfcmodernlife.compat.jade;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.block.RefrigeratorBlock;
import com.jccy.tfcmodernlife.common.block.ThermostaticAirConditionerBlock;
import com.jccy.tfcmodernlife.common.blockentity.ClimateControlBlockEntity;
import com.jccy.tfcmodernlife.common.block.ElectricOvenBlock;
import com.jccy.tfcmodernlife.common.block.ElectricSoupPotBlock;
import com.jccy.tfcmodernlife.common.blockentity.ElectricOvenBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricSoupPotBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import net.minecraft.core.BlockPos;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.Accessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;

@WailaPlugin
public final class TFCModernLifeJadePlugin implements IWailaPlugin
{
    private static final ResourceLocation ELECTRIC_OVEN_UID = new ResourceLocation(TFCModernLife.MOD_ID, "electric_oven");
    private static final ResourceLocation ELECTRIC_SOUP_POT_UID = new ResourceLocation(TFCModernLife.MOD_ID, "electric_soup_pot");
    private static final ResourceLocation THERMOSTATIC_AIR_CONDITIONER_UID = new ResourceLocation(TFCModernLife.MOD_ID, "thermostatic_air_conditioner");
    private static final ResourceLocation REFRIGERATOR_UID = new ResourceLocation(TFCModernLife.MOD_ID, "refrigerator");
    private static final String CURRENT_TEMPERATURE = "CurrentTemperature";
    private static final String RECIPE_TEMPERATURE = "RecipeTemperature";
    private static final String PROGRESS = "Progress";
    private static final String PROGRESS_TOTAL = "ProgressTotal";
    private static final String HAS_OUTPUT = "HasOutput";
    private static final String ENERGY_PER_TICK = "EnergyPerTick";
    private static final String EFFECTIVE_SPACE = "EffectiveSpace";
    private static final String INDOOR_TEMPERATURE = "IndoorTemperature";
    private static final String PRESERVATION_MULTIPLIER = "PreservationMultiplier";
    private static final String STRUCTURE_TYPE = "StructureType";
    private static final String STRUCTURE_TIER = "StructureTier";
    private static final String BASE_TEMPERATURE_DELTA = "BaseTemperatureDelta";
    private static final String SET_TEMPERATURE = "SetTemperature";
    private static final String HAS_SET_TEMPERATURE = "HasSetTemperature";

    @Override
    public void register(IWailaCommonRegistration registry)
    {
        registry.registerBlockDataProvider(OvenComponentProvider.INSTANCE, ElectricOvenBlockEntity.class);
        registry.registerBlockDataProvider(SoupPotComponentProvider.INSTANCE, ElectricSoupPotBlockEntity.class);
        registry.registerBlockDataProvider(ClimateControlComponentProvider.INSTANCE, ThermostaticAirConditionerBlockEntity.class);
        registry.registerBlockDataProvider(ClimateControlComponentProvider.INSTANCE, RefrigeratorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registry)
    {
        registry.registerBlockComponent(OvenComponentProvider.INSTANCE, ElectricOvenBlock.class);
        registry.registerBlockComponent(SoupPotComponentProvider.INSTANCE, ElectricSoupPotBlock.class);
        registry.registerBlockComponent(ClimateControlComponentProvider.INSTANCE, ThermostaticAirConditionerBlock.class);
        registry.registerBlockComponent(ClimateControlComponentProvider.INSTANCE, RefrigeratorBlock.class);
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

    private static void addTemperatureLines(ITooltip tooltip, float actualTemperature)
    {
        final MutableComponent heat = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(actualTemperature);
        if (heat != null)
        {
            tooltip.add(heat);
        }
        tooltip.add(Component.translatable("tfc_modern_life.tooltip.current_temperature", Math.round(actualTemperature)));
    }

    private enum OvenComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config)
        {
            final CompoundTag serverData = access.getServerData();
            if (!serverData.isEmpty())
            {
                addTemperatureLines(tooltip, serverData.getFloat(CURRENT_TEMPERATURE));
            }
            else if (access.getBlockEntity() instanceof ElectricOvenBlockEntity oven)
            {
                addTemperatureLines(tooltip, oven.getTemperature());
            }
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor access)
        {
            if (access.getBlockEntity() instanceof ElectricOvenBlockEntity oven)
            {
                data.putFloat(CURRENT_TEMPERATURE, oven.getTemperature());
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return ELECTRIC_OVEN_UID;
        }
    }

    private enum SoupPotComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config)
        {
            final CompoundTag serverData = access.getServerData();
            final float currentTemperature;
            final int recipeTemperature;
            final int total;
            final int progressValue;
            final boolean hasOutput;

            if (!serverData.isEmpty())
            {
                currentTemperature = serverData.getFloat(CURRENT_TEMPERATURE);
                recipeTemperature = serverData.getInt(RECIPE_TEMPERATURE);
                total = serverData.getInt(PROGRESS_TOTAL);
                progressValue = serverData.getInt(PROGRESS);
                hasOutput = serverData.getBoolean(HAS_OUTPUT);
            }
            else if (access.getBlockEntity() instanceof ElectricSoupPotBlockEntity soupPot)
            {
                currentTemperature = soupPot.getTemperature();
                recipeTemperature = soupPot.getDisplayRecipeTemperature();
                total = soupPot.getDisplayProgressTotal();
                progressValue = soupPot.getDisplayProgress();
                hasOutput = soupPot.hasOutput();
            }
            else
            {
                return;
            }

            addTemperatureLines(tooltip, currentTemperature);

            if (recipeTemperature > 0)
            {
                tooltip.add(Component.translatable("tfc_modern_life.jade.recipe_temperature", recipeTemperature));
            }

            final float progress = hasOutput ? 1f : total > 0 ? Mth.clamp((float) progressValue / total, 0f, 1f) : 0f;
            final Component progressText = hasOutput
                ? Component.translatable("tfc_modern_life.jade.done_short")
                : progress > 0f ? Component.literal(Mth.floor(progress * 100f) + "%") : Component.translatable("tfc_modern_life.jade.not_started");
            final IProgressStyle style = IElementHelper.get().progressStyle()
                .color(hasOutput ? 0xFFB85C24 : 0xFF4CAF50, hasOutput ? 0xFFE07B39 : 0xFF86C36B)
                .textColor(0xFFFFFFFF);
            tooltip.add(IElementHelper.get().progress(progress, progressText, style, BoxStyle.DEFAULT, false));
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor access)
        {
            if (access.getBlockEntity() instanceof ElectricSoupPotBlockEntity soupPot)
            {
                data.putFloat(CURRENT_TEMPERATURE, soupPot.getTemperature());
                data.putInt(RECIPE_TEMPERATURE, soupPot.getDisplayRecipeTemperature());
                data.putInt(PROGRESS, soupPot.getDisplayProgress());
                data.putInt(PROGRESS_TOTAL, soupPot.getDisplayProgressTotal());
                data.putBoolean(HAS_OUTPUT, soupPot.hasOutput());
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return ELECTRIC_SOUP_POT_UID;
        }
    }

    private enum ClimateControlComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor access, IPluginConfig config)
        {
            final CompoundTag data = access.getServerData();
            final int energyPerTick;
            final int effectiveSpace;
            final int indoorTemperatureTenths;
            final int structureType;
            final int structureTier;
            final int baseTemperatureDeltaTenths;
            final int setTemperatureTenths;
            final boolean hasSetTemperature;
            final float preservationMultiplier;

            if (!data.isEmpty())
            {
                energyPerTick = data.getInt(ENERGY_PER_TICK);
                effectiveSpace = data.getInt(EFFECTIVE_SPACE);
                indoorTemperatureTenths = data.getInt(INDOOR_TEMPERATURE);
                structureType = data.getInt(STRUCTURE_TYPE);
                structureTier = data.getInt(STRUCTURE_TIER);
                baseTemperatureDeltaTenths = data.getInt(BASE_TEMPERATURE_DELTA);
                setTemperatureTenths = data.getInt(SET_TEMPERATURE);
                hasSetTemperature = data.getBoolean(HAS_SET_TEMPERATURE);
                preservationMultiplier = data.getFloat(PRESERVATION_MULTIPLIER);
            }
            else if (access.getBlockEntity() instanceof ClimateControlBlockEntity control)
            {
                energyPerTick = control.getEnergyPerTick();
                structureType = control.getDisplayStructureTypeForTooltip();
                structureTier = control.getDisplayTierForTooltip();
                baseTemperatureDeltaTenths = control.getDisplayBaseTemperatureDeltaForTooltip();
                if (control instanceof ThermostaticAirConditionerBlockEntity airConditioner)
                {
                    effectiveSpace = airConditioner.getConnectedEffectiveSpace();
                    indoorTemperatureTenths = GreenhouseTemperatureHelper.toTenths(airConditioner.getIndoorTemperature());
                    hasSetTemperature = structureType == ClimateControlBlockEntity.STRUCTURE_GREENHOUSE || structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR;
                    setTemperatureTenths = hasSetTemperature ? GreenhouseTemperatureHelper.toTenths(airConditioner.getTarget()) : 0;
                    preservationMultiplier = airConditioner.getPreservationMultiplier();
                }
                else if (control instanceof RefrigeratorBlockEntity refrigerator)
                {
                    effectiveSpace = refrigerator.getConnectedEffectiveSpace();
                    indoorTemperatureTenths = GreenhouseTemperatureHelper.toTenths(refrigerator.getIndoorTemperature());
                    hasSetTemperature = structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR;
                    setTemperatureTenths = hasSetTemperature ? GreenhouseTemperatureHelper.toTenths(refrigerator.getTarget()) : 0;
                    preservationMultiplier = refrigerator.getPreservationMultiplier();
                }
                else
                {
                    effectiveSpace = 0;
                    indoorTemperatureTenths = 0;
                    hasSetTemperature = false;
                    setTemperatureTenths = 0;
                    preservationMultiplier = 0f;
                }
            }
            else
            {
                return;
            }

            if (structureType == ClimateControlBlockEntity.STRUCTURE_GREENHOUSE)
            {
                tooltip.add(Component.translatable(
                    "tfc_modern_life.jade.greenhouse_with_base_temperature",
                    getStructureName(structureType, structureTier),
                    GreenhouseTemperatureHelper.formatSignedTemperatureDeltaTenths(baseTemperatureDeltaTenths)
                ));
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
                    tooltip.add(Component.translatable("tfc_modern_life.jade.set_temperature", GreenhouseTemperatureHelper.formatTemperatureTenths(setTemperatureTenths)));
                }
                tooltip.add(Component.translatable("tfc_modern_life.jade.indoor_temperature", GreenhouseTemperatureHelper.formatTemperatureTenths(indoorTemperatureTenths)));
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
            if (access.getBlockEntity() instanceof ClimateControlBlockEntity control)
            {
                data.putInt(ENERGY_PER_TICK, control.getEnergyPerTick());
                data.putInt(STRUCTURE_TYPE, control.getDisplayStructureTypeForTooltip());
                data.putInt(STRUCTURE_TIER, control.getDisplayTierForTooltip());
                data.putInt(BASE_TEMPERATURE_DELTA, control.getDisplayBaseTemperatureDeltaForTooltip());
                if (control instanceof ThermostaticAirConditionerBlockEntity airConditioner)
                {
                    data.putInt(EFFECTIVE_SPACE, airConditioner.getConnectedEffectiveSpace());
                    data.putInt(INDOOR_TEMPERATURE, GreenhouseTemperatureHelper.toTenths(airConditioner.getIndoorTemperature()));
                    final boolean hasSetTemperature = control.getDisplayStructureTypeForTooltip() == ClimateControlBlockEntity.STRUCTURE_GREENHOUSE
                        || control.getDisplayStructureTypeForTooltip() == ClimateControlBlockEntity.STRUCTURE_CELLAR;
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
        }

        @Override
        public ResourceLocation getUid()
        {
            return THERMOSTATIC_AIR_CONDITIONER_UID;
        }

        private static Component getStructureName(int structureType, int tier)
        {
            if (structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR)
            {
                final String cellarTierId = switch (tier)
                {
                    case 1 -> "sealed_brick";
                    case 2 -> "stainless_steel_reinforced";
                    case 3 -> "mixed";
                    default -> "custom";
                };
                return Component.translatable("screen.tfc_modern_life.cellar." + cellarTierId);
            }

            final String greenhouseTierId = switch (tier)
            {
                case 1 -> "wood";
                case 2 -> "copper";
                case 3 -> "iron";
                case 4 -> "stainless_steel";
                case 5 -> "mixed";
                default -> "custom";
            };
            return Component.translatable("screen.tfc_modern_life.greenhouse." + greenhouseTierId);
        }
    }

}
