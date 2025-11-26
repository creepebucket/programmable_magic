package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.spells.SpellData;

import java.util.List;

public abstract class MathOpreationsSpell extends BaseComputeModLogic{
    public String registryName = "";
    public List<Component> tooltip;

    public MathOpreationsSpell(String registryName, List<Component> tooltip) {
        this.registryName = registryName;
        this.tooltip = tooltip;
    }

    @Override
    public String getRegistryName() {
        return registryName;
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() {
        return tooltip;
    }
}
