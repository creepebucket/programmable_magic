package org.creepebucket.programmable_magic.spells.adjust_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;

import java.util.ArrayList;
import java.util.List;

public class PowerMultiplierSpell extends BaseAdjustModLogic {
    
    public PowerMultiplierSpell() {
        super();
    }

    @Override
    public String getRegistryName() {
        return "mpx5";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        // 这个法术只修改数据，不执行实际效果
        return true;
    }
    
    @Override
    public void calculateBaseMana(SpellData data) {
        // 调整法术本身不消耗魔力
    }
    
    @Override
    public void applyManaModification(SpellData data) {
        // 将威力乘以5
        data.setPower(data.getPower() * 5.0);
        
        // 将所有魔力消耗乘以5
        for (String manaType : new String[]{"radiation", "temperature", "momentum", "pressure"}) {
            data.multiplyManaCost(manaType, 5.0);
        }
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.spell_modifier"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.power_multiplier_x5"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost_multiplier_x5"));
        return tooltip;
    }
} 