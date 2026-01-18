package org.creepebucket.programmable_magic.spells.old.adjust_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

public class DelaySpell extends BaseAdjustModLogic {
    @Override
    public String getRegistryName() {
        return "delay";
    }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.trigger"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        return Map.of("successful",  true, "delay", ((Double) spellParams.get(0)).intValue());
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
