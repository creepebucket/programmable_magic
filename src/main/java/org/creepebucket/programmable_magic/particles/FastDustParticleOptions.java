package org.creepebucket.programmable_magic.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.creepebucket.programmable_magic.registries.ModParticleTypes;
import org.joml.Vector3f;

public class FastDustParticleOptions extends ScalableParticleOptionsBase {
    public static final MapCodec<FastDustParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(options -> options.color),
                    SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)
            ).apply(instance, FastDustParticleOptions::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FastDustParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, options -> options.color,
            ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale,
            FastDustParticleOptions::new
    );

    private final int color;

    public FastDustParticleOptions(int color, float scale) {
        super(scale);
        this.color = color;
    }

    @Override
    public ParticleType<FastDustParticleOptions> getType() {
        return ModParticleTypes.FAST_DUST.get();
    }

    public Vector3f getColor() {
        return ARGB.vector3fFromRGB24(this.color);
    }
}

