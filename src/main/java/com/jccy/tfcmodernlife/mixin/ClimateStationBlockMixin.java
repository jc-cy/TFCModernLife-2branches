package com.jccy.tfcmodernlife.mixin;

import com.eerussianguy.firmalife.common.blockentities.ClimateStationBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.eerussianguy.firmalife.common.blocks.greenhouse.ClimateStationBlock;
import com.eerussianguy.firmalife.common.util.FLAdvancements;
import com.eerussianguy.firmalife.common.util.GreenhouseType;
import com.eerussianguy.firmalife.common.util.Mechanics;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import com.jccy.tfcmodernlife.common.climate.ConfiguredCellarDetector;
import com.jccy.tfcmodernlife.common.climate.MixedGreenhouseDetector;
import com.mojang.datafixers.util.Either;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClimateStationBlock.class, remap = false)
public abstract class ClimateStationBlockMixin
{
    @Inject(method = "check", at = @At("HEAD"), cancellable = true, require = 0)
    private static void tfcml$replaceClimateCheck(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Either<Mechanics.GreenhouseInfo, Set<BlockPos>>> cir)
    {
        final StructureCheckResult result = tfcml$checkStructure(level, pos, state);
        if (result == null)
        {
            cir.setReturnValue(null);
            return;
        }

        if (result.greenhouseResult() != null)
        {
            final GreenhouseType representativeType = result.greenhouseResult().representativeType();
            final GreenhouseType displayType = representativeType != null ? representativeType : tfcml$getFallbackGreenhouseType();
            cir.setReturnValue(displayType != null
                ? Either.left(new Mechanics.GreenhouseInfo(displayType, result.greenhouseResult().positions()))
                : null);
            return;
        }

        cir.setReturnValue(Either.right(result.cellarResult().positions()));
    }

