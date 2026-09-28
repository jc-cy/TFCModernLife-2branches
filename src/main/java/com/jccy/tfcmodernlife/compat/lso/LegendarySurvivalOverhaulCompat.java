package com.jccy.tfcmodernlife.compat.lso;

import com.mojang.logging.LogUtils;
import java.lang.reflect.InvocationTargetException;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class LegendarySurvivalOverhaulCompat
{
    private static final String MOD_ID = "legendarysurvivaloverhaul";
    private static final String LOADED_COMPAT_CLASS = "com.jccy.tfcmodernlife.compat.lso.LegendarySurvivalOverhaulLoadedCompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    private LegendarySurvivalOverhaulCompat() {}

    public static void register()
    {
        if (ModList.get().isLoaded(MOD_ID))
        {
            try
            {
                Class.forName(LOADED_COMPAT_CLASS).getMethod("register").invoke(null);
            }
            catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | LinkageError e)
            {
                LOGGER.warn("Legendary Survival Overhaul compatibility could not be enabled", e);
            }
        }
    }
}
