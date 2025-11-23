package org.creepebucket.programmable_magic.spells.compute_mod;

import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 数字类计算法术父类：在本文件内定义并收集 0-9 数字法术
 */
public abstract class NumberComputeBase extends SimpleComputeSpell {

    /**
     * 返回 0-9 所有数字法术的 Supplier，用于注册
     */
    public static List<Supplier<SpellItemLogic>> allNumberSuppliers() {
        List<Supplier<SpellItemLogic>> list = new ArrayList<>();
        list.add(Num0Spell::new);
        list.add(Num1Spell::new);
        list.add(Num2Spell::new);
        list.add(Num3Spell::new);
        list.add(Num4Spell::new);
        list.add(Num5Spell::new);
        list.add(Num6Spell::new);
        list.add(Num7Spell::new);
        list.add(Num8Spell::new);
        list.add(Num9Spell::new);
        return list;
    }

    @Override
    public boolean run(net.minecraft.world.entity.player.Player player, org.creepebucket.programmable_magic.spells.SpellData data) {
        // 仅在数字-符号串尾部的“数字”上进行计算
        if (data == null) return true;

        Object seqObj = data.getCustomData("__seq", Object.class);
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (!(seqObj instanceof List) || idx == null) return true;

        @SuppressWarnings("unchecked")
        List<org.creepebucket.programmable_magic.spells.SpellItemLogic> seq =
                (List<org.creepebucket.programmable_magic.spells.SpellItemLogic>) seqObj;

        if (idx < 0 || idx >= seq.size()) return true;

        // 找到当前连续的 compute_mod 片段 [start, end]
        int start = idx;
        while (start - 1 >= 0 && isCompute(seq.get(start - 1))) start--;
        int end = idx;
        while (end + 1 < seq.size() && isCompute(seq.get(end + 1))) end++;

        // 仅当当前索引是片段的末尾，且末尾是数字，才计算
        if (idx != end) return true;
        if (!(seq.get(end) instanceof NumberComputeBase)) return true;

        // 构造 token 列表（数字/运算符/括号）
        List<String> tokens = new ArrayList<>();
        int j = start;
        while (j <= end) {
            if (seq.get(j) instanceof NumberComputeBase num) {
                StringBuilder digits = new StringBuilder();
                while (j <= end && (seq.get(j) instanceof NumberComputeBase num2)) {
                    digits.append(numDigit(num2));
                    j++;
                }
                tokens.add(digits.toString());
            } else if (seq.get(j) instanceof OperatorComputeBase op) {
                Character ch = opChar(op);
                if (ch != null) tokens.add(String.valueOf(ch));
                j++;
            } else {
                j++;
            }
        }

        // 归一化一元负号：在表达式开头或'('或二元运算符之后，出现的 '-' 与其后的数字合并为负数
        tokens = normalizeUnaryMinus(tokens);

        // 若以二元运算符开头（非左括号），在前面插入缓存结果作为首操作数
        Double prev = data.getCustomData("compute_result", Double.class);
        double cached = (prev != null) ? prev : 0.0;
        if (!tokens.isEmpty()) {
            String first = tokens.get(0);
            if (isBinaryOp(first) && !first.equals("(")) {
                tokens.add(0, String.valueOf(cached));
            }
        }

        StringBuilder err = new StringBuilder();
        Double result = evaluateTokens(tokens, err);
        if (result != null && !result.isNaN() && !result.isInfinite()) {
            data.setCustomData("compute_result", result);
            Integer selfIdx = data.getCustomData("__idx", Integer.class);
            if (selfIdx != null) {
                data.recordComputeValue(selfIdx, new ComputeValue(SpellValueType.NUMBER, result));
            }
        } else {
            // 错误：提示并给予 3 点真伤
            if (player != null) {
                String msg = err.length() > 0 ? err.toString() : "表达式错误";
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c" + msg), false);
                // 给予真伤（直接扣除生命值，忽略护甲/抗性）
                try {
                    float hp = player.getHealth();
                    player.setHealth(Math.max(0f, hp - 3f));
                } catch (Exception ignored) {}
            }
        }
        return true;
    }

    // 获取数字法术所代表的数字（通过 registryName 的末位）
    protected int numDigit(NumberComputeBase n) {
        String rn = n.getRegistryName();
        if (rn != null && rn.startsWith("compute_") && rn.length() > 8) {
            char c = rn.charAt(rn.length() - 1);
            if (c >= '0' && c <= '9') return c - '0';
        }
        return 0;
    }

