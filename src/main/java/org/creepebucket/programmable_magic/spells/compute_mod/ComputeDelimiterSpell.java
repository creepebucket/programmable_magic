package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.List;

/**
 * 计算表达式分隔符，占位用。用于在预处理时作为表达式分割标记。
 * 不产生基础耗魔，也不修改耗魔。
 */
public class ComputeDelimiterSpell extends BaseComputeModLogic {
    @Override
    public String getRegistryName() { return "compute_mod"; }

    @Override
    public boolean run(Player player, SpellData data) { return true; }

    @Override
    public void calculateBaseMana(SpellData data) { /* no-op */ }

    @Override
    public void applyManaModification(SpellData data) { /* delimiter 不修改 */ }

    @Override
    public List<Component> getTooltip() {
        java.util.List<org.creepebucket.programmable_magic.spells.SpellValueType> in = java.util.List.of(
                org.creepebucket.programmable_magic.spells.SpellValueType.ANY
        );
        org.creepebucket.programmable_magic.spells.SpellValueType out = org.creepebucket.programmable_magic.spells.SpellValueType.ANY;
        Component desc = Component.literal("表达式分隔符");
        return org.creepebucket.programmable_magic.spells.SpellTooltipUtil.buildTooltip(new java.util.ArrayList<>(in), out, desc, this);
    }
}
