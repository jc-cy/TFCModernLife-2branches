package com.jccy.tfcmodernlife.common.climate;

import com.eerussianguy.firmalife.common.util.GreenhouseType;
import com.jccy.tfcmodernlife.TFCModernLife;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class ClimateControlConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation FIRMALIFE_CLIMATE_STATION_ID = new ResourceLocation("firmalife", "climate_station");
    private static final ResourceLocation THERMOSTATIC_AIR_CONDITIONER_ID = new ResourceLocation(TFCModernLife.MOD_ID, "thermostatic_air_conditioner");
    private static final ResourceLocation REFRIGERATOR_ID = new ResourceLocation(TFCModernLife.MOD_ID, "refrigerator");

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GREENHOUSE_THERMAL_WALLS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GREENHOUSE_SEAL_ONLY_WALLS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> GREENHOUSE_ALWAYS_VALID_WALLS;
    public static ForgeConfigSpec.DoubleValue GREENHOUSE_MINIMUM_THERMAL_COVERAGE;
    public static ForgeConfigSpec.IntValue GREENHOUSE_MINIMUM_THERMAL_BLOCKS;
    public static ForgeConfigSpec.ConfigValue<String> GREENHOUSE_TIER_MODE;
    public static ForgeConfigSpec.DoubleValue GREENHOUSE_RADIUS_MULTIPLIER;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> CELLAR_THERMAL_WALLS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> CELLAR_SEAL_ONLY_WALLS;
    public static ForgeConfigSpec.DoubleValue CELLAR_MINIMUM_THERMAL_COVERAGE;
    public static ForgeConfigSpec.DoubleValue CELLAR_RADIUS_MULTIPLIER;

    private static volatile GreenhouseRuleSet greenhouseRuleSet = GreenhouseRuleSet.empty();
    private static volatile CellarRuleSet cellarRuleSet = CellarRuleSet.empty();

    private ClimateControlConfig() {}

    public static void define(ForgeConfigSpec.Builder builder)
    {
        builder.push("climateControl");

        builder.push("greenhouse");
        GREENHOUSE_RADIUS_MULTIPLIER = builder
            .comment("Multiplier applied to FirmaLife greenhouse search radius.")
            .defineInRange("greenhouseRadiusMultiplier", 3.0d, 0.1d, 16.0d);
        GREENHOUSE_THERMAL_WALLS = builder
            .comment("Insulated greenhouse wall rules. Format: block_or_tag=firmalifeTier[,greenhouseTier]. greenhouseTier values: wood, copper, iron, stainless_steel.")
            .defineListAllowEmpty(List.of("greenhouseThermalWalls"), ClimateControlConfig::defaultGreenhouseThermalWalls, value -> value instanceof String);
        GREENHOUSE_SEAL_ONLY_WALLS = builder
            .comment("Blocks or tags that seal greenhouses but do not provide a tier.")
            .defineListAllowEmpty(List.of("greenhouseSealOnlyWalls"), ClimateControlConfig::defaultGreenhouseSealOnlyWalls, value -> value instanceof String);
        GREENHOUSE_ALWAYS_VALID_WALLS = builder
            .comment("Blocks or tags that can seal a greenhouse without a sturdy-face check.")
            .defineListAllowEmpty(List.of("greenhouseAlwaysValidWalls"), ClimateControlConfig::defaultGreenhouseAlwaysValidWalls, value -> value instanceof String);
        GREENHOUSE_MINIMUM_THERMAL_COVERAGE = builder
            .comment("Minimum insulated shell coverage for a mixed greenhouse.")
            .defineInRange("greenhouseMinimumThermalCoverage", 0.95d, 0d, 1d);
        GREENHOUSE_MINIMUM_THERMAL_BLOCKS = builder
            .comment("Minimum insulated greenhouse shell blocks.")
            .defineInRange("greenhouseMinimumThermalBlocks", 8, 0, 65536);
        GREENHOUSE_TIER_MODE = builder
            .comment("How mixed greenhouse walls resolve tier: weighted_average, minimum, maximum.")
            .define("greenhouseTierMode", "weighted_average");
        builder.pop();

        builder.push("cellar");
        CELLAR_RADIUS_MULTIPLIER = builder
            .comment("Multiplier applied to FirmaLife cellar search radius.")
            .defineInRange("cellarRadiusMultiplier", 2.0d, 0.1d, 16.0d);
        CELLAR_THERMAL_WALLS = builder
            .comment("Insulated cellar wall rules. Format: block_or_tag=sealed_brick|wrought_iron|stainless_steel.")
            .defineListAllowEmpty(List.of("cellarThermalWalls"), ClimateControlConfig::defaultCellarThermalWalls, value -> value instanceof String);
        CELLAR_SEAL_ONLY_WALLS = builder
            .comment("Blocks or tags that seal cellars but do not provide a tier.")
            .defineListAllowEmpty(List.of("cellarSealOnlyWalls"), ClimateControlConfig::defaultCellarSealOnlyWalls, value -> value instanceof String);
        CELLAR_MINIMUM_THERMAL_COVERAGE = builder
            .comment("Minimum insulated shell coverage for a cellar.")
            .defineInRange("cellarMinimumThermalCoverage", 0.95d, 0d, 1d);
        builder.pop();

        builder.pop();
    }

    public static void registerReloadListener(IEventBus modBus)
    {
        modBus.addListener((ModConfigEvent event) -> refreshCaches());
    }

    public static void refreshCaches()
    {
        greenhouseRuleSet = parseGreenhouseRuleSet();
        cellarRuleSet = parseCellarRuleSet();
    }

    @Nullable
    public static GreenhouseWallDefinition getGreenhouseThermalWall(BlockState state)
    {
        return greenhouseRuleSet.findThermalWall(state);
    }

    public static boolean isGreenhouseSealOnlyWall(BlockState state)
    {
        return isClimateControlDevice(state) || greenhouseRuleSet.isSealOnlyWall(state);
    }

    public static boolean isGreenhouseAlwaysValidWall(BlockState state)
    {
        return isClimateControlDevice(state) || greenhouseRuleSet.isAlwaysValidWall(state);
    }

    public static GreenhouseTierMode getGreenhouseTierMode()
    {
        return greenhouseRuleSet.tierMode();
    }

    public static boolean isCellarThermalWall(BlockState state)
    {
        return cellarRuleSet.findThermalWall(state) != null;
    }

    @Nullable
    public static CellarWallDefinition getCellarThermalWall(BlockState state)
    {
        return cellarRuleSet.findThermalWall(state);
    }

    public static boolean isCellarSealOnlyWall(BlockState state)
    {
        return isClimateControlDevice(state) || cellarRuleSet.isSealOnlyWall(state);
    }

    public static boolean isClimateControlSource(BlockState state)
    {
        final ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return FIRMALIFE_CLIMATE_STATION_ID.equals(key)
            || THERMOSTATIC_AIR_CONDITIONER_ID.equals(key)
            || REFRIGERATOR_ID.equals(key);
    }

    public static boolean isClimateControlDevice(BlockState state)
    {
        final ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return THERMOSTATIC_AIR_CONDITIONER_ID.equals(key) || REFRIGERATOR_ID.equals(key);
    }

    private static GreenhouseRuleSet parseGreenhouseRuleSet()
    {
        return new GreenhouseRuleSet(
            List.copyOf(parseGreenhouseWalls(GREENHOUSE_THERMAL_WALLS.get())),
            List.copyOf(parseBlockMatchers(GREENHOUSE_SEAL_ONLY_WALLS.get(), "greenhouseSealOnlyWalls")),
            List.copyOf(parseBlockMatchers(GREENHOUSE_ALWAYS_VALID_WALLS.get(), "greenhouseAlwaysValidWalls")),
            GreenhouseTierMode.parse(GREENHOUSE_TIER_MODE.get())
        );
    }

    private static CellarRuleSet parseCellarRuleSet()
    {
        return new CellarRuleSet(
            List.copyOf(parseCellarWalls(CELLAR_THERMAL_WALLS.get())),
            List.copyOf(parseBlockMatchers(CELLAR_SEAL_ONLY_WALLS.get(), "cellarSealOnlyWalls"))
        );
    }

    private static List<GreenhouseWallDefinition> parseGreenhouseWalls(List<? extends String> entries)
    {
        final List<GreenhouseWallDefinition> parsed = new ArrayList<>();
        final Set<String> seenMatchers = new HashSet<>();
        for (String rawEntry : entries)
        {
            final String entry = rawEntry.trim();
            final int separator = entry.indexOf('=');
            if (separator <= 0 || separator >= entry.length() - 1)
            {
                LOGGER.warn("Ignoring invalid greenhouse wall rule '{}': expected block_or_tag=firmalifeTier[,greenhouseTier]", entry);
                continue;
            }

            final BlockMatcher matcher = parseBlockMatcher(entry.substring(0, separator).trim(), "greenhouseThermalWalls");
            if (matcher == null || !seenMatchers.add(matcher.describe()))
            {
                continue;
            }

            final String[] parts = entry.substring(separator + 1).trim().split(",");
            final int firmalifeTier;
            try
            {
                firmalifeTier = Integer.parseInt(parts[0].trim());
            }
            catch (NumberFormatException e)
            {
                LOGGER.warn("Ignoring invalid greenhouse wall rule '{}': bad firmalife tier", entry);
                continue;
            }
            if (firmalifeTier <= 0)
            {
                LOGGER.warn("Ignoring invalid greenhouse wall rule '{}': tier must be > 0", entry);
                continue;
            }

            final GreenhouseTier greenhouseTier = parts.length >= 2
                ? GreenhouseTier.byId(parts[1].trim())
                : GreenhouseTier.byMatcherDescription(matcher.describe());
            final ResourceLocation greenhouseTypeId = inferGreenhouseTypeId(matcher.describe(), greenhouseTier);
            final String displayNameKey = "screen." + TFCModernLife.MOD_ID + ".greenhouse." + greenhouseTier.id();
            parsed.add(new GreenhouseWallDefinition(firmalifeTier, greenhouseTier, displayNameKey, greenhouseTypeId, matcher));
        }
        return parsed;
    }

    private static List<CellarWallDefinition> parseCellarWalls(List<? extends String> entries)
    {
        final List<CellarWallDefinition> parsed = new ArrayList<>();
        final Set<String> seenMatchers = new HashSet<>();
        for (String rawEntry : entries)
        {
            final String entry = rawEntry.trim();
            final int separator = entry.indexOf('=');
            if (separator <= 0 || separator >= entry.length() - 1)
            {
                LOGGER.warn("Ignoring invalid cellar wall rule '{}': expected block_or_tag=tier", entry);
                continue;
            }

            final BlockMatcher matcher = parseBlockMatcher(entry.substring(0, separator).trim(), "cellarThermalWalls");
            if (matcher == null || !seenMatchers.add(matcher.describe()))
            {
                continue;
            }
            parsed.add(new CellarWallDefinition(CellarTier.byId(entry.substring(separator + 1).trim()), matcher));
        }
        return parsed;
    }

    private static List<BlockMatcher> parseBlockMatchers(List<? extends String> entries, String keyName)
    {
        final List<BlockMatcher> parsed = new ArrayList<>();
        final Set<String> seen = new HashSet<>();
        for (String rawEntry : entries)
        {
            final BlockMatcher matcher = parseBlockMatcher(rawEntry.trim(), keyName);
            if (matcher != null && seen.add(matcher.describe()))
            {
                parsed.add(matcher);
            }
        }
        return parsed;
    }

    @Nullable
    private static BlockMatcher parseBlockMatcher(String token, String keyName)
    {
        if (token.isEmpty())
        {
            return null;
        }
        if (token.charAt(0) == '#')
        {
            final ResourceLocation tagId = parseResourceLocation(token.substring(1).trim());
            if (tagId == null)
            {
                LOGGER.warn("Ignoring invalid {} tag '{}'", keyName, token);
                return null;
            }
            return new TagBlockMatcher(tagId, TagKey.create(Registries.BLOCK, tagId));
        }

        final ResourceLocation blockId = parseResourceLocation(token);
        if (blockId == null)
        {
            LOGGER.warn("Ignoring invalid {} block '{}'", keyName, token);
            return null;
        }
        final Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
        if (block == null)
        {
            LOGGER.warn("Ignoring unknown {} block '{}'", keyName, token);
            return null;
        }
        return new DirectBlockMatcher(blockId, block);
    }

    @Nullable
    private static ResourceLocation parseResourceLocation(String value)
    {
        try
        {
            return new ResourceLocation(value);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }

    private static ResourceLocation inferGreenhouseTypeId(String matcherDescription, GreenhouseTier tier)
    {
        return switch (tier)
        {
            case STAINLESS_STEEL -> new ResourceLocation("firmalife", "stainless_steel");
            case IRON -> new ResourceLocation("firmalife", "iron");
            case COPPER -> new ResourceLocation("firmalife", "copper");
            case WOOD -> new ResourceLocation("firmalife", "treated_wood");
        };
    }

    private static List<String> defaultGreenhouseThermalWalls()
    {
        return List.of(
            "#firmalife:all_treated_wood_greenhouse=5,wood",
            "#firmalife:all_copper_greenhouse=10,copper",
            "#firmalife:all_iron_greenhouse=15,iron",
            "#firmalife:stainless_steel_greenhouse=20,stainless_steel"
        );
    }

    private static List<String> defaultGreenhouseSealOnlyWalls()
    {
        return List.of(
            "tfc_modern_life:thermostatic_air_conditioner",
            "tfc_modern_life:refrigerator",
            "immersiveengineering:capacitor_lv",
            "immersiveengineering:capacitor_mv",
            "immersiveengineering:capacitor_hv"
        );
    }

    private static List<String> defaultGreenhouseAlwaysValidWalls()
    {
        return List.of(
            "#firmalife:always_valid_greenhouse_wall",
            "#minecraft:doors",
            "#minecraft:trapdoors"
        );
    }

    private static List<String> defaultCellarThermalWalls()
    {
        return List.of(
            "#firmalife:cellar_insulation=sealed_brick",
            "#tfc_modern_life:wrought_iron_cellar=wrought_iron",
            "#tfc_modern_life:stainless_steel_cellar=stainless_steel",
            "tfc:metal/block/wrought_iron=wrought_iron",
            "firmalife:metal/block/stainless_steel=stainless_steel"
        );
    }

    private static List<String> defaultCellarSealOnlyWalls()
    {
        return List.of(
            "tfc_modern_life:thermostatic_air_conditioner",
            "tfc_modern_life:refrigerator",
            "immersiveengineering:capacitor_lv",
            "immersiveengineering:capacitor_mv",
            "immersiveengineering:capacitor_hv"
        );
    }

    public enum GreenhouseTierMode
    {
        WEIGHTED_AVERAGE("weighted_average"),
        MINIMUM("minimum"),
        MAXIMUM("maximum");

        private final String configValue;

        GreenhouseTierMode(String configValue)
        {
            this.configValue = configValue;
        }

        public static GreenhouseTierMode parse(String rawValue)
        {
            for (GreenhouseTierMode mode : values())
            {
                if (mode.configValue.equalsIgnoreCase(rawValue))
                {
                    return mode;
                }
            }
            LOGGER.warn("Unknown greenhouseTierMode '{}', falling back to weighted_average", rawValue);
            return WEIGHTED_AVERAGE;
        }
    }

    public record GreenhouseWallDefinition(
        int firmalifeTier,
        GreenhouseTier tier,
        String displayNameKey,
        ResourceLocation greenhouseTypeId,
        BlockMatcher matcher
    )
    {
        public boolean matches(BlockState state)
        {
            return matcher.matches(state);
        }

        @Nullable
        public GreenhouseType greenhouseType()
        {
            return GreenhouseType.get(greenhouseTypeId);
        }
    }

    public record CellarWallDefinition(CellarTier tier, BlockMatcher matcher)
    {
        public boolean matches(BlockState state)
        {
            return matcher.matches(state);
        }
    }

    public interface BlockMatcher
    {
        boolean matches(BlockState state);

        String describe();
    }

    private record DirectBlockMatcher(ResourceLocation id, Block block) implements BlockMatcher
    {
        @Override
        public boolean matches(BlockState state)
        {
            return state.is(block);
        }

        @Override
        public String describe()
        {
            return id.toString();
        }
    }

    private record TagBlockMatcher(ResourceLocation id, TagKey<Block> tag) implements BlockMatcher
    {
        @Override
        public boolean matches(BlockState state)
        {
            return state.is(tag);
        }

        @Override
        public String describe()
        {
            return "#" + id;
        }
    }

    private static final class GreenhouseRuleSet
    {
        private final List<GreenhouseWallDefinition> thermalWalls;
        private final List<BlockMatcher> sealOnlyWalls;
        private final List<BlockMatcher> alwaysValidWalls;
        private final GreenhouseTierMode tierMode;
        private final Map<Block, GreenhouseWallDefinition> thermalCache = new ConcurrentHashMap<>();
        private final Set<Block> noThermalCache = ConcurrentHashMap.newKeySet();
        private final Set<Block> sealOnlyCache = ConcurrentHashMap.newKeySet();
        private final Set<Block> noSealOnlyCache = ConcurrentHashMap.newKeySet();
        private final Set<Block> alwaysValidCache = ConcurrentHashMap.newKeySet();
        private final Set<Block> noAlwaysValidCache = ConcurrentHashMap.newKeySet();

        private GreenhouseRuleSet(List<GreenhouseWallDefinition> thermalWalls, List<BlockMatcher> sealOnlyWalls, List<BlockMatcher> alwaysValidWalls, GreenhouseTierMode tierMode)
        {
            this.thermalWalls = thermalWalls;
            this.sealOnlyWalls = sealOnlyWalls;
            this.alwaysValidWalls = alwaysValidWalls;
            this.tierMode = tierMode;
        }

        private static GreenhouseRuleSet empty()
        {
            return new GreenhouseRuleSet(List.of(), List.of(), List.of(), GreenhouseTierMode.WEIGHTED_AVERAGE);
        }

        @Nullable
        private GreenhouseWallDefinition findThermalWall(BlockState state)
        {
            final Block block = state.getBlock();
            final GreenhouseWallDefinition cached = thermalCache.get(block);
            if (cached != null)
            {
                return cached;
            }
            if (noThermalCache.contains(block))
            {
                return null;
            }
            for (GreenhouseWallDefinition rule : thermalWalls)
            {
                if (rule.matches(state))
                {
                    thermalCache.put(block, rule);
                    return rule;
                }
            }
            noThermalCache.add(block);
            return null;
        }

        private boolean isSealOnlyWall(BlockState state)
        {
            return matchesBooleanRule(state, sealOnlyWalls, sealOnlyCache, noSealOnlyCache);
        }

        private boolean isAlwaysValidWall(BlockState state)
        {
            return matchesBooleanRule(state, alwaysValidWalls, alwaysValidCache, noAlwaysValidCache);
        }

        private GreenhouseTierMode tierMode()
        {
            return tierMode;
        }
    }

    private static final class CellarRuleSet
    {
        private final List<CellarWallDefinition> thermalWalls;
        private final List<BlockMatcher> sealOnlyWalls;
        private final Map<Block, CellarWallDefinition> thermalCache = new ConcurrentHashMap<>();
        private final Set<Block> noThermalCache = ConcurrentHashMap.newKeySet();
        private final Set<Block> sealOnlyCache = ConcurrentHashMap.newKeySet();
        private final Set<Block> noSealOnlyCache = ConcurrentHashMap.newKeySet();

        private CellarRuleSet(List<CellarWallDefinition> thermalWalls, List<BlockMatcher> sealOnlyWalls)
        {
            this.thermalWalls = thermalWalls;
            this.sealOnlyWalls = sealOnlyWalls;
        }

        private static CellarRuleSet empty()
        {
            return new CellarRuleSet(List.of(), List.of());
        }

        @Nullable
        private CellarWallDefinition findThermalWall(BlockState state)
        {
            final Block block = state.getBlock();
            final CellarWallDefinition cached = thermalCache.get(block);
            if (cached != null)
            {
                return cached;
            }
            if (noThermalCache.contains(block))
            {
                return null;
            }
            for (CellarWallDefinition rule : thermalWalls)
            {
                if (rule.matches(state))
                {
                    thermalCache.put(block, rule);
                    return rule;
                }
            }
            noThermalCache.add(block);
            return null;
        }

        private boolean isSealOnlyWall(BlockState state)
        {
            return matchesBooleanRule(state, sealOnlyWalls, sealOnlyCache, noSealOnlyCache);
        }
    }

    private static boolean matchesBooleanRule(BlockState state, List<BlockMatcher> rules, Set<Block> positiveCache, Set<Block> negativeCache)
    {
        final Block block = state.getBlock();
        if (positiveCache.contains(block))
        {
            return true;
        }
        if (negativeCache.contains(block))
        {
            return false;
        }
        for (BlockMatcher matcher : rules)
        {
            if (matcher.matches(state))
            {
                positiveCache.add(block);
                return true;
            }
        }
        negativeCache.add(block);
        return false;
    }
}
