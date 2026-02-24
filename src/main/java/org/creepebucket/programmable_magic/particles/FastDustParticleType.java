package org.creepebucket.programmable_magic.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class FastDustParticleType extends ParticleType<FastDustParticleOptions> {
    public FastDustParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<FastDustParticleOptions> codec() {
        return FastDustParticleOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, FastDustParticleOptions> streamCodec() {
        return FastDustParticleOptions.STREAM_CODEC;
    }
}

