package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.eerussianguy.firmalife.common.blocks.greenhouse.ClimateStationBlock;
import com.jccy.tfcmodernlife.common.blockentity.ClimateControlBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ClimateStationRegistry
{
    private static final Map<Level, Set<ClimateStationAccess>> STATIONS = Collections.synchronizedMap(new WeakHashMap<>());

    private ClimateStationRegistry() {}

    public static void register(BlockEntity blockEntity, ClimateStationAccess station)
    {
        final Level level = blockEntity.getLevel();
        if (level == null)
        {
            return;
        }
        synchronized (STATIONS)
        {
            STATIONS.computeIfAbsent(level, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(station);
        }
    }

    public static void unregister(BlockEntity blockEntity, ClimateStationAccess station)
    {
        synchronized (STATIONS)
        {
            final Level level = blockEntity.getLevel();
            if (level != null)
            {
                final Set<ClimateStationAccess> stations = STATIONS.get(level);
                if (stations != null)
                {
                    stations.remove(station);
                    if (stations.isEmpty())
                    {
                        STATIONS.remove(level);
                    }
                }
            }
            else
            {
                STATIONS.values().forEach(stations -> stations.remove(station));
            }
        }
    }

    @Nullable
    public static ClimateStationAccess findControllingGreenhouseStation(Level level, BlockPos targetPos)
    {
        return findControllingStation(level, targetPos, ClimateType.GREENHOUSE, true);
    }

    @Nullable
    public static ClimateStationAccess findControllingCellarStation(Level level, BlockPos targetPos)
    {
        return findControllingStation(level, targetPos, ClimateType.CELLAR, false);
    }

    @Nullable
    public static ClimateStationAccess findRunningCellarRefrigerator(Level level, BlockPos targetPos)
    {
        return findControllingStation(level, targetPos, ClimateType.CELLAR, false, false, null, true, true);
    }

    @Nullable
    public static ClimateStationAccess findGreenhouseStationForDevice(Level level, BlockPos targetPos)
    {
        return findGreenhouseStationForDevice(level, targetPos, null);
    }

    @Nullable
    public static ClimateStationAccess findGreenhouseStationForDevice(Level level, BlockPos targetPos, @Nullable ClimateStationAccess excluded)
    {
        return findControllingStation(level, targetPos, ClimateType.GREENHOUSE, true, true, excluded);
    }

    @Nullable
    public static ClimateStationAccess findCellarStationForDevice(Level level, BlockPos targetPos)
    {
        return findCellarStationForDevice(level, targetPos, null);
    }

    @Nullable
    public static ClimateStationAccess findCellarStationForDevice(Level level, BlockPos targetPos, @Nullable ClimateStationAccess excluded)
    {
        return findControllingStation(level, targetPos, ClimateType.CELLAR, false, true, excluded);
    }

    @Nullable
    public static ClimateStationAccess findAnyStationForDevice(Level level, BlockPos targetPos)
    {
        return findAnyStationForDevice(level, targetPos, null);
    }

    @Nullable
    public static ClimateStationAccess findAnyStationForDevice(Level level, BlockPos targetPos, @Nullable ClimateStationAccess excluded)
    {
        synchronized (STATIONS)
        {
            final Set<ClimateStationAccess> stations = STATIONS.get(level);
            if (stations == null || stations.isEmpty())
            {
                return null;
            }

            final BlockPos soilPos = targetPos.below();
            ClimateStationAccess closestStation = null;
            double closestDistance = Double.MAX_VALUE;
            int closestPriority = Integer.MAX_VALUE;

            final Iterator<ClimateStationAccess> iterator = stations.iterator();
            while (iterator.hasNext())
            {
                final ClimateStationAccess station = iterator.next();
                if (station == excluded)
                {
                    continue;
                }
                if (!(station instanceof BlockEntity blockEntity))
                {
                    iterator.remove();
                    continue;
                }
                if (blockEntity.isRemoved() || blockEntity.getLevel() != level)
                {
                    iterator.remove();
                    continue;
                }
                if (!isActiveStation(blockEntity, station) || !hasStructureData(station))
                {
                    continue;
                }

                final Set<BlockPos> positions = station.tfcml$getClimatePositions();
                if (positions == null || !containsTarget(positions, targetPos, soilPos, true, true))
                {
                    continue;
                }

                final double distance = blockEntity.getBlockPos().distSqr(targetPos);
                final int priority = stationPriority(station);
                if (priority < closestPriority || (priority == closestPriority && distance < closestDistance))
                {
                    closestPriority = priority;
                    closestDistance = distance;
                    closestStation = station;
                }
            }

            if (stations.isEmpty())
            {
                STATIONS.remove(level);
            }
            return closestStation;
        }
    }

    public static boolean isActiveStation(BlockEntity blockEntity, ClimateStationAccess station)
    {
        final Level level = blockEntity.getLevel();
        if (level == null || blockEntity.isRemoved())
        {
            return false;
        }

        final BlockState state = blockEntity.getBlockState();
        if (station instanceof ClimateControlBlockEntity control)
        {
            return control.hasLocalStructureData();
        }
        return station.tfcml$getClimatePositions() != null
            && state.getBlock() instanceof ClimateStationBlock
            && state.hasProperty(ClimateStationBlock.STASIS)
            && state.getValue(ClimateStationBlock.STASIS);
    }

    public static void tickLevel(Level level)
    {
        synchronized (STATIONS)
        {
            final Set<ClimateStationAccess> stations = STATIONS.get(level);
            if (stations == null || stations.isEmpty())
            {
                return;
            }

            final Iterator<ClimateStationAccess> iterator = stations.iterator();
            while (iterator.hasNext())
            {
                final ClimateStationAccess station = iterator.next();
                if (!(station instanceof BlockEntity blockEntity) || blockEntity.isRemoved() || blockEntity.getLevel() != level)
                {
                    iterator.remove();
                    continue;
                }
                if (station.tfcml$getClimateType() == ClimateType.GREENHOUSE && isActiveStation(blockEntity, station))
                {
                    station.tfcml$refreshAutoTemperature(false);
                }
            }

            if (stations.isEmpty())
            {
                STATIONS.remove(level);
            }
        }
    }

    @Nullable
    private static ClimateStationAccess findControllingStation(Level level, BlockPos targetPos, ClimateType climateType, boolean includeBelow)
    {
        return findControllingStation(level, targetPos, climateType, includeBelow, false, null);
    }

    @Nullable
    private static ClimateStationAccess findControllingStation(Level level, BlockPos targetPos, ClimateType climateType, boolean includeBelow, boolean includeAdjacent)
    {
        return findControllingStation(level, targetPos, climateType, includeBelow, includeAdjacent, null);
    }

    @Nullable
    private static ClimateStationAccess findControllingStation(Level level, BlockPos targetPos, ClimateType climateType, boolean includeBelow, boolean includeAdjacent, @Nullable ClimateStationAccess excluded)
    {
        return findControllingStation(level, targetPos, climateType, includeBelow, includeAdjacent, excluded, false, false);
    }

    @Nullable
    private static ClimateStationAccess findControllingStation(Level level, BlockPos targetPos, ClimateType climateType, boolean includeBelow, boolean includeAdjacent, @Nullable ClimateStationAccess excluded, boolean runningOnly, boolean refrigeratorOnly)
    {
        synchronized (STATIONS)
        {
            final Set<ClimateStationAccess> stations = STATIONS.get(level);
            if (stations == null || stations.isEmpty())
            {
                return null;
            }

            final BlockPos soilPos = targetPos.below();
            ClimateStationAccess closestStation = null;
            double closestDistance = Double.MAX_VALUE;
            int closestPriority = Integer.MAX_VALUE;

            final Iterator<ClimateStationAccess> iterator = stations.iterator();
            while (iterator.hasNext())
            {
                final ClimateStationAccess station = iterator.next();
                if (station == excluded)
                {
                    continue;
                }
                if (!(station instanceof BlockEntity blockEntity))
                {
                    iterator.remove();
                    continue;
                }
                if (blockEntity.isRemoved() || blockEntity.getLevel() != level)
                {
                    iterator.remove();
                    continue;
                }
                if (station.tfcml$getClimateType() != climateType || !isActiveStation(blockEntity, station))
                {
                    continue;
                }
                if (runningOnly && (!(station instanceof ClimateControlBlockEntity control) || !control.isRunning()))
                {
                    continue;
                }
                if (refrigeratorOnly && !(station instanceof RefrigeratorBlockEntity))
                {
                    continue;
                }

                final Set<BlockPos> positions = station.tfcml$getClimatePositions();
                if (positions == null || !containsTarget(positions, targetPos, soilPos, includeBelow, includeAdjacent))
                {
                    continue;
                }

                final double distance = blockEntity.getBlockPos().distSqr(targetPos);
                final int priority = stationPriority(station);
                if (priority < closestPriority || (priority == closestPriority && distance < closestDistance))
                {
                    closestPriority = priority;
                    closestDistance = distance;
                    closestStation = station;
                }
            }

            if (stations.isEmpty())
            {
                STATIONS.remove(level);
            }
            return closestStation;
        }
    }

    private static boolean containsTarget(Set<BlockPos> positions, BlockPos targetPos, BlockPos soilPos, boolean includeBelow, boolean includeAdjacent)
    {
        if (positions.contains(targetPos)
            || (includeBelow && (positions.contains(soilPos) || positions.contains(targetPos.above()))))
        {
            return true;
        }
        if (!includeAdjacent)
        {
            return false;
        }
        for (net.minecraft.core.Direction direction : net.dries007.tfc.util.Helpers.DIRECTIONS)
        {
            if (positions.contains(targetPos.relative(direction)))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean hasStructureData(ClimateStationAccess station)
    {
        return station.tfcml$getGreenhouseStructureData() != null || station.tfcml$getCellarStructureData() != null;
    }

    private static int stationPriority(ClimateStationAccess station)
    {
        if (station instanceof ClimateControlBlockEntity control)
        {
            return control.isRunning() ? 0 : 1;
        }
        return 2;
    }
}
