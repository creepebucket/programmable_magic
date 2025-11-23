package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.ArrayList;
import java.util.List;

public class GetStorageSpell extends ComputeFunctionalSpell {

    @Override
    public String getRegistryName() { return "compute_get_storage"; }

    @Override
    protected ComputeRuntime.ArgSpec argumentSpec() {
        return ComputeRuntime.ArgSpec.fixed(1, SpellValueType.ANY);
    }

    @Override
    protected ComputeValue compute(Player player, SpellData data, List<ComputeValue> args) {
        if (data == null) return null;
        ComputeValue first = args.isEmpty() ? null : args.get(0);
        String key = first == null || first.value() == null ? null : first.value().toString();
        if (key == null) {
            ComputeRuntime.sendError(player, "读取存储: 索引不能为空");
            return null;
        }
        return data.getStorageValue(key);
    }

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        in.add(SpellValueType.ANY);
        Component desc = Component.literal("读取存储槽: 输入为索引");
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.ANY, desc, this);
    }
}
