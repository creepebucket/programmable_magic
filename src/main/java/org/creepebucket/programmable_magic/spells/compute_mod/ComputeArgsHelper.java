package org.creepebucket.programmable_magic.spells.compute_mod;

import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComputeArgsHelper {
    private ComputeArgsHelper() {}

    static List<ComputeValue> collectArgs(SpellData data, int currentIndex, int required) {
        Object seqObj = data.getCustomData("__seq", Object.class);
        if (!(seqObj instanceof List<?> raw) || required <= 0) return List.of();
        @SuppressWarnings("unchecked")
        List<SpellItemLogic> seq = (List<SpellItemLogic>) raw;

        List<ComputeValue> args = new ArrayList<>();
        int idx = currentIndex - 1;
        while (idx >= 0 && args.size() < required) {
            // 跳过连续分隔符
            while (idx >= 0 && isDelimiter(seq.get(idx))) idx--;
            if (idx < 0) break;

            ComputeValue value = data.getComputeValue(idx);
            if (value == null) {
                value = ComputeRuntime.ensureValueAt(null, data, idx);
            }
            if (value != null) {
                args.add(value);
                idx--;
                // 向左移动直到遇到分隔符，保证每段只取一个值
                while (idx >= 0 && !isDelimiter(seq.get(idx))) {
                    idx--;
                }
            } else {
                idx--;
            }
        }
        Collections.reverse(args);
        return args;
    }

    public static ComputeValue collectSingleArg(SpellData data, int currentIndex) {
        List<ComputeValue> args = collectArgs(data, currentIndex, 1);
        return args.isEmpty() ? null : args.getFirst();
    }

    static boolean isDelimiter(SpellItemLogic logic) {
        return logic != null
                && logic.getSpellType() == SpellItemLogic.SpellType.COMPUTE_MOD
                && "compute_mod".equals(logic.getRegistryName());
    }
}
