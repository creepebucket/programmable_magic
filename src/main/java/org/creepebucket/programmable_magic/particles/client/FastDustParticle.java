package org.creepebucket.programmable_magic.particles.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.creepebucket.programmable_magic.particles.FastDustParticleOptions;
import org.joml.Vector3f;

public class FastDustParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    protected FastDustParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            FastDustParticleOptions options,
            SpriteSet sprites
    ) {
        super(level, x, y, z, sprites.first());
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;

        this.setParticleSpeed(xSpeed, ySpeed, zSpeed);

        this.quadSize = this.quadSize * (0.75F * options.getScale());
        int i = (int) (8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.lifetime = (int) Math.max(i * options.getScale(), 1.0F);
        this.setSpriteFromAge(sprites);

        float f = this.random.nextFloat() * 0.4F + 0.6F;
        Vector3f color = options.getColor();
        this.rCol = this.randomizeColor(color.x(), f);
        this.gCol = this.randomizeColor(color.y(), f);
        this.bCol = this.randomizeColor(color.z(), f);
    }

    protected float randomizeColor(float baseColor, float mult) {
        return (this.random.nextFloat() * 0.2F + 0.8F) * baseColor * mult;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * Mth.clamp((this.age + partialTick) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<FastDustParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                FastDustParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed,
                RandomSource random
        ) {
            return new FastDustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options, this.sprites);
        }
    }
}

