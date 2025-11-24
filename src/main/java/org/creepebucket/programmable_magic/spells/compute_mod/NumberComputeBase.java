package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

/**
 * 数字类计算法术父类：在本文件内定义并收集 0-9 数字法术
 *
 * 解析策略（KISS）：
 * - 先按括号递归解析（递归下降）；
 * - 幂^ 右结合，乘除，最后加减；含一元负号；
 * - 仅当片段“自包含”（括号配对、末尾为数字或')'）才尝试求值，避免跨段括号误报；
 * - 以二元运算符开头时，用上一次 compute_result 作为首操作数。
 */
public abstract class NumberComputeBase extends SimpleComputeSpell {

    // ========== 注册辅助 ==========
    public static List<Supplier<SpellItemLogic>> allNumberSuppliers() {
        List<Supplier<SpellItemLogic>> list = new ArrayList<>();
        list.add(Num0Spell::new); list.add(Num1Spell::new); list.add(Num2Spell::new); list.add(Num3Spell::new); list.add(Num4Spell::new);
        list.add(Num5Spell::new); list.add(Num6Spell::new); list.add(Num7Spell::new); list.add(Num8Spell::new); list.add(Num9Spell::new);
        return list;
    }

    // ========== 运行 ==========
    @Override
    public boolean run(Player player, SpellData data) {
        // KISS：数字节点本身不做表达式求值，只负责把所有数字按分隔符分割并折叠为字面量，移除分隔符。
        if (data == null) return true;
        ComputeRuntime.normalizeNumbers(data);
        // 让同一索引下一tick重新处理（当前位置很可能已被字面量替换）
        return false;
    }

    // ========== token/工具 ==========
    private List<String> buildTokens(List<SpellItemLogic> seq, int start, int end) {
        List<String> out = new ArrayList<>();
        int i = start;
        while (i <= end) {
            SpellItemLogic s = seq.get(i);
            if (s instanceof NumberComputeBase) {
                StringBuilder digits = new StringBuilder();
                while (i <= end && seq.get(i) instanceof NumberComputeBase nb) { digits.append(numDigit(nb)); i++; }
                out.add(digits.toString());
                continue;
            }
            if (s instanceof OperatorComputeBase op) {
                Character ch = opChar(op);
                if (ch != null) out.add(String.valueOf(ch));
            }
            i++;
        }
        return out;
    }

    private boolean looksSelfContained(List<String> tokens) {
        if (tokens.isEmpty()) return false;
        String last = tokens.get(tokens.size() - 1);
        if (!(isNumber(last) || ")".equals(last))) return false;
        Deque<Character> st = new ArrayDeque<>();
        for (String t : tokens) {
            if ("(".equals(t)) st.push('(');
            else if (")".equals(t)) { if (st.isEmpty()) return false; st.pop(); }
        }
        return st.isEmpty();
    }

    private boolean isNumber(String s) {
        if (s == null || s.isEmpty()) return false;
        int i = 0;
        if (s.charAt(0) == '-') {
            if (s.length() == 1) return false;
            i = 1;
        }
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }
    private boolean isBinaryOp(String s) { return s != null && ("+ - * / ^".contains(s) && s.length() == 1); }

    protected boolean isCompute(SpellItemLogic s) { return s instanceof NumberComputeBase || s instanceof OperatorComputeBase; }

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

    // 解析器：
    // Expr -> Term ((+|-) Term)*
    // Term -> Pow ((*|/) Pow)*
    // Pow  -> Unary (^ Pow)?  (右结合)
    // Unary-> '-' Unary | Primary
    // Primary -> number | '(' Expr ')'
    private static final class Parser {
        private final List<String> t;
        private int p = 0;
        Parser(List<String> tokens) { this.t = tokens; }
        boolean atEnd() { return p >= t.size(); }
        String peek() { return atEnd() ? null : t.get(p); }
        String next() { return atEnd() ? null : t.get(p++); }

        Double parseExpression() {
            Double v = parseTerm();
            while (!atEnd()) {
                String op = peek();
                if (!"+".equals(op) && !"-".equals(op)) break;
                next();
                Double rhs = parseTerm();
                if (v == null || rhs == null) return null;
                v = "+".equals(op) ? v + rhs : v - rhs;
            }
            return v;
        }

        Double parseTerm() {
            Double v = parsePow();
            while (!atEnd()) {
                String op = peek();
                if (!"*".equals(op) && !"/".equals(op)) break;
                next();
                Double rhs = parsePow();
                if (v == null || rhs == null) return null;
                if ("/".equals(op)) {
                    if (rhs == 0.0) throw new ArithmeticException("除以0");
                    v = v / rhs;
                } else v = v * rhs;
            }
            return v;
        }

        Double parsePow() {
            Double base = parseUnary();
            if (!atEnd() && "^".equals(peek())) {
                next();
                Double exp = parsePow(); // 右结合
                if (base == null || exp == null) return null;
                return Math.pow(base, exp);
            }
            return base;
        }

        Double parseUnary() {
            if (!atEnd() && "-".equals(peek())) { next(); Double v = parseUnary(); return v == null ? null : -v; }
            return parsePrimary();
        }

        Double parsePrimary() {
            String s = peek();
            if (s == null) return null;
            if ("(".equals(s)) {
                next();
                Double inner = parseExpression();
                String r = next();
                if (!")".equals(r)) return null; // 未闭合（looksSelfContained 已拦截）
                return inner;
            }
            try { next(); return Double.parseDouble(s); }
            catch (NumberFormatException e) { return null; }
        }
    }

    // ========== 数字工具 ==========
    protected int numDigit(NumberComputeBase n) {
        String rn = n.getRegistryName();
        if (rn != null && rn.startsWith("compute_") && rn.length() > 8) {
            char c = rn.charAt(rn.length() - 1);
            if (c >= '0' && c <= '9') return c - '0';
        }
        return 0;
    }
    public int digitValue() { return numDigit(this); }
}

// 以下为 0-9 的具体数字法术（放在父类文件内）
class Num0Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_0"; } }
class Num1Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_1"; } }
class Num2Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_2"; } }
class Num3Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_3"; } }
class Num4Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_4"; } }
class Num5Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_5"; } }
class Num6Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_6"; } }
class Num7Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_7"; } }
class Num8Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_8"; } }
class Num9Spell extends NumberComputeBase { @Override public String getRegistryName() { return "compute_9"; } }
