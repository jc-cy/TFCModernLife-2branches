package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.common.climate.ClimateControlConfig;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModConfig
{
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> JARRING_STATION_INSERT_WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> JARRING_STATION_EXTRACT_BLACKLIST;
    public static final ModConfigSpec.IntValue ELECTRIC_OVEN_ENERGY_PER_TICK;
    public static final ModConfigSpec.IntValue ELECTRIC_SOUP_POT_ENERGY_PER_TICK;

    static
    {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("automation");
        builder.push("firmalifeJarringStation");
        JARRING_STATION_INSERT_WHITELIST = builder
            .comment("Items or item tags allowed to be inserted into FirmaLife jarring stations by automation. Use item ids like 'minecraft:apple' or tags like '#tfc:empty_jar_with_lid'.")
            .defineListAllowEmpty(List.of("insertWhitelist"), () -> List.of("tfc:empty_jar_with_lid"), ModConfig::isConfigItemEntry);
        JARRING_STATION_EXTRACT_BLACKLIST = builder
            .comment("Items or item tags blocked from automation extraction from FirmaLife jarring stations. Use item ids like 'minecraft:apple' or tags like '#tfc:empty_jar_with_lid'.")
            .defineListAllowEmpty(List.of("extractBlacklist"), () -> List.of("#tfc:empty_jar_with_lid"), ModConfig::isConfigItemEntry);
        builder.pop();
        builder.pop();

        builder.push("power");
        builder.push("electricKitchen");
        ELECTRIC_OVEN_ENERGY_PER_TICK = builder
            .comment("FE consumed per tick while the Electric Oven is powered. Set to 0 to make oven heating free.")
            .defineInRange("electricOvenEnergyPerTick", 20, 0, 1_000_000);
        ELECTRIC_SOUP_POT_ENERGY_PER_TICK = builder
            .comment("FE consumed per tick while the Electric Soup Pot is powered. Set to 0 to make pot heating free.")
            .defineInRange("electricSoupPotEnergyPerTick", 20, 0, 1_000_000);
        builder.pop();
        builder.pop();

        ClimateControlConfig.define(builder);

        COMMON_SPEC = builder.build();
    }

    private ModConfig()
    {
    }

    private static boolean isConfigItemEntry(Object value)
    {
        return value instanceof String text && !text.isBlank();
    }
}
