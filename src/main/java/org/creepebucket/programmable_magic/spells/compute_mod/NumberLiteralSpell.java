package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.Collections;
import java.util.List;

/**
 * 预处理阶段产生的数字字面量节点，直接携带一个 double 值。
 * 不参与注册（不会作为物品出现），仅作为运行时序列节点。
 */
public class NumberLiteralSpell extends SimpleComputeSpell implements ComputeValueProvider {
    private final double value;
    private final ComputeValue providedValue;

    public NumberLiteralSpell(double value) {
        this.value = value;
        this.providedValue = new ComputeValue(SpellValueType.NUMBER, value);
    }

    public double getValue() { return value; }

    @Override
    public String getRegistryName() {
        return "compute_num"; // 仅作为识别；不会注册为物品
    }

    @Override
    public boolean run(Player player, SpellData data) {
        return true;
    }

    @Override
    public ComputeValue getProvidedValue() {
        return providedValue;
    }

    @Override
    public List<Component> getTooltip() {
        // 运行时生成节点，不需要在物品上显示
        return Collections.emptyList();
    }
}
