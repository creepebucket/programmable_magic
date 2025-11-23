package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.NumberLiteralSpell;
import org.creepebucket.programmable_magic.spells.compute_mod.NumberComputeBase;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责 compute_mod 运行时公共逻辑：参数获取、类型校验、结果记录等。
 */
public final class ComputeRuntime {
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

    public static List<ComputeValue> collectArgs(SpellData data, int currentIndex, ArgSpec spec) {
        if (data == null || spec == null) return List.of();
        int max = Math.max(0, spec.maxArgs());
        List<ComputeValue> values = ComputeArgsHelper.collectArgs(data, currentIndex, max);
        return values;
    }

    public static String validateArgs(List<ComputeValue> args, ArgSpec spec) {
        if (spec == null) return null;
        if (args.size() < spec.requiredArgs()) {
            return "缺少计算参数，期望 " + spec.requiredArgs() + " 个";
        }
        SpellValueType[] expected = spec.expectedTypes();
        if (expected == null) return null;
        for (int i = 0; i < Math.min(expected.length, args.size()); i++) {
            SpellValueType expect = expected[i];
            if (expect == null) continue;
            ComputeValue arg = args.get(i);
            if (arg == null || arg.type() != expect) {
                SpellValueType actual = arg == null ? null : arg.type();
                return String.format("参数 #%d 类型错误: 期望 %s, 实际 %s",
                        i + 1,
                        display(expect),
                        display(actual));
            }
        }
        return null;
    }

    private static String display(SpellValueType type) {
        return type == null ? "未知" : type.display();
    }

    public static void sendError(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) return;
        player.displayClientMessage(Component.literal("§c" + message), true);
    }

    public static void recordProvidedValue(SpellData data, SpellItemLogic logic, int index) {
        if (data == null || logic == null) return;
        if (logic instanceof ComputeValueProvider provider) {
            ComputeValue value = provider.getProvidedValue();
            if (value != null) {
                data.recordComputeValue(index, value);
            }
        }
    }

    public static ComputeValue ensureValueAt(Player player, SpellData data, int index) {
        if (data == null) return null;
        ComputeValue existing = data.getComputeValue(index);
        if (existing != null) return existing;
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return null;
        if (index < 0 || index >= raw.size()) return null;
        SpellItemLogic logic = (SpellItemLogic) raw.get(index);
        if (logic.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) return null;

        Integer previousIdx = data.getCustomData("__idx", Integer.class);
        data.setCustomData("__idx", index);
        try {
            logic.run(player, data);
            recordProvidedValue(data, logic, index);
            data.markIndexSkipped(index);
        } finally {
            if (previousIdx != null) {
                data.setCustomData("__idx", previousIdx);
            } else {
                data.clearCustomData("__idx");
            }
        }
        return data.getComputeValue(index);
    }

    public static ComputeValue findRightValue(Player player, SpellData data, int currentIndex) {
        if (data == null) return null;
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw)) return null;
        if (currentIndex < 0 || currentIndex >= raw.size()) return null;
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;
        for (int idx = currentIndex + 1; idx < seq.size(); idx++) {
            SpellItemLogic logic = seq.get(idx);
            if (ComputeArgsHelper.isDelimiter(logic)) break;
            if (logic.getSpellType() != SpellItemLogic.SpellType.COMPUTE_MOD) break;
            ComputeValue value = data.getComputeValue(idx);
            if (value == null) {
                value = literalValueForward(data, seq, idx);
            }
            if (value == null) {
                value = ensureValueAt(player, data, idx);
            }
            if (value != null) return value;
        }
        return null;
    }

    private static ComputeValue literalValueForward(SpellData data, List<SpellItemLogic> sequence, int startIndex) {
        if (sequence == null || startIndex < 0 || startIndex >= sequence.size()) return null;
        int idx = startIndex;
        StringBuilder digits = new StringBuilder();
        while (idx < sequence.size()) {
            SpellItemLogic logic = sequence.get(idx);
            if (logic instanceof NumberLiteralSpell literal) {
                double val = literal.getValue();
                data.markIndexSkipped(idx);
                ComputeValue cv = new ComputeValue(SpellValueType.NUMBER, val);
                data.recordComputeValue(startIndex, cv);
                return cv;
            }
            if (logic instanceof NumberComputeBase num) {
                digits.append(num.digitValue());
                data.markIndexSkipped(idx);
                idx++;
                continue;
            }
            break;
        }
        if (digits.length() == 0) return null;
        double val = Double.parseDouble(digits.toString());
        ComputeValue cv = new ComputeValue(SpellValueType.NUMBER, val);
        data.recordComputeValue(startIndex, cv);
        return cv;
    }
}
