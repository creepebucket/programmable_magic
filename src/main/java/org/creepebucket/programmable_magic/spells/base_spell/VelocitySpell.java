package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeArgsHelper;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;

import java.util.ArrayList;
import java.util.List;

public class VelocitySpell extends BaseSpellEffectLogic {

    public VelocitySpell() { super(); }

    @Override
    public String getRegistryName() { return "gain_velocity"; }

    @Override
    public boolean run(Player player, SpellData data) {
        if (player == null || data == null) return true;

        Entity carrier = data.getTarget();
        if (carrier == null) return true;

        // 仅接受 Vector3：优先从左侧取一个向量参数，其次尝试从右侧取
        Vec3 input = null;
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx != null) {
            ComputeValue left = ComputeRuntime.findLeftValue(player, data, idx, SpellValueType.VECTOR3);
            if (left != null && left.value() instanceof Vec3 v) {
                input = v;
            }
        }
        if (input == null) return true; // 缺少向量则不生效

        // 将输入视为目标速度（单位 m/s），按此设定载体速度
        Vec3 vTarget_mps = input;
        Vec3 vTarget_bt = vTarget_mps.scale(1.0 / 20.0); // Minecraft 1tick = 1/20 s，1方块≈1m

        // 设定目标速度（以块/每tick 存入）
        carrier.setDeltaMovement(vTarget_bt);
        return true;
    }

    @Override
    public void calculateBaseMana(SpellData data) {
        // 基础耗魔=将载体从静止加速到 |v| 的动能：E = 0.5*m*|v|^2 (J)
        // 1 mana = 1 kJ
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) { data.setManaCost("momentum", 0.0); return; }
        ComputeValue left = ComputeRuntime.findLeftValue(null, data, idx, SpellValueType.VECTOR3);
        if (left == null) {
            data.setManaCost("momentum", 0.0);
            return;
        }
        Vec3 v = (Vec3) left.value();
        double massKg = 10.0; // 默认质量：10kg
        double eJ = 0.5 * massKg * v.lengthSqr();
        double mana = Math.max(0.0, eJ / 1000.0);
        data.setManaCost("momentum", mana);
    }

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        in.add(SpellValueType.VECTOR3);
        SpellValueType out = SpellValueType.SPELL;
        Component desc = Component.literal("将载体速度设为给定向量（m/s）；消耗≈0.5·m·|v|^2 / 1000（m=10kg，1mana=1kJ）");
        return SpellTooltipUtil.buildTooltip(in, out, desc, this);
    }
}
