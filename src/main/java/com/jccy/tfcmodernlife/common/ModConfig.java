package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.climate.ClimateControlConfig;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfig
{
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> JARRING_STATION_INSERT_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> JARRING_STATION_EXTRACT_BLACKLIST;
    public static final ForgeConfigSpec.IntValue ELECTRIC_OVEN_ENERGY_PER_TICK;
    public static final ForgeConfigSpec.IntValue ELECTRIC_SOUP_POT_ENERGY_PER_TICK;

    static
    {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.translation(configTranslationKey("automation")).push("automation");
        builder.translation(configTranslationKey("automation.firmalifeJarringStation")).push("firmalifeJarringStation");
        JARRING_STATION_INSERT_WHITELIST = builder
            .translation(configTranslationKey("automation.firmalifeJarringStation.insertWhitelist"))
            .comment("允许自动化输入 FirmaLife 装罐台的物品: 物品 id（如 'minecraft:apple'）或物品标签（如 '#tfc:empty_jar_with_lid'）。",
                "Items or item tags allowed to be inserted into FirmaLife jarring stations by automation. Use item ids like 'minecraft:apple' or tags like '#tfc:empty_jar_with_lid'.")
            .defineListAllowEmpty(List.of("insertWhitelist"), () -> List.of("tfc:empty_jar_with_lid"), ModConfig::isConfigItemEntry);
        JARRING_STATION_EXTRACT_BLACKLIST = builder
            .translation(configTranslationKey("automation.firmalifeJarringStation.extractBlacklist"))
            .comment("禁止自动化从 FirmaLife 装罐台抽出的物品: 物品 id 或物品标签（如 '#tfc:empty_jar_with_lid'）。",
                "Items or item tags blocked from automation extraction from FirmaLife jarring stations. Use item ids like 'minecraft:apple' or tags like '#tfc:empty_jar_with_lid'.")
            .defineListAllowEmpty(List.of("extractBlacklist"), () -> List.of("#tfc:empty_jar_with_lid"), ModConfig::isConfigItemEntry);
        builder.pop();
        builder.pop();

        builder.translation(configTranslationKey("power")).push("power");
        builder.translation(configTranslationKey("power.electricKitchen")).push("electricKitchen");
        ELECTRIC_OVEN_ENERGY_PER_TICK = builder
            .translation(configTranslationKey("power.electricKitchen.electricOvenEnergyPerTick"))
            .comment("电热烤箱通电时每 tick 消耗的 FE。设为 0 则烤箱加热不耗电。",
                "FE consumed per tick while the Electric Oven is powered. Set to 0 to make oven heating free.")
            .defineInRange("electricOvenEnergyPerTick", 20, 0, 1_000_000);
        ELECTRIC_SOUP_POT_ENERGY_PER_TICK = builder
            .translation(configTranslationKey("power.electricKitchen.electricSoupPotEnergyPerTick"))
            .comment("电热高压锅通电时每 tick 消耗的 FE。设为 0 则该设备加热不耗电。",
                "FE consumed per tick while the Electric Soup Pot is powered. Set to 0 to make pot heating free.")
            .defineInRange("electricSoupPotEnergyPerTick", 20, 0, 1_000_000);
        builder.pop();
        builder.pop();

        ClimateControlConfig.define(builder);

        COMMON_SPEC = builder.build();
    }

    private ModConfig()
    {
    }

    /**
     * 游戏内配置界面的翻译键前缀。值键与值自身的路径一一对应（category 与值共用前缀），
     * 具体文案见 assets/tfc_modern_life/lang/*.json 里的 "tfc_modern_life.config.*"。
     */
    public static String configTranslationKey(String path)
    {
        return TFCModernLife.MOD_ID + ".config." + path;
    }

    private static boolean isConfigItemEntry(Object value)
    {
        return value instanceof String text && !text.isBlank();
    }
}
