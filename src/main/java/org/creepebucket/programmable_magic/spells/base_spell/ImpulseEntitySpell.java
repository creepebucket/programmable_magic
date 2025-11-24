package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;
import org.creepebucket.programmable_magic.util.WeightUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 给予实体动量：将实体瞬时速度增加给定向量（m/s）。
 * 若存在威力（power）>1，则将向量乘以 power。
 * 耗魔按动能增量近似：0.5*m*|dv|^2（kJ -> mana）。
 */
public class ImpulseEntitySpell extends AbstractImpulseBaseSpell {
    @Override
    public String getRegistryName() { return "impulse_entity"; }

    @Override
    public boolean run(Player player, SpellData data) { return super.run(player, data); }

    @Override
    public void calculateBaseMana(SpellData data) { super.calculateBaseMana(data); }

    @Override
    protected Entity resolveTarget(Player player, SpellData data, int idx) {
        // KISS: 规范化后，期望布局为 [ENTITY, VECTOR3, BASE]
        ComputeValue eArg = ComputeRuntime.valueAtExact(player, data, idx - 2);
        return eArg != null && eArg.type() == SpellValueType.ENTITY && eArg.value() instanceof Entity e ? e : null;
    }

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        in.add(SpellValueType.MODIFIER);
        in.add(SpellValueType.ENTITY);
        in.add(SpellValueType.VECTOR3);
        Component desc = Component.literal("给予实体动量：Δv(m/s)，受威力影响则向量×power");
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.SPELL, desc, this);
    }
}
