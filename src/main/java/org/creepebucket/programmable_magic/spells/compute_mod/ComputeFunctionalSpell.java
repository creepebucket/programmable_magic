package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.List;

/**
 * 统一处理参数获取与类型校验的 compute_mod 基类。
 */
public abstract class ComputeFunctionalSpell extends SimpleComputeSpell implements ComputeValueProvider {
    private ComputeValue providedValue;

    @Override
    public final boolean run(Player player, SpellData data) {
        if (data == null) {
            providedValue = null;
            return true;
        }
        Integer idx = data.getCustomData("__idx", Integer.class);
        if (idx == null) {
            providedValue = null;
            return true;
        }

        ComputeRuntime.ArgSpec spec = argumentSpec();
        List<ComputeValue> args = ComputeRuntime.collectArgs(data, idx, spec);
        String error = ComputeRuntime.validateArgs(args, spec);
        if (error != null) {
            ComputeRuntime.sendError(player, error);
            providedValue = null;
            return true;
        }

        providedValue = compute(player, data, args);
        return true;
    }

    protected abstract ComputeRuntime.ArgSpec argumentSpec();

    protected abstract ComputeValue compute(Player player, SpellData data, List<ComputeValue> args);

    @Override
    public ComputeValue getProvidedValue() {
        return providedValue;
    }
}
