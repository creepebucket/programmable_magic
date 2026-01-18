package org.creepebucket.programmable_magic.spells.old.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class ValueLiteralSpell extends BaseComputeModLogic{

    public SpellValueType VALUE_TYPE;
    public String REGISTRY_NAME;
    public List<Component> TOOLTIP;
    public Object VALUE;
    public SpellType spellType = SpellType.COMPUTE_MOD;
    public Component SUB_CATEGORY;

    public ValueLiteralSpell(SpellValueType valueType, Object value) {
        this(valueType, "internal_value", value, List.of());
    }

    public ValueLiteralSpell(SpellValueType valueType, String registryName, Object value, List<Component> tooltip) {
        VALUE_TYPE = valueType;
        VALUE = value;
        REGISTRY_NAME = registryName;
        TOOLTIP = tooltip;
        SUB_CATEGORY = Component.translatable("subcategory.programmable_magic.compute_constant");
    }

    public ValueLiteralSpell(SpellValueType valueType, String registryName, Object value, List<Component> tooltip, SpellType spellType) {
        VALUE_TYPE = valueType;
        VALUE = value;
        REGISTRY_NAME = registryName;
        TOOLTIP = tooltip;
        this.spellType = spellType;
        SUB_CATEGORY = spellType == SpellType.CONTROL_MOD
                ? Component.translatable("subcategory.programmable_magic.control_constant")
                : Component.translatable("subcategory.programmable_magic.compute_constant");
    }

    public ValueLiteralSpell(SpellValueType valueType, String registryName, Object value, List<Component> tooltip, SpellType spellType, Component subCategory) {
        VALUE_TYPE = valueType;
        VALUE = value;
        REGISTRY_NAME = registryName;
        TOOLTIP = tooltip;
        this.spellType = spellType;
        this.SUB_CATEGORY = subCategory;
    }

    @Override
    public SpellType getSpellType() {
        return spellType;
    }

    @Override
    public net.minecraft.network.chat.Component getSubCategory() {
        return SUB_CATEGORY;
    }

    @Override
    public String getRegistryName() {return REGISTRY_NAME;}

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful", true);
    }

    

    @Override
    public List<Component> getTooltip() {return TOOLTIP;}

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of();
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of();
    }
}
