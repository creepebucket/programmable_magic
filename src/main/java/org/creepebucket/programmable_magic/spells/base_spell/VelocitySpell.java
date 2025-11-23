package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.ModUtils;

import java.util.ArrayList;
import java.util.List;

public class VelocitySpell extends BaseSpellEffectLogic {
    
    public VelocitySpell() {
        super();
    }

    @Override
    public String getRegistryName() {
        return "gain_velocity";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        // 给法术实体（载体）施加速度：优先使用 Vector3 参数作为方向
        Entity carrier = data.getTarget();
        if (carrier != null) {
            Vec3 v = data.getCustomData("vector_xyz", Vec3.class);
            Vec3 dir = (v != null && v.lengthSqr() > 0) ? v.normalize() : data.getDirection();
            double force = 0.5 * Math.max(0.0, data.getPower());
            Vec3 velocity = dir.scale(force);
            carrier.setDeltaMovement(carrier.getDeltaMovement().add(velocity));
        }
        
        // 速度法术立即完成
        return true;
    }
    
    @Override
    public void calculateBaseMana(SpellData data) {
        // 速度法术消耗动量系魔力
        data.setManaCost("momentum", 0.05);
    }

    @Override
    public List<Component> getTooltip() {
        java.util.List<org.creepebucket.programmable_magic.spells.SpellValueType> in = new java.util.ArrayList<>();
        in.add(org.creepebucket.programmable_magic.spells.SpellValueType.MODIFIER); // ...Modifier
        in.add(org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3);
        org.creepebucket.programmable_magic.spells.SpellValueType out = org.creepebucket.programmable_magic.spells.SpellValueType.SPELL;
        var desc = net.minecraft.network.chat.Component.literal("为载体施加速度（方向取自向量）");
        return org.creepebucket.programmable_magic.spells.SpellTooltipUtil.buildTooltip(in, out, desc, this);
    }
} 
