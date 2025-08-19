package org.creepebucket.programmable_magic.spells.base_spell;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;

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
        // 仅当载体为投射物时生效
        Boolean isProjectile = data.getCustomData("isProjectile", Boolean.class);
        if (isProjectile == null || !isProjectile) {
            return true;
        }

        // 给法术实体（载体）施加速度，而不是玩家
        Entity carrier = data.getTarget();
        if (carrier != null) {
            Vec3 direction = data.getDirection();
            double force = 0.5 * data.getPower();
            Vec3 velocity = direction.scale(force);
            carrier.setDeltaMovement(carrier.getDeltaMovement().add(velocity));
        }
        
        // 速度法术立即完成
        return true;
    }
    
    @Override
    public void calculateBaseMana(SpellData data) {
        // 速度法术消耗动量系魔力
        data.setManaCost("momentum", 3.0);
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost"));
        tooltip.add(Component.literal("  Momentum: 3.0"));
        return tooltip;
    }
} 