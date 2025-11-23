package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 提供“无输入 -> Vector3 输出”的 compute_mod 基类。
 */
public abstract class BaseVectorOutputSpell extends ComputeFunctionalSpell {

    @Override
    protected ComputeRuntime.ArgSpec argumentSpec() {
        return ComputeRuntime.ArgSpec.fixed(0);
    }

    @Override
    protected ComputeValue compute(Player player, SpellData data, List<ComputeValue> args) {
        Vec3 vec = resolveVector(player, data);
        if (vec == null) return null;
        if (data != null) {
            data.setCustomData("vector_xyz", vec);
            data.setCustomData("vector_xyz_str",
                    String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)", vec.x, vec.y, vec.z));
        }
        return new ComputeValue(SpellValueType.VECTOR3, vec);
    }

    protected abstract Vec3 resolveVector(Player player, SpellData data);

    protected abstract Component describe();

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.VECTOR3, describe(), this);
    }
}
