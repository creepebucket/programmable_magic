package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellTooltipUtil;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeRuntime;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 短途传送（法术实体）：把法术实体沿给定 deltaPos 传送。
 * 耗能：在 1 tick 内位移到目标并停下的动能，E = m*v^2，v = |Δx|*20 m/s。
 * 封顶 10 kJ，超出则报错且不执行。
 */
public class BlinkSpell extends AbstractBlinkBaseSpell {
    @Override
    public String getRegistryName() { return "blink_spell"; }

    @Override
    public boolean run(Player player, SpellData data) { return super.run(player, data); }

    @Override
    public void calculateBaseMana(SpellData data) { super.calculateBaseMana(data); }

    @Override
    protected Entity resolveTarget(Player player, SpellData data, int idx) {
        return data != null && data.getTarget() instanceof SpellEntity se ? se : null;
    }

    @Override
    public List<Component> getTooltip() {
        List<SpellValueType> in = new ArrayList<>();
        in.add(SpellValueType.MODIFIER);
        in.add(SpellValueType.VECTOR3);
        Component desc = Component.literal("短途传送(法术)：Δpos；耗能≈m·(|Δ|·20)^2/1000，封顶10MJ");
        return SpellTooltipUtil.buildTooltip(in, SpellValueType.SPELL, desc, this);
    }
}