    @Inject(method = {"use", "m_6227_"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void tfcml$useClimateCheck(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir)
    {
        final boolean willConsumeAction = level.getBlockEntity(pos) instanceof ClimateStationBlockEntity station && station.setFavorite(player.getItemInHand(hand));
        final StructureCheckResult result = tfcml$checkStructure(level, pos, state);
        if (result == null)
        {
            cir.setReturnValue(willConsumeAction ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS);
            return;
        }

        if (!level.isClientSide())
        {
            if (result.greenhouseResult() != null)
            {
                final MixedGreenhouseDetector.Result greenhouse = result.greenhouseResult();
                if (greenhouse.positions().size() > 200 && player instanceof ServerPlayer server && greenhouse.isRepresentativeStainless())
                {
                    FLAdvancements.BIG_STAINLESS_GREENHOUSE.trigger(server);
                }
                player.displayClientMessage(Component.translatable("firmalife.greenhouse.found", greenhouse.foundTitle(), greenhouse.positions().size()), true);
            }
            else if (result.cellarResult() != null)
            {
                if (result.cellarResult().positions().size() > 200 && player instanceof ServerPlayer server)
                {
                    FLAdvancements.BIG_CELLAR.trigger(server);
                }
                player.displayClientMessage(Component.translatable("firmalife.cellar.found", result.cellarResult().positions().size()), true);
            }
        }

        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }

    @Inject(method = "addHoeOverlayInfo", at = @At("TAIL"))
    private void tfcml$addClimateTooltip(Level level, BlockPos pos, BlockState state, List<Component> tooltip, boolean debug, CallbackInfo ci)
    {
        if (level.getBlockEntity(pos) instanceof ClimateStationBlockEntity blockEntity && blockEntity instanceof ClimateStationAccess station)
        {
            if (!com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry.isActiveStation(blockEntity, station))
            {
                tooltip.add(Component.translatable("screen.tfc_modern_life.climate_station.inactive"));
                return;
            }
            if (station.tfcml$getClimateType() == ClimateType.GREENHOUSE && station.tfcml$getGreenhouseStructureData() != null)
            {
                tooltip.add(Component.translatable(
                    "tfc_modern_life.tooltip.greenhouse_with_base_temperature",
                    Component.translatable(station.tfcml$getGreenhouseStructureData().displayNameKey()),
                    com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper.formatSignedTemperatureDelta(
                        com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper.getGreenhouseBaseTemperatureDelta(level, station)
                    )
                ));
            }
            else if (station.tfcml$getClimateType() == ClimateType.CELLAR && station.tfcml$getCellarStructureData() != null)
            {
                tooltip.add(Component.translatable("tfc_modern_life.tooltip.cellar_detected"));
            }
        }
    }

    @Unique
    @Nullable
    private static StructureCheckResult tfcml$checkStructure(Level level, BlockPos pos, BlockState state)
    {
        final MixedGreenhouseDetector.Result greenhouse = MixedGreenhouseDetector.detect(level, pos);
        if (greenhouse != null)
        {
            if (level.getBlockEntity(pos) instanceof ClimateStationBlockEntity station)
            {
                if (station instanceof ClimateStationAccess access)
                {
                    access.tfcml$setGreenhouseStructureData(greenhouse.structureData());
                    access.tfcml$setCellarStructureData(null);
                    ClimateStationRegistry.register(station, access);
                    if (greenhouse.representativeType() == null)
                    {
                        access.tfcml$clearFavoriteClimateHints();
                    }
                }
                if (greenhouse.representativeType() != null)
                {
                    station.setFavorite(greenhouse.representativeType());
                }
                station.setPositions(new HashSet<>(greenhouse.positions()));
                station.updateValidity(true, greenhouse.firmalifeTier());
                station.setType(ClimateType.GREENHOUSE);
            }
            tfcml$updateState(level, pos, state, true);
            return new StructureCheckResult(greenhouse, null);
        }

        final ConfiguredCellarDetector.Result cellar = ConfiguredCellarDetector.detect(level, pos);
        if (cellar != null)
        {
            if (level.getBlockEntity(pos) instanceof ClimateStationBlockEntity station)
            {
                if (station instanceof ClimateStationAccess access)
                {
                    access.tfcml$setGreenhouseStructureData(null);
                    access.tfcml$setCellarStructureData(cellar.structureData());
                    ClimateStationRegistry.register(station, access);
                }
                station.setPositions(new HashSet<>(cellar.positions()));
                station.updateValidity(true, 0);
                station.setType(ClimateType.CELLAR);
            }
            tfcml$updateState(level, pos, state, true);
            return new StructureCheckResult(null, cellar);
        }

        tfcml$denyAll(level, pos);
        tfcml$updateState(level, pos, state, false);
        return null;
    }

    @Unique
    private static void tfcml$denyAll(Level level, BlockPos pos)
    {
        if (level.getBlockEntity(pos) instanceof ClimateStationBlockEntity station)
        {
            if (station instanceof ClimateStationAccess access)
            {
                access.tfcml$setGreenhouseStructureData(null);
                access.tfcml$setCellarStructureData(null);
            }
            station.updateValidity(false, 0);
        }
    }

    @Unique
    private static void tfcml$updateState(Level level, BlockPos pos, BlockState state, boolean valid)
    {
        final Boolean currentValue = state.getOptionalValue(ClimateStationBlock.STASIS).orElse(false);
        if (currentValue != valid)
        {
            level.setBlockAndUpdate(pos, state.setValue(ClimateStationBlock.STASIS, valid));
        }
    }

    @Unique
    @Nullable
    private static GreenhouseType tfcml$getFallbackGreenhouseType()
    {
        GreenhouseType type = GreenhouseType.get(new ResourceLocation("firmalife", "treated_wood"));
        if (type != null)
        {
            return type;
        }
        for (GreenhouseType candidate : GreenhouseType.MANAGER.getValues())
        {
            return candidate;
        }
        return null;
    }

    @Unique
    private record StructureCheckResult(@Nullable MixedGreenhouseDetector.Result greenhouseResult, @Nullable ConfiguredCellarDetector.Result cellarResult) {}
}
