package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.ArrayList;
import java.util.List;

public class CasterEntitySpell extends ComputeFunctionalSpell {

    @Override
    public String getRegistryName() { return "compute_caster"; }

    @Override
    protected ComputeRuntime.ArgSpec argumentSpec() {
        return ComputeRuntime.ArgSpec.fixed(0);
    }

    @Override
    protected ComputeValue compute(Player player, SpellData data, List<ComputeValue> args) {
        Player caster = data != null && data.getCaster() != null ? data.getCaster() : player;
        return caster != null ? new ComputeValue(SpellValueType.ENTITY, caster) : null;
    }

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        Component desc = Component.literal("施法者本体 (Entity)");
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.ENTITY, desc, this);
    }
}
