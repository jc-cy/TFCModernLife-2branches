package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TFCModernLife.MOD_ID)
public final class ModEvents
{
    private static final int CLIMATE_REGISTRY_TICK_INTERVAL = 100;

    private ModEvents() {}

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END
            && event.level instanceof ServerLevel serverLevel
            && serverLevel.getGameTime() % CLIMATE_REGISTRY_TICK_INTERVAL == 0)
        {
            ClimateStationRegistry.tickLevel(serverLevel);
        }
    }

}
