package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.List;

/**
 * 统一处理参数获取与类型校验的 compute_mod 基类。
 */
public abstract class ComputeFunctionalSpell extends SimpleComputeSpell implements ComputeValueProvider {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("ProgrammableMagic:ComputeFunc");
    private ComputeValue providedValue;

    @Override
    public final boolean run(Player player, SpellData data) {
        if (data == null) { providedValue = null; return true; }
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) { providedValue = null; return true; }

        // 此处不做全局标准化，避免移除分隔符，便于按分隔符切分参数

        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof java.util.List<?> raw)) { providedValue = null; return true; }
        @SuppressWarnings("unchecked")
        java.util.List<org.creepebucket.programmable_magic.spells.SpellItemLogic> seq =
                (java.util.List<org.creepebucket.programmable_magic.spells.SpellItemLogic>) raw;

        // 归一化后，索引可能已左移：重新定位当前节点的真实位置
        int realIdx = seq.indexOf(this);
        if (realIdx < 0) { providedValue = null; return true; }
        if (!java.util.Objects.equals(idx, realIdx)) {
            data.setCustomData("__idx", realIdx);
            idx = realIdx;
        }

        ComputeRuntime.ArgSpec spec = argumentSpec();

        // 若当前运算符被括号包裹，则在括号范围内取参并整体替换括号
        int parenL = -1, parenR = -1;
        {
            int depth = 0;
            for (int i = idx; i >= 0; i--) {
                var s = seq.get(i);
                String rn = s == null ? null : s.getRegistryName();
                if ("compute_rparen".equals(rn)) { depth++; }
                else if ("compute_lparen".equals(rn)) { if (depth == 0) { parenL = i; break; } depth--; }
            }
            depth = 0;
            for (int i = idx; i < seq.size(); i++) {
                var s = seq.get(i);
                String rn = s == null ? null : s.getRegistryName();
                if ("compute_lparen".equals(rn)) { depth++; }
                else if ("compute_rparen".equals(rn)) { if (depth == 0) { parenR = i; break; } depth--; }
            }
        }

        // 复用统一“组”扫描：从左侧依次提取值组（优先拿最近的值），直到达到参数上限
        java.util.List<ComputeValue> args = new java.util.ArrayList<>();
        java.util.List<int[]> ranges = new java.util.ArrayList<>(); // [start, end]
        int cur = idx - 1;
        int windowStart = -1;
        int maxArgs = Math.max(0, spec.maxArgs());
        int lowerBound = parenL >= 0 ? parenL + 1 : 0;
        while (cur >= lowerBound && args.size() < maxArgs) {
            ComputeRuntime.GroupValue g = ComputeRuntime.valueFromLeftGroup(player, data, cur);
            if (g == null || g.value() == null) break;
            // 参数按从左到右顺序入列
            args.add(0, g.value());
            ranges.add(0, new int[]{g.start(), g.end()});
            windowStart = g.start();
            cur = g.start() - 1;
        }

        if (LOGGER.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.size(); i++) {
                ComputeValue av = args.get(i);
                sb.append(i).append(':').append(av == null ? "<null>" : (av.type()+":"+String.valueOf(av.value()))).append(' ');
            }
            LOGGER.debug("函数 {} 收集到参数({}) 范围: L={}, R={}, 窗口起点={} -> {}", getRegistryName(), args.size(), parenL, parenR, windowStart, sb.toString().trim());
        }

        String error = ComputeRuntime.validateArgs(args, spec);
        if (error != null) { ComputeRuntime.sendError(player, error); providedValue = null; return true; }

        providedValue = compute(player, data, args);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("函数 {} 计算结果: {}", getRegistryName(), providedValue == null ? "<null>" : (providedValue.type()+":"+String.valueOf(providedValue.value())));
        }
        if (providedValue == null) return true;

        // 若处于“只求值”模式（由 ComputeRuntime.ensureValueAt 设置），则不进行任何序列替换，避免参数求值引发重写与索引回退
        Boolean evalOnly = data.getCustomData("__eval_only", Boolean.class);
        if (Boolean.TRUE.equals(evalOnly)) {
            return true;
        }

        // KISS: 一次性重建 —— 若存在括号，用结果替换 [parenL .. parenR]；否则替换 [windowStart .. idx]
        int opIdx = seq.indexOf(this);
        if (opIdx < 0) return false;
        if (windowStart < 0) windowStart = Math.max(0, opIdx); // 兜底：至少替换自身
        int leftBound = (parenL >= 0 && parenR >= 0) ? parenL : windowStart;
        int rightBound = (parenL >= 0 && parenR >= 0) ? parenR : opIdx;
        org.creepebucket.programmable_magic.spells.SpellItemLogic lit =
                providedValue.type() == org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER
                        ? new NumberLiteralSpell(((Number) providedValue.value()).doubleValue())
                        : new ValueLiteralSpell(providedValue);
        java.util.ArrayList<org.creepebucket.programmable_magic.spells.SpellItemLogic> rebuilt = new java.util.ArrayList<>(seq.size());
        for (int i = 0; i < leftBound; i++) rebuilt.add(seq.get(i));
        int insertIdx = rebuilt.size();
        rebuilt.add(lit);
        for (int i = rightBound + 1; i < seq.size(); i++) rebuilt.add(seq.get(i));
        // 提供整体替换给载体
        data.setCustomData("__seq_replace", rebuilt);
        data.setCustomData("__idx_replace", insertIdx);
        // 返回 false，避免索引递增跳过折叠后的相邻节点
        return false;
    }

    protected abstract ComputeRuntime.ArgSpec argumentSpec();

    protected abstract ComputeValue compute(Player player, SpellData data, List<ComputeValue> args);

    @Override
    public ComputeValue getProvidedValue() {
        return providedValue;
    }
}
