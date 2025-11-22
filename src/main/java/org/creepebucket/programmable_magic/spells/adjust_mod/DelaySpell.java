package org.creepebucket.programmable_magic.spells.adjust_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.adjust_mod.BaseAdjustModLogic;

import java.util.ArrayList;
import java.util.List;

public class DelaySpell extends BaseAdjustModLogic {
    
    public DelaySpell() {
        super();
    }

    @Override
    public String getRegistryName() {
        return "delay";
    }

    @Override
    public boolean run(Player player, SpellData data) {
        // 依据前面表达式的运算结果设置延时（单位：秒）
        Double seconds = data.getCustomData("compute_result", Double.class);
        double sec = seconds != null ? seconds : 0.0;
        if (Double.isNaN(sec) || Double.isInfinite(sec)) sec = 0.0;
        // 负数视为0，保留小数到tick
        int ticks = (int) Math.max(0, Math.round(sec * 20.0));
        data.setDelay(ticks);
        return true;
    }
    
    @Override
    public void calculateBaseMana(SpellData data) { /* 调整法术本身不消耗魔力 */ }
    
    @Override
    public void applyManaModification(SpellData data) { /* 延时不修改耗魔 */ }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.programmable_magic.spell_modifier"));
        tooltip.add(Component.translatable("tooltip.programmable_magic.delay_by_expression_seconds"));
        return tooltip;
    }
}
