package com.jccy.tfcmodernlife;

import com.jccy.tfcmodernlife.common.ModConfig;
import com.jccy.tfcmodernlife.common.ModCapabilities;
import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.ModContainerTypes;
import com.jccy.tfcmodernlife.common.ModCreativeTab;
import com.jccy.tfcmodernlife.common.ModFoodTraits;
import com.jccy.tfcmodernlife.common.ModSounds;
import com.jccy.tfcmodernlife.common.ModParticles;
import com.jccy.tfcmodernlife.common.ModRecipeSerializers;
import com.jccy.tfcmodernlife.client.ModClientEvents;
import com.jccy.tfcmodernlife.common.block.ThermostaticAirConditionerBlock;
import com.jccy.tfcmodernlife.common.blockentity.ClimateControlBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import com.jccy.tfcmodernlife.common.climate.ClimateControlConfig;
import com.jccy.tfcmodernlife.compat.lso.LegendarySurvivalOverhaulCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(TFCModernLife.MOD_ID)
public final class TFCModernLife
{
    public static final String MOD_ID = "tfc_modern_life";

    public TFCModernLife(ModContainer mod, IEventBus modBus)
    {
        ModFoodTraits.register(modBus);
        ModBlocks.register(modBus);
        ModContainerTypes.register(modBus);
        ModCreativeTab.register(modBus);
        ModParticles.register(modBus);
        mod.registerConfig(Type.COMMON, ModConfig.COMMON_SPEC, MOD_ID + "-common.toml");
        ClimateControlConfig.registerReloadListener(modBus);
        ModSounds.register(modBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(ModCapabilities::register);
        NeoForge.EVENT_BUS.addListener(TFCModernLife::preserveEnergyOnDrops);
        LegendarySurvivalOverhaulCompat.register();

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            if (event.getServer().getTickCount() % 100 == 0)
            {
                event.getServer().getAllLevels().forEach(ClimateStationRegistry::tickLevel);
            }
        });

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            ModClientEvents.register(modBus);
            ModClientEvents.registerConfigScreen(mod);
        }
    }

    private static void preserveEnergyOnDrops(BlockDropsEvent event)
    {
        if (!(event.getBlockEntity() instanceof ClimateControlBlockEntity entity))
        {
            return;
        }
        if (entity instanceof ThermostaticAirConditionerBlockEntity
            && event.getState().hasProperty(ThermostaticAirConditionerBlock.HALF)
            && event.getState().getValue(ThermostaticAirConditionerBlock.HALF) != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
        {
            return;
        }
        final net.minecraft.world.item.Item targetItem = entity instanceof RefrigeratorBlockEntity
            ? ModBlocks.REFRIGERATOR_ITEM.get()
            : entity instanceof ThermostaticAirConditionerBlockEntity
                ? ModBlocks.THERMOSTATIC_AIR_CONDITIONER_ITEM.get()
                : null;
        if (targetItem == null)
        {
            return;
        }
        event.getDrops().stream()
            .map(net.minecraft.world.entity.item.ItemEntity::getItem)
            .filter(stack -> stack.is(targetItem))
            .findFirst()
            .ifPresent(entity::saveEnergyToItem);
    }
}
