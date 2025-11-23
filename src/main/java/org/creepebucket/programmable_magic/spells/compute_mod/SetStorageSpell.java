package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SetStorageSpell extends ComputeFunctionalSpell {
    @Override
    public String getRegistryName() { return "compute_set_storage"; }

    @Override
    protected ComputeRuntime.ArgSpec argumentSpec() {
        SpellValueType[] types = new SpellValueType[]{
                SpellValueType.ANY,
                SpellValueType.ANY
        };
        return ComputeRuntime.ArgSpec.fixed(2, types);
    }

    @Override
    protected ComputeValue compute(Player player, SpellData data, List<ComputeValue> args) {
        if (data == null) return null;
        String key = buildKey(args.get(0));
        if (key == null) {
            ComputeRuntime.sendError(player, "存储索引缺失");
            return null;
        }
        data.putStorageValue(key, args.get(1));
        return null;
    }

    private String buildKey(ComputeValue value) {
        if (value == null) return null;
        Object raw = value.value();
        if (raw == null) return null;
        if (raw instanceof Number num) {
            double d = num.doubleValue();
            long l = (long) d;
            if (Math.abs(d - l) < 1e-9) return Long.toString(l);
            return String.format(Locale.ROOT, "%.6f", d);
        }
        return raw.toString();
    }

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        in.add(SpellValueType.ANY);
        in.add(SpellValueType.ANY);
        Component desc = Component.literal("设置存储槽: 左参数为索引，右参数为值");
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.ANY, desc, this);
    }
}
