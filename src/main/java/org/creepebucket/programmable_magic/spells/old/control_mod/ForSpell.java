package org.creepebucket.programmable_magic.spells.old.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.old.SpellData;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.old.SpellUtils;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.ModUtils.formatSpellError;
import static org.creepebucket.programmable_magic.spells.old.SpellUtils.setSpellError;
import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;

public class ForSpell extends LoopStartSpell {
    private LoopEndSpell pairedEnd;
    private int remaining;
    private boolean remainingInitialized = false;

    @Override
    public String getRegistryName() { return "for"; }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.flow_control"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 首次在 For 处捕获循环体模板与配对的 LoopEnd，以便复用 LoopEnd 的回跳逻辑
        if (!loopTemplateCaptured) {
            int depth = 0;
            SpellItemLogic p = this.getNextSpell();
            while (true) {
                if (p == null) {
                    int index = SpellUtils.displayIndexOf(spellSequence, this);
                    setSpellError(player, data, formatSpellError(
                            Component.translatable("message.programmable_magic.error.kind.syntax"),
                            Component.translatable("message.programmable_magic.error.detail.loop_not_pair", index)
                    ));
                    return Map.of("successful", false, "should_discard", true);
                } else if (p instanceof LoopStartSpell) {
                    depth++;
                } else if (p instanceof LoopEndSpell) {
                    if (depth == 0) {
                        this.loopBodyTemplate = spellSequence.subSequence(this, p);
                        this.loopTemplateCaptured = true;
                        this.pairedEnd = (LoopEndSpell) p;
                        break;
                    } else {
                        depth--;
                    }
                }
                p = p.getNextSpell();
            }
        }

        if (!remainingInitialized) {
            this.remaining = (int) Math.floor((Double) spellParams.get(0));
            this.remainingInitialized = true;
        }

        if (this.remaining <= 0) {
            SpellItemLogic next = this.pairedEnd.getNextSpell();
            if (next == null) return Map.of("successful", true, "should_discard", true);
            return Map.of("successful", true, "current_spell", next);
        }

        this.remaining--;
        return Map.of("successful", true);
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(
                Component.translatable("tooltip.programmable_magic.spell.for.desc1"),
                Component.translatable("tooltip.programmable_magic.spell.for.desc2")
        );
    }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(NUMBER)); }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(SpellValueType.MODIFIER)); }
}
