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

public class LoopStartSpell extends BaseControlModLogic{
    // 循环体模板：首次识别配对后缓存 (start, end) 之间的原始序列克隆
    /* package */ org.creepebucket.programmable_magic.spells.SpellSequence loopBodyTemplate;
    /* package */ boolean loopTemplateCaptured = false;
    @Override
    public String getRegistryName() {
        return "loop_start";
    }

    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.loop_control"); }

    @Override
    public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
        // 首次在 LoopStart 处捕获循环体模板（不含边界），以便每次 LoopEnd 重置
        if (!loopTemplateCaptured) {
            int depth = 0;
            SpellItemLogic p = this.getNextSpell();
            while (true) {
                if (p == null) {
                    sendErrorMessageToPlayer(Component.translatable("programmable_magic.error.loop_not_pair"), player);
                    break;
                } else if (p instanceof LoopStartSpell) {
                    depth++;
                } else if (p instanceof LoopEndSpell) {
                    if (depth == 0) {
                        // 在同层找到配对的 LoopEnd
                        this.loopBodyTemplate = spellSequence.subSequence(this, p);
                        this.loopTemplateCaptured = true;
                        break;
                    } else {
                        depth--;
                    }
                }
                p = p.getNextSpell();
            }
        }
        return Map.of("successful",  true);
    }

    @Override
    public List<Component> getTooltip() { return List.of(
            Component.translatable("tooltip.programmable_magic.spell.loop_start.desc1"),
            Component.translatable("tooltip.programmable_magic.spell.loop_start.desc2")
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
