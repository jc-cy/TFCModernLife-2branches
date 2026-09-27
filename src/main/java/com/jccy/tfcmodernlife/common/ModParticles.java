package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles
{
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, TFCModernLife.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> REFRIGERATOR_COLD_MIST = PARTICLE_TYPES.register("refrigerator_cold_mist",
        () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUP_POT_STEAM = PARTICLE_TYPES.register("soup_pot_steam",
        () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUP_POT_BUBBLE = PARTICLE_TYPES.register("soup_pot_bubble",
        () -> new SimpleParticleType(false));

    private ModParticles() {}

    public static void register(IEventBus bus)
    {
        PARTICLE_TYPES.register(bus);
    }
}
