package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.ArrayList;
import java.util.List;

public class DelaySpell extends BaseControlModLogic {
    
    public DelaySpell() {
        super();
    }

    @Override
    public String getRegistryName() {
        return "delay_1s";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        // 添加1秒延时（20 ticks）
        data.setDelay(20);
        return true;
    }
    
    @Override
    public void calculateBaseMana(SpellData data) {
        // 控制法术本身不消耗魔力
    }
    
    @Override
    public void applyManaModification(SpellData data) {
        // 延时法术将魔力消耗提升到1.05次方
        for (String manaType : new String[]{"radiation", "temperature", "momentum", "pressure"}) {
            data.powerManaCost(manaType, 1.05);
        }
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.spell_modifier"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.delay_1s"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.mana_cost_power_1.05"));
        return tooltip;
    }
}

