package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.NumberLiteralSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.NumberComputeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责 compute_mod 运行时公共逻辑：参数获取、类型校验、结果记录等。
 */
public final class ComputeRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:ComputeRuntime");
    private ComputeRuntime() {}

    public record ArgSpec(int requiredArgs, int maxArgs, SpellValueType[] expectedTypes) {
        public ArgSpec {
            if (requiredArgs < 0) requiredArgs = 0;
            if (maxArgs < requiredArgs) maxArgs = requiredArgs;
        }

        public static ArgSpec fixed(int count, SpellValueType... types) {
            return new ArgSpec(count, count, types);
        }

        public static ArgSpec optional(int max, SpellValueType... types) {
            return new ArgSpec(0, max, types);
        }
    }

    public static String validateArgs(List<ComputeValue> args, ArgSpec spec) {
        if (spec == null) return null;
        if (args.size() < spec.requiredArgs()) {
            String msg = "缺少计算参数，期望 " + spec.requiredArgs() + " 个";
            LOGGER.warn("参数校验失败: {} (实际: {})", msg, args.size());
            return msg;
        }
        SpellValueType[] expected = spec.expectedTypes();
        if (expected == null) return null;
        for (int i = 0; i < Math.min(expected.length, args.size()); i++) {
            SpellValueType expect = expected[i];
            if (expect == null) continue;
            ComputeValue arg = args.get(i);
            if (arg == null || arg.type() != expect) {
                SpellValueType actual = arg == null ? null : arg.type();
                String msg = String.format("参数 #%d 类型错误: 期望 %s, 实际 %s",
                        i + 1,
                        display(expect),
                        display(actual));
                LOGGER.warn("参数类型错误: {}", msg);
                return msg;
            }
        }
        return null;
    }

    private static String display(SpellValueType type) {
        return type == null ? "未知" : type.display();
    }

    public static void sendError(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) return;
        LOGGER.warn("向玩家发送错误: {}", message);
        player.displayClientMessage(Component.literal("§c" + message), true);
    }

    public static void recordProvidedValue(SpellData data, SpellItemLogic logic, int index) {
        if (data == null || logic == null) return;
        if (logic instanceof ComputeValueProvider provider) {
            ComputeValue value = provider.getProvidedValue();
            if (value != null) {
                data.recordComputeValue(index, value);
                LOGGER.debug("记录计算值: 索引={}, 类型={}, 值={}", index, value.type(), safeVal(value.value()));
            }
        }
    }

    public static ComputeValue ensureValueAt(Player player, SpellData data, int index) {
        if (data == null) return null;
        ComputeValue existing = data.getComputeValue(index);
        if (existing != null) return existing;
        LOGGER.debug("尝试确保索引 {} 存在计算值", index);
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return null;
        if (index < 0 || index >= raw.size()) return null;
        SpellItemLogic logic = (SpellItemLogic) raw.get(index);
        if (logic.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) return null;

        Integer previousIdx = data.getCustomData("__idx", Integer.class);
        data.setCustomData("__idx", index);
        try {
            LOGGER.debug("运行 compute_mod 节点: 索引={}, 名称={}", index, logic.getRegistryName());
            logic.run(player, data);
            recordProvidedValue(data, logic, index);
        } finally {
            if (previousIdx != null) {
                data.setCustomData("__idx", previousIdx);
            } else {
                data.clearCustomData("__idx");
            }
        }
        ComputeValue got = data.getComputeValue(index);
        LOGGER.debug("索引 {} 计算完成: {}", index, got == null ? "<null>" : (got.type() + ":" + safeVal(got.value())));
        return data.getComputeValue(index);
    }

    /**
     * 只在精确索引上取值：不做跨位置搜索。
     * 若该位置是数字字面量或数字序列的起点，会在此位置聚合为 NumberLiteral 并缓存；
     * 否则尝试直接运行该节点获取其提供值。
     */
    public static ComputeValue valueAtExact(Player player, SpellData data, int index) {
        if (data == null) return null;
        ComputeValue v = data.getComputeValue(index);
        if (v != null) return v;
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return null;
        if (index < 0 || index >= raw.size()) return null;
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;
        SpellItemLogic logic = seq.get(index);
        if (logic == null || logic.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) return null;
        // 分隔符不参与取值
        if (isNumSeparator(logic)) return null;

        // 先处理数字场景：字面量直接取，数字序列从此起点向右聚合
        if (logic instanceof NumberLiteralSpell) {
            recordProvidedValue(data, logic, index);
            LOGGER.debug("索引 {} 为数字字面量，直接取值", index);
            return data.getComputeValue(index);
        }
        if (logic instanceof NumberComputeBase) {
            LOGGER.debug("索引 {} 为数字序列起点，向右聚合", index);
            return literalValueForward(data, seq, index);
        }

        // 其它 compute_mod，直接运行并取值（例如向量构造等）
        LOGGER.debug("索引 {} 为一般 compute_mod，直接运行", index);
        return ensureValueAt(player, data, index);
    }

    public record GroupValue(ComputeValue value, int start, int end) {}

    /**
     * 从左侧紧邻位置（作为一个“组”的右边界）提取值与其覆盖范围。
     * - 对连续数字：向左扩展到该数字组的起点，并解析为 Number。
     * - 其它节点：仅取该索引自身。
     */
    public static GroupValue valueFromLeftGroup(Player player, SpellData data, int endIndex) {
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return null;
        if (endIndex < 0 || endIndex >= raw.size()) return null;
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;
        // 跳过分隔符
        while (endIndex >= 0 && isNumSeparator(seq.get(endIndex))) endIndex--;
        if (endIndex < 0) return null;
        SpellItemLogic node = seq.get(endIndex);
        // 括号组（以右括号结尾）：向左找到匹配的左括号并对内部求值
        if (node != null && "compute_rparen".equals(node.getRegistryName())) {
            int depth = 1;
            int l = -1;
            for (int k = endIndex - 1; k >= 0; k--) {
                SpellItemLogic s = seq.get(k);
                if (s == null) continue;
                String rn = s.getRegistryName();
                if ("compute_rparen".equals(rn)) depth++;
                else if ("compute_lparen".equals(rn)) { depth--; if (depth == 0) { l = k; break; } }
            }
            if (l >= 0) {
                ComputeValue inner = evaluateRange(player, data, seq, l + 1, endIndex - 1);
                if (inner != null) {
                    LOGGER.debug("左括号组取值: [{}..{}] -> {}", l, endIndex, simpleVal(inner));
                    return new GroupValue(inner, l, endIndex);
                }
            }
        }
        if (node instanceof NumberComputeBase) {
            int j = endIndex;
            while (j - 1 >= 0 && seq.get(j - 1) instanceof NumberComputeBase) j--;
            StringBuilder digits = new StringBuilder();
            for (int k = j; k <= endIndex; k++) digits.append(((NumberComputeBase) seq.get(k)).digitValue());
            double d = digits.length() == 0 ? 0.0 : Double.parseDouble(digits.toString());
            LOGGER.debug("左组取值: [{}..{}] -> {}", j, endIndex, d);
            return new GroupValue(new ComputeValue(SpellValueType.NUMBER, d), j, endIndex);
        }
        ComputeValue v = valueAtExact(player, data, endIndex);
        LOGGER.debug("左组取值: [{}] -> {}", endIndex, v == null ? "<null>" : (v.type() + ":" + safeVal(v.value())));
        return v == null ? null : new GroupValue(v, endIndex, endIndex);
    }

    /**
     * 从右侧紧邻位置（作为一个“组”的左边界）提取值与其覆盖范围。
     * - 对连续数字：向右扩展到该数字组的终点，并解析为 Number。
     * - 其它节点：仅取该索引自身。
     */
    public static GroupValue valueFromRightGroup(Player player, SpellData data, int startIndex) {
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return null;
        if (startIndex < 0 || startIndex >= raw.size()) return null;
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;
        // 跳过分隔符
        while (startIndex < seq.size() && isNumSeparator(seq.get(startIndex))) startIndex++;
        if (startIndex >= seq.size()) return null;
        SpellItemLogic node = seq.get(startIndex);
        // 括号组（以左括号开始）：向右找到匹配的右括号并对内部求值
        if (node != null && "compute_lparen".equals(node.getRegistryName())) {
            int depth = 1;
            int r = -1;
            for (int k = startIndex + 1; k < seq.size(); k++) {
                SpellItemLogic s = seq.get(k);
                if (s == null) continue;
                String rn = s.getRegistryName();
                if ("compute_lparen".equals(rn)) depth++;
                else if ("compute_rparen".equals(rn)) { depth--; if (depth == 0) { r = k; break; } }
            }
            if (r >= 0) {
                ComputeValue inner = evaluateRange(player, data, seq, startIndex + 1, r - 1);
                if (inner != null) {
                    LOGGER.debug("右括号组取值: [{}..{}] -> {}", startIndex, r, simpleVal(inner));
                    return new GroupValue(inner, startIndex, r);
                }
            }
        }
        if (node instanceof NumberComputeBase) {
            int j = startIndex;
            while (j + 1 < seq.size() && seq.get(j + 1) instanceof NumberComputeBase) j++;
            StringBuilder digits = new StringBuilder();
            for (int k = startIndex; k <= j; k++) digits.append(((NumberComputeBase) seq.get(k)).digitValue());
            double d = digits.length() == 0 ? 0.0 : Double.parseDouble(digits.toString());
            LOGGER.debug("右组取值: [{}..{}] -> {}", startIndex, j, d);
            return new GroupValue(new ComputeValue(SpellValueType.NUMBER, d), startIndex, j);
        }
        ComputeValue v = valueAtExact(player, data, startIndex);
        LOGGER.debug("右组取值: [{}] -> {}", startIndex, v == null ? "<null>" : (v.type() + ":" + safeVal(v.value())));
        return v == null ? null : new GroupValue(v, startIndex, startIndex);
    }

    public static ComputeValue findLeftValue(Player player, SpellData data, int currentIndex, SpellValueType expected) {
        if (data == null) return null;
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return null;
        if (currentIndex <= 0 || currentIndex > raw.size()) return null;
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;

        // 数字与分隔符先标准化一次
        normalizeNumbers(data);

        GroupValue g = valueFromLeftGroup(player, data, currentIndex - 1);
        if (g == null) return null;
        if (expected != null && g.value().type() != expected) return null;
        return g.value();
    }

    // 删除“向左扫描匹配类型”的方法，保持索引语义简单稳定

    // =============== 数字标准化与分隔符移除 ===============
    public static void normalizeNumbers(SpellData data) {
        if (data == null) return;
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return;
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;
        int before = seq.size();
        int removedSeparators = 0;
        int groupsCollapsed = 0;
        int i = 0;
        while (i < seq.size()) {
            SpellItemLogic cur = seq.get(i);
            if (cur == null) { i++; continue; }
            if (cur instanceof NumberComputeBase) {
                int j = i;
                List<String> nums = new ArrayList<>();
                StringBuilder digits = new StringBuilder();
                int seps = 0;
                while (j < seq.size()) {
                    SpellItemLogic s = seq.get(j);
                    if (s instanceof NumberComputeBase nb) {
                        digits.append(nb.digitValue());
                        j++;
                        continue;
                    }
                    if (isNumSeparator(s)) {
                        if (digits.length() > 0) { nums.add(digits.toString()); digits.setLength(0); }
                        seps++;
                        j++;
                        continue;
                    }
                    break;
                }
                if (digits.length() > 0) { nums.add(digits.toString()); digits.setLength(0); }
                // 替换 [i..j-1]
                for (int k = i; k < j; k++) seq.remove(i);
                for (int n = 0; n < nums.size(); n++) {
                    double d = Double.parseDouble(nums.get(n));
                    seq.add(i + n, new NumberLiteralSpell(d));
                }
                i += Math.max(1, nums.size());
                groupsCollapsed++;
                removedSeparators += seps;
                continue;
            }
            i++;
        }
        int after = seq.size();
        LOGGER.debug("数字归一化完成: 原长度={}, 新长度={}, 移除分隔符={}, 合并组={}",
                before, after, removedSeparators, groupsCollapsed);
    }

    private static boolean isNumSeparator(SpellItemLogic logic) {
        if (logic == null || logic.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) return false;
        String rn = logic.getRegistryName();
        return "compute_mod".equals(rn);
    }

    // 取消额外的括号解析辅助，括号组的值由括号节点自身提供（OpenParenSpell/CloseParenSpell）。

    private static ComputeValue literalValueForward(SpellData data, List<SpellItemLogic> sequence, int startIndex) {
        if (sequence == null || startIndex < 0 || startIndex >= sequence.size()) return null;
        int idx = startIndex;
        StringBuilder digits = new StringBuilder();
        while (idx < sequence.size()) {
            SpellItemLogic logic = sequence.get(idx);
            if (logic instanceof NumberLiteralSpell literal) {
                double val = literal.getValue();
                ComputeValue cv = new ComputeValue(SpellValueType.NUMBER, val);
                data.recordComputeValue(startIndex, cv);
                LOGGER.debug("从索引 {} 前向扫描遇到字面量，值={}", startIndex, val);
                return cv;
            }
            if (logic instanceof NumberComputeBase num) {
                digits.append(num.digitValue());
                idx++;
                continue;
            }
            break;
        }
        if (digits.length() == 0) return null;
        double val = Double.parseDouble(digits.toString());
        ComputeValue cv = new ComputeValue(SpellValueType.NUMBER, val);
        data.recordComputeValue(startIndex, cv);
        LOGGER.debug("从索引 {} 聚合数字得到字面量，值={}", startIndex, val);
        return cv;
    }

    // ====================== 新的表达式解析/求值核心 ======================
    public static ComputeValue evaluateRange(Player player, SpellData data, List<SpellItemLogic> fullSeq,
                                              int start, int end) {
        if (fullSeq == null || data == null) return null;
        if (start > end) return null;
        LOGGER.debug("开始求值范围: [{}..{}]", start, end);
        // 工作片段与原索引映射，避免修改原序列
        List<SpellItemLogic> seg = new ArrayList<>(fullSeq.subList(start, end + 1));
        // 记录片段快照（用于排查表达式结构）
        if (LOGGER.isDebugEnabled()) {
            StringBuilder snap = new StringBuilder();
            for (SpellItemLogic s : seg) {
                if (s == null) { snap.append("<null>"); }
                else { snap.append(String.valueOf(s.getRegistryName())); }
                snap.append(' ');
            }
            LOGGER.debug("片段节点(共 {}): {}", seg.size(), snap.toString().trim());
        }
        List<Integer> origin = new ArrayList<>(seg.size());
        for (int i = 0; i < seg.size(); i++) origin.add(start + i);

        // 1) 递归处理括号：每次找到最内层 ()，将其整体替换为数字字面量
        while (true) {
            int l = -1, r = -1, depth = 0, lastL = -1;
            for (int i = 0; i < seg.size(); i++) {
                SpellItemLogic s = seg.get(i);
                if (s == null) continue;
                String rn = s.getRegistryName();
                if ("compute_lparen".equals(rn)) { depth++; lastL = i; }
                else if ("compute_rparen".equals(rn)) {
                    if (depth <= 0) return null; // 配对错误
                    depth--;
                    if (depth == 0) { l = lastL; r = i; break; }
                }
            }
            if (l >= 0 && r >= 0) {
                int globalL = origin.get(l);
                int globalR = origin.get(r);
                LOGGER.debug("发现括号组: ({} .. {})，递归求值内部", globalL, globalR);
                ComputeValue inside = evaluateRange(player, data, fullSeq, globalL + 1, globalR - 1);
                if (inside == null) return null;
                SpellItemLogic litNode = (inside.type() == SpellValueType.NUMBER)
                        ? new NumberLiteralSpell(((Number) inside.value()).doubleValue())
                        : new ValueLiteralSpell(inside);
                // 用 litNode 替换 [l..r]
                for (int i = l; i <= r; i++) { seg.remove(l); origin.remove(l); }
                seg.add(l, litNode); origin.add(l, -1);
                LOGGER.debug("括号组替换完成: [{}..{}] -> {}，当前段长度={}", globalL, globalR, simpleVal(inside), seg.size());
                continue;
            }
            break;
        }
        LOGGER.debug("括号处理完成，片段长度={}", seg.size());

        // 2) 数字组装：将连续的 NumberComputeBase 合并为 NumberLiteralSpell
        List<SpellItemLogic> compact = new ArrayList<>();
        List<Integer> compactOrigin = new ArrayList<>();
        for (int i = 0; i < seg.size();) {
            SpellItemLogic s = seg.get(i);
            if (s instanceof NumberLiteralSpell lit) {
                compact.add(s); compactOrigin.add(-1); i++; continue;
            }
            if (s instanceof NumberComputeBase) {
                StringBuilder buf = new StringBuilder();
                int j = i;
                while (j < seg.size() && seg.get(j) instanceof NumberComputeBase nb) {
                    buf.append(nb.digitValue()); j++;
                }
                double val = buf.length() == 0 ? 0.0 : Double.parseDouble(buf.toString());
                compact.add(new NumberLiteralSpell(val));
                compactOrigin.add(-1);
                i = j; continue;
            }
            compact.add(s); compactOrigin.add(origin.get(i)); i++;
        }

        // 3) 构建线性 token：值(ComputeValue) 与 运算符(String) 交错
        List<Object> tokens = new ArrayList<>();
        for (int i = 0; i < compact.size(); i++) {
            SpellItemLogic s = compact.get(i);
            if (s == null) continue;
            String rn = s.getRegistryName();
            Character op = opChar(rn);
            if (op != null) {
                // 如果仍检测到括号，说明递归阶段未完全清除；记录警告以便排查
                if (op == '(' || op == ')') {
                    LOGGER.warn("检测到残留括号 '{}' 于 token 构建阶段，这不应发生", op);
                    continue;
                }
                tokens.add(String.valueOf(op));
                continue;
            }
            // 显式跳过分隔符，避免触发 ensureValueAt 噪声
            if (isNumSeparator(s)) continue;
            // 值节点：NumberLiteral 或 其它可提供值的法术
            ComputeValue v = null;
            if (s instanceof ComputeValueProvider provider) {
                v = provider.getProvidedValue();
            }
            if (v == null) {
                int orig = compactOrigin.get(i) == null ? -1 : compactOrigin.get(i);
                if (orig >= 0) v = ensureValueAt(player, data, orig);
            }
            if (v == null && s instanceof NumberLiteralSpell lit) {
                v = new ComputeValue(SpellValueType.NUMBER, lit.getValue());
            }
            if (v != null) tokens.add(v);
        }
        if (tokens.isEmpty()) return null;
        LOGGER.debug("构建 tokens 完成，数量={}", tokens.size());

        // 3.1) 一元负号折叠：在表达式起始或运算符后出现的 '-' 视为取负
        tokens = foldUnaryMinus(tokens);

        // 4) 依次处理 ^, */, +-, 其余
        if (!reduceBinary(tokens, "^") ) { LOGGER.debug("规约 ^ 失败或无变化"); return extractSingle(tokens); }
        if (!reduceBinary(tokens, "* /") ) { LOGGER.debug("规约 * / 失败或无变化"); return extractSingle(tokens); }
        if (!reduceBinary(tokens, "+ -") ) { LOGGER.debug("规约 + - 失败或无变化"); return extractSingle(tokens); }
        // 其他运算预留（左侧收参类），当前无实现

        return extractSingle(tokens);
    }

    private static List<Object> foldUnaryMinus(List<Object> tokens) {
        List<Object> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Object t = tokens.get(i);
            if (t instanceof String s && "-".equals(s)) {
                boolean unary = (out.isEmpty() || out.get(out.size() - 1) instanceof String);
                if (unary) {
                    // 取负紧随的一个值
                    if (i + 1 < tokens.size() && tokens.get(i + 1) instanceof ComputeValue v) {
                        out.add(negate(v));
                        i++; // 跳过被取负的值
                        continue;
                    }
                }
            }
            out.add(t);
        }
        return out;
    }

    private static ComputeValue negate(ComputeValue v) {
        if (v == null) return null;
        if (v.type() == SpellValueType.NUMBER && v.value() instanceof Number n) {
            return new ComputeValue(SpellValueType.NUMBER, -n.doubleValue());
        }
        return v; // 非数字不处理
    }

    private static boolean reduceBinary(List<Object> tokens, String ops) {
        boolean changed = false;
        while (true) {
            int idx = findOpIndex(tokens, ops);
            if (idx < 0) break;
            if (idx - 1 < 0 || idx + 1 >= tokens.size()) return false;
            if (!(tokens.get(idx - 1) instanceof ComputeValue left)) return false;
            if (!(tokens.get(idx + 1) instanceof ComputeValue right)) return false;
            String op = (String) tokens.get(idx);
            ComputeValue res = calculateBinary(op, left, right);
            if (res == null) return false;
            // 替换 [idx-1 .. idx+1]
            tokens.remove(idx + 1);
            tokens.remove(idx);
            tokens.set(idx - 1, res);
            LOGGER.debug("二元运算规约: {} {} {} -> {}", simpleVal(left), op, simpleVal(right), simpleVal(res));
            changed = true;
        }
        return true;
    }

    private static int findOpIndex(List<Object> tokens, String ops) {
        for (int i = 0; i < tokens.size(); i++) {
            Object t = tokens.get(i);
            if (t instanceof String s) {
                if (ops.contains(s)) return i;
            }
        }
        return -1;
    }

    private static ComputeValue extractSingle(List<Object> tokens) {
        // 返回最后一个值（若仅剩一个值）
        ComputeValue last = null;
        for (Object t : tokens) if (t instanceof ComputeValue v) last = v;
        return last;
    }

    private static ComputeValue calculateBinary(String op, ComputeValue left, ComputeValue right) {
        if (left == null || right == null) return null;
        // 数值运算
        if (left.type() == SpellValueType.NUMBER && right.type() == SpellValueType.NUMBER) {
            double a = toDouble(left.value());
            double b = toDouble(right.value());
            return switch (op) {
                case "+" -> new ComputeValue(SpellValueType.NUMBER, a + b);
                case "-" -> new ComputeValue(SpellValueType.NUMBER, a - b);
                case "*" -> new ComputeValue(SpellValueType.NUMBER, a * b);
                case "/" -> {
                    if (b == 0.0) { LOGGER.warn("数值除零: {}/{}", a, b); yield null; }
                    yield new ComputeValue(SpellValueType.NUMBER, a / b);
                }
                case "^" -> new ComputeValue(SpellValueType.NUMBER, Math.pow(a, b));
                default -> null;
            };
        }
        // 向量 + -
        if (left.type() == SpellValueType.VECTOR3 && right.type() == SpellValueType.VECTOR3) {
            net.minecraft.world.phys.Vec3 lv = (net.minecraft.world.phys.Vec3) left.value();
            net.minecraft.world.phys.Vec3 rv = (net.minecraft.world.phys.Vec3) right.value();
            return switch (op) {
                case "+" -> new ComputeValue(SpellValueType.VECTOR3, new net.minecraft.world.phys.Vec3(lv.x + rv.x, lv.y + rv.y, lv.z + rv.z));
                case "-" -> new ComputeValue(SpellValueType.VECTOR3, new net.minecraft.world.phys.Vec3(lv.x - rv.x, lv.y - rv.y, lv.z - rv.z));
                default -> null;
            };
        }
        // 数字 * 向量 / 向量 * 数字
        if ("*".equals(op)) {
            if (left.type() == SpellValueType.VECTOR3 && right.type() == SpellValueType.NUMBER) {
                net.minecraft.world.phys.Vec3 v = (net.minecraft.world.phys.Vec3) left.value();
                double f = toDouble(right.value());
                return new ComputeValue(SpellValueType.VECTOR3, new net.minecraft.world.phys.Vec3(v.x * f, v.y * f, v.z * f));
            }
            if (left.type() == SpellValueType.NUMBER && right.type() == SpellValueType.VECTOR3) {
                double f = toDouble(left.value());
                net.minecraft.world.phys.Vec3 v = (net.minecraft.world.phys.Vec3) right.value();
                return new ComputeValue(SpellValueType.VECTOR3, new net.minecraft.world.phys.Vec3(v.x * f, v.y * f, v.z * f));
            }
        }
        // 向量 / 数字
        if ("/".equals(op)) {
            if (left.type() == SpellValueType.VECTOR3 && right.type() == SpellValueType.NUMBER) {
                net.minecraft.world.phys.Vec3 v = (net.minecraft.world.phys.Vec3) left.value();
                double b = toDouble(right.value());
                if (b == 0.0) { LOGGER.warn("向量除以零: v/{}, v=({}, {}, {})", b, v.x, v.y, v.z); return null; }
                double f = 1.0 / b;
                return new ComputeValue(SpellValueType.VECTOR3, new net.minecraft.world.phys.Vec3(v.x * f, v.y * f, v.z * f));
            }
        }
        return null;
    }

    private static double toDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0.0;
    }

    private static Character opChar(String registryName) {
        if (registryName == null) return null;
        return switch (registryName) {
            case "compute_add" -> '+';
            case "compute_sub" -> '-';
            case "compute_mul" -> '*';
            case "compute_div" -> '/';
            case "compute_pow" -> '^';
            case "compute_lparen" -> '(';
            case "compute_rparen" -> ')';
            default -> null;
        };
    }

    // 已精简：不再需要按优先级截取左右操作数范围的工具

    private static Object safeVal(Object v) {
        if (v == null) return "<null>";
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof net.minecraft.world.phys.Vec3 vec) return String.format("(%.3f, %.3f, %.3f)", vec.x, vec.y, vec.z);
        return v.toString();
    }

    private static String simpleVal(ComputeValue v) {
        if (v == null) return "<null>";
        return v.type() + ":" + safeVal(v.value());
    }

}
