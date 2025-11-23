package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Locale;

/**
 * 将其左侧最近的按 compute_mod 分隔的最多三个数字构造成 XYZ 向量，写入 SpellData。
 * 简化后预期效果示例：1, 2, 3 构建XYZ向量 -> (1, 2, 3)
 */
public class BuildXYZVectorSpell extends ComputeFunctionalSpell {

    @Override
    public String getRegistryName() { return "compute_vec_xyz"; }

    @Override
    protected ComputeRuntime.ArgSpec argumentSpec() {
        SpellValueType[] types = new SpellValueType[] {
                SpellValueType.NUMBER,
                SpellValueType.NUMBER,
                SpellValueType.NUMBER
        };
        return ComputeRuntime.ArgSpec.optional(3, types);
    }

    @Override
    protected ComputeValue compute(Player player, SpellData data, List<ComputeValue> args) {
        double x = asNumber(args, 0, 0.0);
        double y = asNumber(args, 1, 0.0);
        double z = asNumber(args, 2, 0.0);

        Vec3 vec = new Vec3(x, y, z);
        if (data != null) {
            data.setCustomData("vector_xyz", vec);
            data.setCustomData("vector_xyz_str",
                    String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)", x, y, z));
        }
        return new ComputeValue(SpellValueType.VECTOR3, vec);
    }

    private double asNumber(List<ComputeValue> values, int index, double fallback) {
        if (index >= values.size()) return fallback;
        Object raw = values.get(index).value();
        return raw instanceof Number num ? num.doubleValue() : fallback;
    }

    @Override
    public List<Component> getTooltip() {
        java.util.List<org.creepebucket.programmable_magic.spells.SpellValueType> in = java.util.List.of(
                org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER,
                org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER,
                org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER
        );
        org.creepebucket.programmable_magic.spells.SpellValueType out = org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;
        Component desc = Component.literal("构建XYZ向量: 使用左侧以分隔符划分的最多三个数");
        return org.creepebucket.programmable_magic.spells.SpellTooltipUtil.buildTooltip(new java.util.ArrayList<>(in), out, desc, this);
    }
}
