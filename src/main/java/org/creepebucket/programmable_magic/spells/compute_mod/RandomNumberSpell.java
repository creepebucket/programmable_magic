package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;

public class RandomNumberSpell extends BaseComputeModLogic {
    @Override
    public String getRegistryName() { return "compute_random_number"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.dynamic_constant_number"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        double max = (Double) spellParams.get(0);
        double min = (Double) spellParams.get(1);
        return Map.of("successful", true, "type", NUMBER, "value", Math.random() * (max - min) + min);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.random_number.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.random_number.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(NUMBER, NUMBER)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }
}

