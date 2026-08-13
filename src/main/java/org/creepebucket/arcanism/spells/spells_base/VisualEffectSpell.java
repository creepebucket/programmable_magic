package org.creepebucket.arcanism.spells.spells_base;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.arcanism.entities.SpellEntity;
import org.creepebucket.arcanism.spells.SpellValueType;
import org.creepebucket.arcanism.spells.api.ExecutionResult;
import org.creepebucket.arcanism.spells.api.SpellItemLogic;
import org.creepebucket.arcanism.spells.api.SpellSequence;
import org.creepebucket.arcanism.utils.Mana;

import java.util.List;

import static org.creepebucket.arcanism.Arcanism.MODID;

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
    public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new Mana();
    }

    public static class DebugPrintSpell extends VisualEffectSpell {
        public DebugPrintSpell() {
            name = "debug_print";
            inputTypes = List.of(List.of(SpellValueType.ANY));
            bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {

            // 生成调试背景基础信息
            MutableComponent debugText = Component.translatable("spell." + MODID + ".debug_print_head")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withHoverEvent(
                            new HoverEvent.ShowText(Component.translatable("spell." + MODID + ".debug_print_head_hover_time")
                                    .append(Component.literal(caster.level().getGameTime() + "\n" + spellEntity.getStringUUID())))
                    ));

            var p = paramsList.get(0);

            debugText.append(Component.literal(p + ":").withColor(-1)).append(SpellValueType.fromValue(p).typed());

            caster.sendSystemMessage(debugText);

            return ExecutionResult.SUCCESS(this);
        }
    }
}
