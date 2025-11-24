package org.creepebucket.programmable_magic.spells.compute_mod;

import org.creepebucket.programmable_magic.spells.SpellValueType;

public class ExponentSpell extends BinaryOperatorSpell {
    @Override
    public String getRegistryName() { return "compute_pow"; }

    @Override
    protected ComputeValue evaluate(ComputeValue left, ComputeValue right) {
        if (left == null || right == null) return null;
        if (left.type() == SpellValueType.NUMBER && right.type() == SpellValueType.NUMBER) {
            double a = asDouble(left.value());
            double b = asDouble(right.value());
            return new ComputeValue(SpellValueType.NUMBER, Math.pow(a, b));
        }
        return null;
    }
}