    public int digitValue() {
        return numDigit(this);
    }

    protected boolean isCompute(org.creepebucket.programmable_magic.spells.SpellItemLogic s) {
        return s instanceof NumberComputeBase || s instanceof OperatorComputeBase;
    }

    protected Character opChar(OperatorComputeBase op) {
        String rn = op.getRegistryName();
        if (rn == null) return null;
        return switch (rn) {
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

    private boolean isBinaryOp(String s) {
        return s != null && (s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("^"));
    }

    private int precedence(String op) {
        return switch (op) {
            case "^" -> 4;
            case "*", "/" -> 3;
            case "+", "-" -> 2;
            default -> 0;
        };
    }

    private boolean isRightAssoc(String op) {
        return "^".equals(op);
    }

    private Double evaluateTokens(List<String> tokens, StringBuilder error) {
        if (tokens == null || tokens.isEmpty()) { error.append("表达式为空"); return Double.NaN; }

        java.util.Deque<Double> values = new java.util.ArrayDeque<>();
        java.util.Deque<String> ops = new java.util.ArrayDeque<>();

        int n = tokens.size();
        for (int i = 0; i < n; i++) {
            String t = tokens.get(i);
            if (t.isEmpty()) continue;

            if (t.equals("(")) {
                ops.push(t);
            } else if (t.equals(")")) {
                boolean matched = false;
                while (!ops.isEmpty()) {
                    if (ops.peek().equals("(")) { ops.pop(); matched = true; break; }
                    if (!applyTop(values, ops.pop(), error)) return Double.NaN;
                }
                if (!matched) { error.append("括号未闭合"); return Double.NaN; }
            } else if (isBinaryOp(t)) {
                while (!ops.isEmpty() && !ops.peek().equals("(") &&
                        (isRightAssoc(t) ? precedence(t) < precedence(ops.peek()) : precedence(t) <= precedence(ops.peek()))) {
                    if (!applyTop(values, ops.pop(), error)) return Double.NaN;
                }
                ops.push(t);
            } else {
                // number
                try {
                    values.push(Double.parseDouble(t));
                } catch (NumberFormatException e) {
                    error.append("非法数字: ").append(t);
                    return Double.NaN;
                }
            }
        }

        while (!ops.isEmpty()) {
            if (ops.peek().equals("(")) { error.append("括号未闭合"); return Double.NaN; }
            if (!applyTop(values, ops.pop(), error)) return Double.NaN;
        }

        if (values.isEmpty()) { error.append("表达式无结果"); return Double.NaN; }
        return values.pop();
    }

    private boolean applyTop(java.util.Deque<Double> values, String op, StringBuilder error) {
        if (values.size() < 2) { error.append("符号缺少数字: ").append(op); return false; }
        double b = values.pop();
        double a = values.pop();
        double r;
        switch (op) {
            case "+" -> r = a + b;
            case "-" -> r = a - b;
            case "*" -> r = a * b;
            case "/" -> {
                if (b == 0.0) { error.append("除以0"); return false; }
                r = a / b;
            }
            case "^" -> r = Math.pow(a, b);
            default -> { error.append("未知运算符: ").append(op); return false; }
        }
        values.push(r);
        return true;
    }

    private List<String> normalizeUnaryMinus(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return tokens;
        List<String> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if ("-".equals(t)) {
                String prev = out.isEmpty() ? null : out.get(out.size() - 1);
                boolean unary = (out.isEmpty() || "(".equals(prev) || isBinaryOp(prev));
                if (unary && i + 1 < tokens.size()) {
                    String next = tokens.get(i + 1);
                    if (next.matches("\\d+")) { // 将 -number 合并
                        out.add("-" + next);
                        i++; // 跳过 next
                        continue;
                    }
                }
            }
            out.add(t);
        }
        return out;
    }
}

// 以下为 0-9 的具体数字法术（放在父类文件内）
class Num0Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_0"; }
}

class Num1Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_1"; }
}

class Num2Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_2"; }
}

class Num3Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_3"; }
}

class Num4Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_4"; }
}

class Num5Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_5"; }
}

class Num6Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_6"; }
}

class Num7Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_7"; }
}

class Num8Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_8"; }
}

class Num9Spell extends NumberComputeBase {
    @Override
    public String getRegistryName() { return "compute_9"; }
}
