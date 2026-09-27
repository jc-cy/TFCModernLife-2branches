package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.blockentities.ClimateStationBlockEntity;
import com.eerussianguy.firmalife.common.util.Mechanics;
import com.eerussianguy.firmalife.config.FLConfig;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public final class ConfiguredCellarDetector
{
    private static final Direction[] DEVICE_START_DIRECTIONS = {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN
    };

    private ConfiguredCellarDetector() {}

    @Nullable
    public static Result detect(Level level, BlockPos pos)
    {
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Helpers.DIRECTIONS)
        {
            mutable.setWithOffset(pos, direction);
            if (ClimateControlConfig.isCellarThermalWall(level.getBlockState(mutable)) || ClimateControlConfig.isCellarSealOnlyWall(level.getBlockState(mutable)))
            {
                return tryFindCellar(level, pos, mutable);
            }
        }
        if (level.getBlockEntity(pos) instanceof ClimateStationBlockEntity station && station.favoriteIsCellar())
        {
            return tryFindCellar(level, pos, mutable);
        }
        return null;
    }

    @Nullable
    public static Result detectFromDevice(Level level, BlockPos pos)
    {
        final Result sourceResult = tryFindCellar(level, pos, pos, new BlockPos.MutableBlockPos());
        if (sourceResult != null)
        {
            return sourceResult;
        }

        for (Direction direction : DEVICE_START_DIRECTIONS)
        {
            final BlockPos startPos = pos.relative(direction);
            final BlockState startState = level.getBlockState(startPos);
            if (!isInteriorCandidate(startState))
            {
                continue;
            }
            final Result result = tryFindCellar(level, pos, startPos, new BlockPos.MutableBlockPos());
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }

    @Nullable
    private static Result tryFindCellar(Level level, BlockPos pos, BlockPos.MutableBlockPos mutable)
    {
        return tryFindCellar(level, pos, pos, mutable);
    }

    @Nullable
    private static Result tryFindCellar(Level level, BlockPos originPos, BlockPos startPos, BlockPos.MutableBlockPos mutable)
    {
        final int radius = Math.min(128, Mth.ceil(FLConfig.SERVER.cellarRadius.get() * ClimateControlConfig.CELLAR_RADIUS_MULTIPLIER.get()));
        final BoundingBox box = new BoundingBox(originPos).inflatedBy(radius);
        final ShellAccumulator accumulator = new ShellAccumulator();
        final Set<BlockPos> filled = Mechanics.floodfill(
            level,
            startPos,
            mutable,
            box,
            accumulator::testWall,
            state -> !ClimateControlConfig.isClimateControlSource(state),
            false,
            -1,
            Helpers.DIRECTIONS
        );
        if (filled.isEmpty() || !accumulator.isValid())
        {
            return null;
        }
        return accumulator.createResult(filled);
    }

    private static boolean isInteriorCandidate(BlockState state)
    {
        return !ClimateControlConfig.isClimateControlSource(state)
            && !ClimateControlConfig.isCellarThermalWall(state)
            && !ClimateControlConfig.isCellarSealOnlyWall(state);
    }

    public record Result(Set<BlockPos> positions, CellarStructureData structureData) {}

    private static final class ShellAccumulator
    {
        private final Set<BlockPos> shellBlocks = new HashSet<>();
        private final Set<BlockPos> thermalBlocks = new HashSet<>();
        private final Map<CellarTier, Integer> thermalCounts = new LinkedHashMap<>();

        private boolean testWall(BlockState wallState, BlockPos wallPos, Direction direction)
        {
            if (ClimateControlConfig.isClimateControlSource(wallState))
            {
                return true;
            }

            final ClimateControlConfig.CellarWallDefinition thermalRule = ClimateControlConfig.getCellarThermalWall(wallState);
            final boolean sealOnly = ClimateControlConfig.isCellarSealOnlyWall(wallState);
            if (thermalRule == null && !sealOnly)
            {
                return false;
            }

            final BlockPos immutablePos = wallPos.immutable();
            shellBlocks.add(immutablePos);
            if (thermalRule != null)
            {
                if (thermalBlocks.add(immutablePos))
                {
                    thermalCounts.merge(thermalRule.tier(), 1, Integer::sum);
                }
            }
            return true;
        }

        private boolean isValid()
        {
            if (thermalBlocks.isEmpty() || shellBlocks.isEmpty())
            {
                return false;
            }
            return coverage() >= ClimateControlConfig.CELLAR_MINIMUM_THERMAL_COVERAGE.get();
        }

        private double coverage()
        {
            return shellBlocks.isEmpty() ? 0d : thermalBlocks.size() / (double) shellBlocks.size();
        }

        private Result createResult(Set<BlockPos> positions)
        {
            final CellarTier tier = findDominantTier();
            final int totalThermal = thermalCounts.values().stream().mapToInt(Integer::intValue).sum();
            final boolean mixed = thermalCounts.size() > 1;
            float minimumTemperature = 0f;
            for (Map.Entry<CellarTier, Integer> entry : thermalCounts.entrySet())
            {
                minimumTemperature += entry.getKey().minimumTemperature() * entry.getValue();
            }
            if (totalThermal > 0)
            {
                minimumTemperature /= totalThermal;
            }
            else
            {
                minimumTemperature = tier.minimumTemperature();
            }
            return new Result(
                new HashSet<>(positions),
                new CellarStructureData(positions.size(), tier, shellBlocks.size(), thermalBlocks.size(), mixed, minimumTemperature)
            );
        }

        private CellarTier findDominantTier()
        {
            CellarTier dominant = CellarTier.SEALED_BRICK;
            int bestCount = -1;
            for (Map.Entry<CellarTier, Integer> entry : thermalCounts.entrySet())
            {
                if (entry.getValue() > bestCount || (entry.getValue() == bestCount && entry.getKey().powerMultiplier() > dominant.powerMultiplier()))
                {
                    dominant = entry.getKey();
                    bestCount = entry.getValue();
                }
            }
            return dominant;
        }
    }
}
