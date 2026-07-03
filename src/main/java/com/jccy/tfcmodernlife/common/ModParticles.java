package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles
{
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TFCModernLife.MOD_ID);

    public static final RegistryObject<SimpleParticleType> REFRIGERATOR_COLD_MIST = PARTICLE_TYPES.register("refrigerator_cold_mist",
        () -> new SimpleParticleType(false));

    private ModParticles() {}

    public static void register(IEventBus bus)
    {
        PARTICLE_TYPES.register(bus);
    }
}
