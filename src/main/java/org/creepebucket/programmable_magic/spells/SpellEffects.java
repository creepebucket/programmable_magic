package org.creepebucket.programmable_magic.spells;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.gui.lib.api.Color;

public class SpellEffects {
    public static void charge(Player player, ServerLevel serverLevel, double mana) {
        for (int i = 1; i < Math.log(mana); i++) {
            var particleTarget = player.position().add(new Vec3(0, 1.5, 0));
            var particlePos = new Vec3(Math.random() * 4 - 2, Math.random() * 4 - 2, Math.random() * 4 - 2).add(particleTarget).subtract(player.getLookAngle());

            // 以粒子到玩家的距离作为生成概率
            var distanceFactor = Math.pow(particlePos.distanceTo(particleTarget), 2);
            if (Math.random() > distanceFactor) continue;
            var color = new Color(-1).toArgbWithAlphaMult(1 / distanceFactor);

            // 计算粒子的delta
            var clusterFact = distanceFactor / 10;
            var inertiaFact = 0.5;
            var scatterFact = 0.3;

            var cluster = particleTarget.subtract(particlePos).multiply(clusterFact, clusterFact, clusterFact);
            var inertia = player.getLookAngle().multiply(inertiaFact, inertiaFact, inertiaFact);
            var scatter = new Vec3(Math.random() - .5, Math.random() - .5, Math.random() - .5).normalize().multiply(scatterFact, scatterFact, scatterFact);

            var delta = cluster.add(inertia).add(scatter);
            var to = particlePos.add(delta.normalize());

            serverLevel.sendParticles(new TrailParticleOption(to, color, 10), particlePos.x(), particlePos.y(), particlePos.z(), 1, 0, 0, 0, 0);
        }
    }

    public static void trail(SpellEntity entity) {
        var speed = entity.getDeltaMovement().length();
        var velocity = entity.getDeltaMovement();
        var level = entity.level();

        var particleMult = 1;

        // 基础粒子
        var baseTrailScatterFact = 0.5;
        var baseTrailParticleMult = 10;

        for (int i = 0; i < baseTrailParticleMult * particleMult; i++) {
            if (Math.random() > speed / baseTrailParticleMult / particleMult) continue;
            level.addParticle(ParticleTypes.END_ROD, true, true,
                    entity.getX() + Math.random() * baseTrailScatterFact - baseTrailScatterFact / 2 + velocity.x() * i / baseTrailParticleMult / particleMult,
                    entity.getY() + Math.random() * baseTrailScatterFact - baseTrailScatterFact / 2 + velocity.y() * i / baseTrailParticleMult / particleMult,
                    entity.getZ() + Math.random() * baseTrailScatterFact - baseTrailScatterFact / 2 + velocity.z() * i / baseTrailParticleMult / particleMult,
                    0, 0, 0);
        }

        // 烟雾, 出现在低速到中速
        var smallSmokeParticleMult = 50;
        var bigSmokeParticleMult = 10;
        var smokePositionScatterFact = 0.2;
        var scatterSpeed = 1;

        for (int i = 0; i < smallSmokeParticleMult * particleMult; i++) {
            if (Math.random() > (speed + 5) / smallSmokeParticleMult / particleMult) continue;

            var color = new Color(192, 192 - (int) Math.min(speed * 10, 192), 192 - (int) Math.min(speed * 10, 192)).toArgb(); // 速度增大逐渐变红

            // 让粒子从中心分散
            var velocityDirection = velocity.normalize();
            var newNormalX = velocityDirection.cross(velocityDirection.y() > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize();
            var newNormalZ = velocityDirection.cross(newNormalX).normalize();
            var yaw = Math.random() * 2 * Math.PI;

            var scatterDirection = newNormalX.multiply(Math.cos(yaw), Math.cos(yaw), Math.cos(yaw)).add(newNormalZ.multiply(Math.sin(yaw), Math.sin(yaw), Math.sin(yaw)));
            var particleVelocity = velocity.add(scatterDirection.multiply(scatterSpeed, scatterSpeed, scatterSpeed)).multiply(9, 9, 9);
            
            level.addParticle(new DustParticleOptions(color, 1), true, true,
                    entity.getX() + Math.random() * smokePositionScatterFact - smokePositionScatterFact / 2 + velocity.x() * i / smallSmokeParticleMult / particleMult,
                    entity.getY() + Math.random() * smokePositionScatterFact - smokePositionScatterFact / 2 + velocity.y() * i / smallSmokeParticleMult / particleMult,
                    entity.getZ() + Math.random() * smokePositionScatterFact - smokePositionScatterFact / 2 + velocity.z() * i / smallSmokeParticleMult / particleMult,
                    particleVelocity.x(), particleVelocity.y(), particleVelocity.z());
        }
    }
}
