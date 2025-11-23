package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellValueType;

public class AdditionSpell extends BinaryOperatorSpell {
    @Override
    public String getRegistryName() { return "compute_add"; }

    @Override
    protected ComputeValue evaluate(ComputeValue left, ComputeValue right) {
        if (left == null || right == null) return null;
        if (left.type() == SpellValueType.NUMBER && right.type() == SpellValueType.NUMBER) {
            return new ComputeValue(SpellValueType.NUMBER,
                    asDouble(left.value()) + asDouble(right.value()));
        }
        if (left.type() == SpellValueType.VECTOR3 && right.type() == SpellValueType.VECTOR3) {
            Vec3 lv = (Vec3) left.value();
            Vec3 rv = (Vec3) right.value();
            return new ComputeValue(SpellValueType.VECTOR3,
                    new Vec3(lv.x + rv.x, lv.y + rv.y, lv.z + rv.z));
        }
        return null;
    }
}
