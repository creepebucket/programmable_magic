package org.creepebucket.arcanism.registries;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.arcanism.particles.FastDustParticleType;

import java.util.function.Supplier;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);

    public static final Supplier<FastDustParticleType> FAST_DUST = PARTICLE_TYPES.register(
            "fast_dust",
            () -> new FastDustParticleType(false)
    );

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}

