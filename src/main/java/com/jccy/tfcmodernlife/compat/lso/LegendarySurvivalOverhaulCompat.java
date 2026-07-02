package com.jccy.tfcmodernlife.compat.lso;

import com.mojang.logging.LogUtils;
import java.lang.reflect.InvocationTargetException;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

public final class LegendarySurvivalOverhaulCompat
{
    private static final String MOD_ID = "legendarysurvivaloverhaul";
    private static final String LOADED_COMPAT_CLASS = "com.jccy.tfcmodernlife.compat.lso.LegendarySurvivalOverhaulLoadedCompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    private LegendarySurvivalOverhaulCompat() {}

    public static void register(IEventBus modBus)
    {
        if (ModList.get().isLoaded(MOD_ID))
        {
            try
            {
                Class.forName(LOADED_COMPAT_CLASS).getMethod("register", IEventBus.class).invoke(null, modBus);
            }
            catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | LinkageError e)
            {
                LOGGER.warn("Legendary Survival Overhaul compatibility could not be enabled", e);
            }
        }
    }
}
