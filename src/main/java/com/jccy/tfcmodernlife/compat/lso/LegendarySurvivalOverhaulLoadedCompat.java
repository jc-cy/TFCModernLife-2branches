package com.jccy.tfcmodernlife.compat.lso;

import sfiomn.legendarysurvivaloverhaul.registry.TemperatureModifierRegistry;

public final class LegendarySurvivalOverhaulLoadedCompat
{
    private static final String MODIFIER_NAME = "tfc_modern_life_climate_control";

    private LegendarySurvivalOverhaulLoadedCompat() {}

    public static void register()
    {
        // LSO 1.21（2.4.7.2）的 TemperatureUtilInternal#getWorldTemperature 只遍历
        // TemperatureModifierRegistry.MODIFIERS 这一个 DeferredRegister 自己的条目
        //（DeferredRegister#getEntries() 返回该实例的 entriesView），不再像 1.20 那样遍历整个注册表。
        // 自建一个同 key 的 DeferredRegister 只会把条目写进注册表、不会被 LSO 读取，
        // 因此必须注册到 LSO 自己的 DeferredRegister 上。必须在本模组构造函数里完成，
        // 早于 RegisterEvent 触发。
        TemperatureModifierRegistry.MODIFIERS.register(MODIFIER_NAME, TFCMLTemperatureModifier::new);
    }
}
