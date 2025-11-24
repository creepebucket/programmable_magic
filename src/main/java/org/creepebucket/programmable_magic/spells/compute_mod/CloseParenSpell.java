package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;

public class CloseParenSpell extends OperatorComputeBase implements ComputeValueProvider {
    private ComputeValue providedValue;

    @Override
    public String getRegistryName() { return "compute_rparen"; }

    @Override
    public boolean run(Player player, SpellData data) {
        providedValue = null;
        if (data == null) return true;
        Integer idx = data.getCustomData("__idx", Integer.class);
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (idx == null || !(seqObj instanceof List<?> raw)) return true;
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;

        // 标准化数字并移除分隔符，确保括号内数字已就绪
        ComputeRuntime.normalizeNumbers(data);

        // 归一化后，索引可能已变化：重新定位当前节点
        int realIdx = seq.indexOf(this);
        if (realIdx < 0) return true;
        if (!java.util.Objects.equals(idx, realIdx)) {
            data.setCustomData("__idx", realIdx);
            idx = realIdx;
        }

        // 向左寻找匹配的 '('，只在最外层配对
        int depth = 0;
        int l = -1;
        for (int i = idx; i >= 0; i--) {
            SpellItemLogic s = seq.get(i);
            if (s == null) continue;
            String rn = s.getRegistryName();
            if ("compute_rparen".equals(rn)) depth++;
            else if ("compute_lparen".equals(rn)) {
                depth--;
                if (depth == 0) { l = i; break; }
            }
        }
        if (l < 0 || l >= idx) return true;

        ComputeValue inner = ComputeRuntime.evaluateRange(player, data, seq, l + 1, idx - 1);
        if (inner == null) return true;
        providedValue = inner;

        // 用结果替换整个括号段 [l..idx]
        int len = idx - l + 1;
        for (int k = 0; k < len; k++) seq.remove(l);
        SpellItemLogic lit = inner.type() == SpellValueType.NUMBER
                ? new NumberLiteralSpell(((Number) inner.value()).doubleValue())
                : new ValueLiteralSpell(inner);
        // 下标 l 处插入替换：使用 set 需先有占位，改为 add
        seq.add(l, lit);
        // 提供整体替换给载体，避免外部仍持有旧引用导致索引错位
        data.setCustomData("__seq_replace", new java.util.ArrayList<>(seq));
        data.setCustomData("__idx_replace", l);
        return false;
    }

    @Override
    public ComputeValue getProvidedValue() { return providedValue; }
}
