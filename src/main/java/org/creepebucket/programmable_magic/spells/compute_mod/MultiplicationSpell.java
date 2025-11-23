package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellValueType;

public class MultiplicationSpell extends BinaryOperatorSpell {
    @Override
    public String getRegistryName() { return "compute_mul"; }

    @Override
    protected ComputeValue evaluate(ComputeValue left, ComputeValue right) {
        if (left == null || right == null) return null;
        if (left.type() == SpellValueType.NUMBER && right.type() == SpellValueType.NUMBER) {
            return new ComputeValue(SpellValueType.NUMBER,
                    asDouble(left.value()) * asDouble(right.value()));
        }
        if (left.type() == SpellValueType.VECTOR3 && right.type() == SpellValueType.NUMBER) {
            return new ComputeValue(SpellValueType.VECTOR3,
                    scaleVec((Vec3) left.value(), asDouble(right.value())));
        }
        if (left.type() == SpellValueType.NUMBER && right.type() == SpellValueType.VECTOR3) {
            return new ComputeValue(SpellValueType.VECTOR3,
                    scaleVec((Vec3) right.value(), asDouble(left.value())));
        }
        return null;
    }

    protected double asDouble(Object obj) {
        return obj instanceof Number num ? num.doubleValue() : 0.0;
    }

    protected Vec3 scaleVec(Vec3 vec, double factor) {
        if (vec == null) return Vec3.ZERO;
        return new Vec3(vec.x * factor, vec.y * factor, vec.z * factor);
    }
}
