package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.List;

/**
 * 通用的二元算符基类，负责抓取左右参数并缓存结果。
 */
public abstract class BinaryOperatorSpell extends OperatorComputeBase implements ComputeValueProvider {
    private ComputeValue providedValue;

    @Override
    public boolean run(Player player, SpellData data) {
        providedValue = null;
        if (data == null) return true;
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) return true;

        List<ComputeValue> leftArgs = ComputeArgsHelper.collectArgs(data, idx, 1);
        ComputeValue left = leftArgs.isEmpty() ? null : leftArgs.get(0);
        ComputeValue right = ComputeRuntime.findRightValue(player, data, idx);

        providedValue = evaluate(left, right);
        return true;
    }

    protected abstract ComputeValue evaluate(ComputeValue left, ComputeValue right);

    @Override
    public ComputeValue getProvidedValue() {
        return providedValue;
    }

    protected double asDouble(Object obj) {
        return obj instanceof Number num ? num.doubleValue() : 0.0;
    }

    protected Vec3 scaleVec(Vec3 vec, double factor) {
        if (vec == null) return Vec3.ZERO;
        return new Vec3(vec.x * factor, vec.y * factor, vec.z * factor);
    }
}
