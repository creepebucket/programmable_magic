package org.creepebucket.programmable_magic.spells.spells_control;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellExceptions;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class FlowControlSpell {

    public static class LoopStartSpell extends SpellItemLogic.PairedLeftSpell implements SpellItemLogic.ControlMod {
        public SpellSequence originalSequence;

        public LoopStartSpell() {
            super();
            this.name = "loop_start";
            this.rightSpellType = LoopEndSpell.class;
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 循环: 获取配对后子序列并存储

            originalSequence = spellSequence.subSequence(next, rightSpell.prev);
            return ExecutionResult.SUCCESS(this);
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return false;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return null;
        }
    }

    public static class ForLoopSpell extends LoopStartSpell {

        // 次数计数
        public double count;
        public boolean firstRun;

        public ForLoopSpell() {
            super();
            this.name = "for_loop";
            this.inputTypes = List.of(List.of(SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            if (firstRun) {
                // 设置循环次数并返回
                count = (Double) paramsList.get(0);
                return ExecutionResult.SUCCESS(this);
            }

            count--;

            if (count > 0) {
                // 循环未结束则返回
                return ExecutionResult.SUCCESS(this);
            }

            return new ExecutionResult(rightSpell.next, 0, false, null, null);
        }
    }

    public static class LoopEndSpell extends SpellItemLogic.PairedRightSpell implements SpellItemLogic.ControlMod {
        public LoopEndSpell() {
            super();
            this.name = "loop_end";
            this.leftSpellType = LoopStartSpell.class;
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 还原 originalSequence
            spellSequence.replaceSection(leftSpell.next, prev, ((LoopStartSpell) leftSpell).originalSequence);

            return new ExecutionResult(leftSpell.next, 1, false, null, null);
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ModUtils.Mana();
        }
    }

    public static class BreakSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public BreakSpell() {
            super();
            this.name = "break";
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 向右搜索到最近的 IfEnd/LoopEnd

            SpellItemLogic pointer = this;
            while (pointer != null) {
                if (pointer instanceof IfEndSpell || pointer instanceof LoopEndSpell) {
                    return new ExecutionResult(pointer.next, 0, false, null, null);
                }
                pointer = pointer.next;
            }

            // 找不到则报错
            SpellExceptions.RUNTIME(Component.translatable("message.programmable_magic.error.break_not_found_end"), caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ModUtils.Mana();
        }
    }

    public static class ContinueSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public ContinueSpell() {
            super();
            this.name = "continue";
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 向左搜索到最近的 IfStart/LoopStart

            SpellItemLogic pointer = this;
            while (pointer != null) {
                if (pointer instanceof IfStartSpell || pointer instanceof LoopStartSpell) {
                    return new ExecutionResult(pointer.next, 0, false, null, null);
                }
                pointer = pointer.prev;
            }

            // 找不到则报错
            SpellExceptions.RUNTIME(Component.translatable("message.programmable_magic.error.continue_not_found_start"), caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ModUtils.Mana();
        }
    }

    public static class IfStartSpell extends SpellItemLogic.PairedLeftSpell implements SpellItemLogic.ControlMod {
        public IfStartSpell() {
            super();
            this.name = "if_start";
            this.rightSpellType = IfEndSpell.class;
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
            this.inputTypes = List.of(List.of(SpellValueType.BOOLEAN));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 如果bool条件不成立直接跳到pairedRight

            if (!(boolean) paramsList.get(0)) {
                return new ExecutionResult(rightSpell.next, 0, false, null, null);
            } else {
                return ExecutionResult.SUCCESS(this);
            }
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ModUtils.Mana();
        }
    }

    public static class IfEndSpell extends SpellItemLogic.PairedRightSpell implements SpellItemLogic.ControlMod {
        public IfEndSpell() {
            super();
            this.name = "if_end";
            this.leftSpellType = IfStartSpell.class;
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.SUCCESS(this);
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ModUtils.Mana();
        }
    }

    public static class StopSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public StopSpell() {
            super();
            this.name = "stop";
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.COMPLETED();
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ModUtils.Mana();
        }
    }

    public static class RestartSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public RestartSpell() {
            super();
            this.name = "restart";
            this.subCategoryName = Component.translatable("spell." + MODID + ".subcategory.flow_control");
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ExecutionResult(spellSequence.head, 0, false, null, null);
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ModUtils.Mana();
        }
    }
}
