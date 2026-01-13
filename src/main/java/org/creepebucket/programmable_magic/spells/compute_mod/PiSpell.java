package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.network.chat.Component;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;

import static org.creepebucket.programmable_magic.spells.SpellValueType.EMPTY;
import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;

public class PiSpell extends ValueLiteralSpell {
    public PiSpell() {
        super(NUMBER, "compute_pi", Math.PI, List.of(
                Component.translatable("tooltip.programmable_magic.spell.pi.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.pi.desc2")
        ));
        SUB_CATEGORY = Component.translatable("subcategory.programmable_magic.number_digit");
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(EMPTY)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }
}
