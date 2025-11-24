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

        // 不做全局数字归一化：避免提前移除分隔符，统一交给 evaluateRange 处理

        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof java.util.List<?> raw)) return true;
        @SuppressWarnings("unchecked")
        java.util.List<org.creepebucket.programmable_magic.spells.SpellItemLogic> seq =
                (java.util.List<org.creepebucket.programmable_magic.spells.SpellItemLogic>) raw;

        // 归一化后，索引可能已左移：重新定位当前节点的真实位置
        int realIdx = seq.indexOf(this);
        if (realIdx < 0) return true;
        if (!java.util.Objects.equals(idx, realIdx)) {
            data.setCustomData("__idx", realIdx);
            idx = realIdx;
        }

        ComputeRuntime.GroupValue leftG = ComputeRuntime.valueFromLeftGroup(player, data, idx - 1);
        ComputeRuntime.GroupValue rightG = ComputeRuntime.valueFromRightGroup(player, data, idx + 1);
        if (leftG == null || rightG == null) return true;

        // 复用统一表达式求值：对 [left..right] 这个最小覆盖段整体 evaluateRange
        ComputeValue inner = ComputeRuntime.evaluateRange(player, data, seq, leftG.start(), rightG.end());
        providedValue = inner;

        // KISS: 直接把 左组 + 运算符 + 右组 折叠为结果节点
        if (providedValue != null) {
            // 以“重建新列表”的方式一次性替换，避免多次删除带来的索引漂移
            int leftStart = leftG.start();
            int rightEnd = rightG.end();
            org.creepebucket.programmable_magic.spells.SpellItemLogic lit =
                    providedValue.type() == org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER
                            ? new NumberLiteralSpell(((Number) providedValue.value()).doubleValue())
                            : new ValueLiteralSpell(providedValue);

            java.util.ArrayList<org.creepebucket.programmable_magic.spells.SpellItemLogic> rebuilt = new java.util.ArrayList<>(seq.size());
            // 前缀 [0 .. leftStart-1]
            for (int i = 0; i < leftStart; i++) rebuilt.add(seq.get(i));
            // 中间替换为结果
            int insertIdx = rebuilt.size();
            rebuilt.add(lit);
            // 后缀 [rightEnd+1 .. end]
            for (int i = rightEnd + 1; i < seq.size(); i++) rebuilt.add(seq.get(i));

            // 将新列表作为整体替换交给载体
            data.setCustomData("__seq_replace", rebuilt);
            data.setCustomData("__idx_replace", insertIdx);
            // 返回 false，避免当前索引自增导致跳过折叠后紧随其后的节点
            return false;
        }
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
