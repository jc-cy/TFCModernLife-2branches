package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.util.GreenhouseType;
import com.eerussianguy.firmalife.common.util.Mechanics;
import com.eerussianguy.firmalife.config.FLConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public final class MixedGreenhouseDetector
{
    private static final String MIXED_NAME_KEY = "screen.tfc_modern_life.greenhouse.mixed";
    private static final String CUSTOM_NAME_KEY = "screen.tfc_modern_life.greenhouse.custom";
    private static final String MIXED_FOUND_KEY = "greenhouse.tfc_modern_life.mixed";
    private static final String CUSTOM_FOUND_KEY = "greenhouse.tfc_modern_life.custom";
    private static final Direction[] DEVICE_START_DIRECTIONS = {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN
    };

    private MixedGreenhouseDetector() {}

    @Nullable
    public static Result detect(Level level, BlockPos stationPos)
    {
        return detect(level, stationPos, false);
    }

    @Nullable
    public static Result detectFromDevice(Level level, BlockPos devicePos)
    {
        final Result sourceResult = tryDetectFrom(level, devicePos, devicePos);
        if (sourceResult != null)
        {
            return sourceResult;
        }

        for (Direction direction : DEVICE_START_DIRECTIONS)
        {
            final BlockPos startPos = devicePos.relative(direction);
            final BlockState startState = level.getBlockState(startPos);
            if (!isInteriorCandidate(startState))
            {
                continue;
            }
            final Result result = tryDetectFrom(level, devicePos, startPos);
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }

    @Nullable
    private static Result detect(Level level, BlockPos stationPos, boolean force)
    {
        final boolean shouldTry = hasAdjacentWall(level, stationPos) || shouldRetryFromStationState(level, stationPos);
        if (!force && !shouldTry)
        {
            return null;
        }

        return tryDetectFrom(level, stationPos, stationPos);
    }

    @Nullable
    private static Result tryDetectFrom(Level level, BlockPos originPos, BlockPos startPos)
    {
        final int radius = Math.min(128, Mth.ceil(FLConfig.SERVER.greenhouseRadius.get() * ClimateControlConfig.GREENHOUSE_RADIUS_MULTIPLIER.get()));
        final BoundingBox box = new BoundingBox(originPos).inflatedBy(radius);
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        final ShellAccumulator accumulator = new ShellAccumulator(level);
        final Set<BlockPos> positions = Mechanics.floodfill(
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
        if (positions.isEmpty() || !accumulator.isValid())
        {
            return null;
        }
        return accumulator.createResult(positions);
    }

    private static boolean isInteriorCandidate(BlockState state)
    {
        return !ClimateControlConfig.isClimateControlSource(state)
            && ClimateControlConfig.getGreenhouseThermalWall(state) == null
            && !ClimateControlConfig.isGreenhouseSealOnlyWall(state)
            && !ClimateControlConfig.isGreenhouseAlwaysValidWall(state);
    }

    private static boolean hasAdjacentWall(Level level, BlockPos stationPos)
    {
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Helpers.DIRECTIONS)
        {
            mutable.setWithOffset(stationPos, direction);
            if (ClimateControlConfig.getGreenhouseThermalWall(level.getBlockState(mutable)) != null
                || ClimateControlConfig.isGreenhouseSealOnlyWall(level.getBlockState(mutable)))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldRetryFromStationState(Level level, BlockPos stationPos)
    {
        if (!(level.getBlockEntity(stationPos) instanceof ClimateStationAccess station))
        {
            return false;
        }
        return station.tfcml$hasFavoriteGreenhouseType()
            || (station.tfcml$getClimateType() == com.eerussianguy.firmalife.common.blockentities.ClimateType.GREENHOUSE && station.tfcml$getGreenhouseTier() > 0);
    }

    public record Result(Set<BlockPos> positions, int firmalifeTier, GreenhouseStructureData structureData, @Nullable GreenhouseType representativeType)
    {
        public Component foundTitle()
        {
            if (representativeType != null)
            {
                return representativeType.getTitle();
            }
            return Component.translatable(structureData.mixedThermalWalls() ? MIXED_FOUND_KEY : CUSTOM_FOUND_KEY);
        }

        public boolean isRepresentativeStainless()
        {
            return structureData.tier() == GreenhouseTier.STAINLESS_STEEL;
        }
    }

    private static final class ShellAccumulator
    {
        private final Level level;
        private final Map<ClimateControlConfig.GreenhouseWallDefinition, Integer> thermalCounts = new LinkedHashMap<>();
        private final Set<BlockPos> shellBlocks = new java.util.HashSet<>();
        private final Set<BlockPos> thermalBlocks = new java.util.HashSet<>();

        private ShellAccumulator(Level level)
        {
            this.level = level;
        }

        private boolean testWall(BlockState wallState, BlockPos wallPos, Direction direction)
        {
            if (direction == Direction.DOWN)
            {
                return !wallState.isAir();
            }
            if (ClimateControlConfig.isClimateControlSource(wallState))
            {
                return true;
            }

            final ClimateControlConfig.GreenhouseWallDefinition thermalRule = ClimateControlConfig.getGreenhouseThermalWall(wallState);
            final boolean alwaysValid = ClimateControlConfig.isGreenhouseAlwaysValidWall(wallState);
            final boolean sealOnly = ClimateControlConfig.isGreenhouseSealOnlyWall(wallState);
            if (thermalRule == null && !sealOnly && !alwaysValid)
            {
                return false;
            }

            if (!alwaysValid && !(direction == Direction.UP && wallState.getBlock() instanceof SlabBlock) && !wallState.isFaceSturdy(level, wallPos, direction.getOpposite()))
            {
                return false;
            }

            final BlockPos immutablePos = wallPos.immutable();
            shellBlocks.add(immutablePos);
            if (thermalRule != null && thermalBlocks.add(immutablePos))
            {
                thermalCounts.merge(thermalRule, 1, Integer::sum);
            }
            return true;
        }

        private boolean isValid()
        {
            if (thermalCounts.isEmpty() || shellBlocks.isEmpty())
            {
                return false;
            }
            if (thermalBlocks.size() < ClimateControlConfig.GREENHOUSE_MINIMUM_THERMAL_BLOCKS.get())
            {
                return false;
            }
            return coverage() >= ClimateControlConfig.GREENHOUSE_MINIMUM_THERMAL_COVERAGE.get();
        }

        private double coverage()
        {
            return shellBlocks.isEmpty() ? 0d : thermalBlocks.size() / (double) shellBlocks.size();
        }

        private Result createResult(Set<BlockPos> positions)
        {
            final boolean mixed = thermalCounts.size() > 1;
            final ClimateControlConfig.GreenhouseWallDefinition representativeRule = findRepresentativeRule();
            final int firmalifeTier = computeFirmalifeTier();
            final GreenhouseTier tier = computeGreenhouseTier();

            final String displayNameKey;
            final GreenhouseType representativeType;
            if (!mixed && representativeRule != null)
            {
                displayNameKey = representativeRule.displayNameKey();
                representativeType = representativeRule.greenhouseType();
            }
            else
            {
                displayNameKey = mixed ? MIXED_NAME_KEY : CUSTOM_NAME_KEY;
                representativeType = null;
            }

            return new Result(
                new java.util.HashSet<>(positions),
                firmalifeTier,
                new GreenhouseStructureData(displayNameKey, positions.size(), firmalifeTier, tier, shellBlocks.size(), thermalBlocks.size(), mixed),
                representativeType
            );
        }

        @Nullable
        private ClimateControlConfig.GreenhouseWallDefinition findRepresentativeRule()
        {
            ClimateControlConfig.GreenhouseWallDefinition representativeRule = null;
            int bestCount = -1;
            for (Map.Entry<ClimateControlConfig.GreenhouseWallDefinition, Integer> entry : thermalCounts.entrySet())
            {
                if (entry.getValue() > bestCount)
                {
                    representativeRule = entry.getKey();
                    bestCount = entry.getValue();
                }
            }
            return representativeRule;
        }

        private int computeFirmalifeTier()
        {
            return switch (ClimateControlConfig.getGreenhouseTierMode())
            {
                case MINIMUM -> thermalCounts.keySet().stream().mapToInt(ClimateControlConfig.GreenhouseWallDefinition::firmalifeTier).min().orElse(0);
                case MAXIMUM -> thermalCounts.keySet().stream().mapToInt(ClimateControlConfig.GreenhouseWallDefinition::firmalifeTier).max().orElse(0);
                case WEIGHTED_AVERAGE -> {
                    int total = 0;
                    int weighted = 0;
                    for (Map.Entry<ClimateControlConfig.GreenhouseWallDefinition, Integer> entry : thermalCounts.entrySet())
                    {
                        weighted += entry.getKey().firmalifeTier() * entry.getValue();
                        total += entry.getValue();
                    }
                    yield total <= 0 ? 0 : Math.round(weighted / (float) total);
                }
            };
        }

        private GreenhouseTier computeGreenhouseTier()
        {
            return switch (ClimateControlConfig.getGreenhouseTierMode())
            {
                case MINIMUM -> thermalCounts.keySet().stream().map(ClimateControlConfig.GreenhouseWallDefinition::tier).min(java.util.Comparator.comparingInt(Enum::ordinal)).orElse(GreenhouseTier.WOOD);
                case MAXIMUM -> thermalCounts.keySet().stream().map(ClimateControlConfig.GreenhouseWallDefinition::tier).max(java.util.Comparator.comparingInt(Enum::ordinal)).orElse(GreenhouseTier.WOOD);
                case WEIGHTED_AVERAGE -> GreenhouseTier.byFirmalifeTier(computeFirmalifeTier());
            };
        }
    }
}
