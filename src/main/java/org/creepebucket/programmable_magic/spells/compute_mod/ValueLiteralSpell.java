package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.Collections;
import java.util.List;

/**
 * 运行期生成的通用字面量节点（可承载任意 ComputeValue）。
 * 不参与注册，仅作为序列中的中间结果。
 */
public class ValueLiteralSpell extends SimpleComputeSpell implements ComputeValueProvider {
    private final ComputeValue providedValue;

    public ValueLiteralSpell(ComputeValue value) {
        this.providedValue = value;
    }

    @Override
    public String getRegistryName() { return "compute_lit"; }

    @Override
    public boolean run(Player player, SpellData data) { return true; }

    @Override
    public ComputeValue getProvidedValue() { return providedValue; }

    @Override
    public List<Component> getTooltip() { return Collections.emptyList(); }
}

