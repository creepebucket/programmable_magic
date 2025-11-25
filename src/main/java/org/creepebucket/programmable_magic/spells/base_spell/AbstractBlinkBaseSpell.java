package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;
import org.creepebucket.programmable_magic.util.WeightUtil;

import java.util.List;

/**
 * 短途传送抽象基类：负责 Δpos 参数解析、耗魔与上限检查、以及通用位移/清速。
 * 能耗：E = m * (|Δ| * 20)^2；上限 10 MJ，超过则报错且不执行。
 */
public abstract class AbstractBlinkBaseSpell extends BaseSpellEffectLogic {
    private static final double MAX_KJ = 10000.0;

    protected abstract Entity resolveTarget(Player player, SpellData data, int idx);

    protected Vec3 resolveDelta(Player player, SpellData data, int idx) {
        // 不依赖 Player，上下文从 SpellData 提供
        ComputeValue v = new ComputeValue(SpellValueType.VECTOR3, data.getCustomData("__seq", List.class).get(idx-1));
        return v != null && v.value() instanceof Vec3 delta ? delta : null;
    }

    protected double targetMassKg(Entity e) {
        if (e instanceof SpellEntity se) return WeightUtil.massForSpellEntity(se);
        return WeightUtil.massForEntity(e);
    }

    protected double blinkEnergyJ(double massKg, Vec3 delta) {
        double v = delta.length() * 20.0; // m/s
        return massKg * v * v; // 加速+减速两次：2*(0.5*m*v^2) = m*v^2
    }

    @Override
    public boolean run(Player player, SpellData data) {
        if (data == null) return true;
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) return true;
        Entity target = resolveTarget(player, data, idx);
        Vec3 delta = resolveDelta(player, data, idx);
        if (target == null || delta == null) return true;

        double eJ = blinkEnergyJ(targetMassKg(target), delta);
        double need = eJ / 1000.0;
        if (need > MAX_KJ) {
            ComputeRuntime.sendError(player, "短途传送超出上限(10MJ)");
            return true;
        }

        var np = target.position().add(delta);
        // 对玩家使用服务端连接级传送，其他实体使用 setPos
        if (target instanceof ServerPlayer sp) {
            try {
                sp.connection.teleport(np.x, np.y, np.z, sp.getYRot(), sp.getXRot());
            } catch (Throwable ignored) {
                // 兜底：若 API 变化或失败则退回 setPos
                target.setPos(np.x, np.y, np.z);
            }
        } else {
            target.setPos(np.x, np.y, np.z);
        }
        target.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) { data.setManaCost("momentum", 0.0); return; }
        Entity target = resolveTarget(null, data, idx);
        Vec3 delta = resolveDelta(null, data, idx);
        if (target == null || delta == null) { data.setManaCost("momentum", 0.0); return; }
        double eJ = blinkEnergyJ(targetMassKg(target), delta);
        double mana = Math.min(MAX_KJ, Math.max(0.0, eJ / 1000.0));
        data.setManaCost("momentum", mana);
    }
}
