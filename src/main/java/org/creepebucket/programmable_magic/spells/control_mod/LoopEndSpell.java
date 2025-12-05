package org.creepebucket.programmable_magic.spells.control_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.ModUtils.sendErrorMessageToPlayer;

public class LoopEndSpell extends BaseControlModLogic{
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("ProgrammableMagic:LoopEndSpell");

    @Override
    public String getRegistryName() {
        return "loop_end";
    }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 循环到配对的LoopStartSpell
        int count = 0;
        SpellItemLogic pointer = this.getPrevSpell();

        while (true) {
            if (pointer == null) {
                // 未找到配对
                sendErrorMessageToPlayer(Component.translatable("programmable_magic.error.loop_not_pair"), player);
                LOGGER.error("[ProgrammableMagic:SpellEntity] 未找到配对");
                return Map.of("successful", true);
            } else if (pointer instanceof LoopStartSpell) {
                if (count == 0) {
                    // 找到配对
                    LoopStartSpell start = (LoopStartSpell) pointer;

                    // 当前循环体（不含边界）
                    SpellItemLogic bodyStart = start.getNextSpell();
                    SpellItemLogic bodyEnd = this.getPrevSpell();

                    // 每次回跳前，用模板重置循环体（深拷贝一次，避免污染模板）
                    if (start.loopTemplateCaptured && bodyStart != null && bodyEnd != null) {
                        SpellSequence fresh = new SpellSequence();
                        for (SpellItemLogic it = start.loopBodyTemplate.getFirstSpell(); it != null; it = it.getNextSpell()) {
                            fresh.addLast(it.clone());
                        }
                        spellSequence.replaceSection(bodyStart, bodyEnd, fresh);
                    }

                    return Map.of("successful", true, "current_spell",  pointer, "delay", 1);
                } else {
                    count--;
                }
            } else if (pointer instanceof LoopEndSpell) {
                count++;
            }

            pointer = pointer.getPrevSpell();
        }
    }

    @Override
    public void calculateBaseMana(SpellData data) {

    }

    @Override
    public List<Component> getTooltip() { return List.of(
            Component.translatable("tooltip.programmable_magic.spell.loop_end.desc1"),
            Component.translatable("tooltip.programmable_magic.spell.loop_end.desc2")
    ); }

    @Override
    public List<List<SpellValueType>> getNeededParamsType() {
        return List.of(List.of(SpellValueType.EMPTY));
    }

    @Override
    public List<List<SpellValueType>> getReturnParamsType() {
        return List.of(List.of(SpellValueType.MODIFIER));
    }
}
