package org.creepebucket.programmable_magic.spells.adjust_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.adjust_mod.BaseAdjustModLogic;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeArgsHelper;
import org.creepebucket.programmable_magic.spells.compute_mod.ComputeValue;

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
        Integer idx = data.getCustomData("__idx", Integer.class);
        ComputeValue arg = idx != null ? ComputeArgsHelper.collectSingleArg(data, idx) : null;
        double sec = extractNumber(arg, 0.0);
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
        java.util.List<org.creepebucket.programmable_magic.spells.SpellValueType> in = java.util.List.of(
                org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER
        );
        org.creepebucket.programmable_magic.spells.SpellValueType out = org.creepebucket.programmable_magic.spells.SpellValueType.MODIFIER;
        Component desc = Component.translatable("tooltip.programmable_magic.delay_by_expression_seconds");
        return org.creepebucket.programmable_magic.spells.SpellTooltipUtil.buildTooltip(new java.util.ArrayList<>(in), out, desc, this);
    }

    private double extractNumber(ComputeValue value, double fallback) {
        if (value == null || value.value() == null) return fallback;
        if (value.value() instanceof Number num) return num.doubleValue();
        return fallback;
    }
}
