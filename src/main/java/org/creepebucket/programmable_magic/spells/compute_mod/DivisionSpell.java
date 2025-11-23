package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellValueType;

public class DivisionSpell extends BinaryOperatorSpell {
    @Override
    public String getRegistryName() { return "compute_div"; }

    @Override
    protected ComputeValue evaluate(ComputeValue left, ComputeValue right) {
        if (left == null || right == null) return null;
        double divisor;
        if (right.value() instanceof Number num) {
            divisor = num.doubleValue();
        } else if (right.type() == SpellValueType.VECTOR3) {
            return null; // 暂不支持向量除法
        } else {
            divisor = 0;
        }
        if (divisor == 0.0) return null;

        if (left.type() == SpellValueType.NUMBER && right.type() == SpellValueType.NUMBER) {
            return new ComputeValue(SpellValueType.NUMBER,
                    asDouble(left.value()) / divisor);
        }
        if (left.type() == SpellValueType.VECTOR3 && right.type() == SpellValueType.NUMBER) {
            Vec3 vec = (Vec3) left.value();
            double factor = 1.0 / divisor;
            return new ComputeValue(SpellValueType.VECTOR3,
                    new Vec3(vec.x * factor, vec.y * factor, vec.z * factor));
        }
        return null;
    }
}
