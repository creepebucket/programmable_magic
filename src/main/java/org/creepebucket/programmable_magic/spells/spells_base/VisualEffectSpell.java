package org.creepebucket.programmable_magic.spells.spells_base;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class VisualEffectSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {

    public VisualEffectSpell() {
        subCategory = "spell." + MODID + ".subcategory.visual";
        precedence = -99;
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }

    public static class DebugPrintSpell extends VisualEffectSpell {
        public DebugPrintSpell() {
            name = "debug_print";
            inputTypes = List.of(List.of(SpellValueType.ANY));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {

            // 生成调试背景基础信息
            Component debugText = Component.translatable("spell." + MODID + ".debug_print_head")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withHoverEvent(
                            new HoverEvent.ShowText(Component.translatable("spell." + MODID + ".debug_print_head_hover_time")
                                    .append(Component.literal(caster.level().getGameTime() + "\n")))
                    ));

            if (paramsList.get(0) instanceof Double) {
                // TODO
            }

            return ExecutionResult.SUCCESS(this);
        }
    }
}
