package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class ValueLiteralSpell extends BaseComputeModLogic{

    public SpellValueType VALUE_TYPE;
    public String REGISTRY_NAME;
    public List<Component> TOOLTIP;
    public Object VALUE;

    public ValueLiteralSpell(SpellValueType valueType, Object value) {
        this(valueType, "internal_value", value, List.of());
    }

    public ValueLiteralSpell(SpellValueType valueType, String registryName, Object value, List<Component> tooltip) {
        VALUE_TYPE = valueType;
        VALUE = value;
        REGISTRY_NAME = registryName;
        TOOLTIP = tooltip;
    }

    @Override
    public String getRegistryName() {return REGISTRY_NAME;}

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful", true);
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() {return TOOLTIP;}

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of();
    }
}
