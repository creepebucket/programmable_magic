package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;
import org.creepebucket.programmable_magic.util.WeightUtil;

/**
 * 动量系列法术的抽象基类：负责解析 (Entity, Vector3) 参数与耗魔计算，
 * 默认将速度增量按威力倍率缩放。
 */
public abstract class AbstractImpulseBaseSpell extends BaseSpellEffectLogic {

    protected abstract Entity resolveTarget(Player player, SpellData data, int idx);

    protected boolean scaleVectorByPower() { return true; }

    protected Vec3 resolveVector(Player player, SpellData data, int idx) {
        // 不依赖 Player，上下文从 SpellData 提供
        ComputeValue v = ComputeRuntime.findLeftValue(null, data, idx, SpellValueType.VECTOR3);
        if (v != null && v.value() instanceof Vec3 vec) {
            if (scaleVectorByPower()) {
                double p = Math.max(0.0, data.getPower());
                if (p != 1.0) return vec.scale(p);
            }
            return vec;
        }
        return null;
    }

    protected double targetMassKg(Entity e) {
        return WeightUtil.massForEntity(e);
    }

    protected double impulseEnergyJ(double massKg, Vec3 dv_mps) {
        return 0.5 * massKg * dv_mps.lengthSqr();
    }

    @Override
    public boolean run(Player player, SpellData data) {
        if (data == null) return true;
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) return true;
        Entity target = resolveTarget(player, data, idx);
        Vec3 dv = resolveVector(player, data, idx);
        if (target == null || dv == null) return true;

        // 应用速度增量（m/s -> 块/tick）
        target.setDeltaMovement(target.getDeltaMovement().add(dv.scale(1.0 / 20.0)));
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) { data.setManaCost("momentum", 0.0); return; }
        Entity target = resolveTarget(null, data, idx);
        Vec3 dv = resolveVector(null, data, idx);
        if (target == null || dv == null) { data.setManaCost("momentum", 0.0); return; }
        double eJ = impulseEnergyJ(targetMassKg(target), dv);
        data.setManaCost("momentum", Math.max(0.0, eJ / 1000.0));
    }
}
