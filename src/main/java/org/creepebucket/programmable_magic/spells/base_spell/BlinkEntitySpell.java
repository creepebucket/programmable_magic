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
 * 短途传送（实体）：将给定实体沿 deltaPos 传送。
 * 耗能同 BlinkSpell，使用实体质量。
 */
public class BlinkEntitySpell extends AbstractBlinkBaseSpell {
    @Override
    public String getRegistryName() { return "blink_entity"; }

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
        Component desc = Component.literal("短途传送(实体)：Δpos；耗能≈m·(|Δ|·20)^2/1000，封顶10MJ");
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.SPELL, desc, this);
    }
}
