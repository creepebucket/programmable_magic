package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class DelaySpell extends BaseControlModLogic{
    @Override
    public String getRegistryName() {
        return "delay";
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful",  true, "delay", ((Double) spellParams.get(0)).intValue());
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.delay.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.delay.desc2"),
                Component.translatable("tooltip.programmable_magic.spell.delay.desc3")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.NUMBER));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.MODIFIER));
    }
}
