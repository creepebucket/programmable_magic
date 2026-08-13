package org.creepebucket.arcanism.spells.spells_control;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.creepebucket.arcanism.entities.SpellEntity;
import org.creepebucket.arcanism.spells.SpellValueType;
import org.creepebucket.arcanism.spells.api.ExecutionResult;
import org.creepebucket.arcanism.spells.api.SpellExceptions;
import org.creepebucket.arcanism.spells.api.SpellItemLogic;
import org.creepebucket.arcanism.spells.api.SpellSequence;
import org.creepebucket.arcanism.utils.Mana;

import java.util.List;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class FlowControlSpell {

    public static class LoopStartSpell extends SpellItemLogic.PairedLeftSpell implements SpellItemLogic.ControlMod {
        public SpellSequence originalSequence;

        public LoopStartSpell() {
            super();
            this.name = "loop_start";
            this.rightSpellType = LoopEndSpell.class;
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 循环: 获取配对后子序列并存储

            originalSequence = next == rightSpell ? new SpellSequence() : spellSequence.subSequence(next, rightSpell.prev);
            return ExecutionResult.SUCCESS(this);
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }

    public static class ForLoopSpell extends LoopStartSpell {

        // 次数计数
        public double count;

        public ForLoopSpell() {
            super();
            this.name = "for_loop";
            this.inputTypes = List.of(List.of(SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            count = (Double) paramsList.get(0);

            return super.run(caster, spellSequence, paramsList, spellEntity);
        }
    }

    public static class LoopEndSpell extends SpellItemLogic.PairedRightSpell implements SpellItemLogic.ControlMod {
        public LoopEndSpell() {
            super();
            this.name = "loop_end";
            this.leftSpellType = LoopStartSpell.class;
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 对for做特判
            if (leftSpell instanceof ForLoopSpell forLoopSpell) {
                forLoopSpell.count--;

                if (forLoopSpell.count == 0) return ExecutionResult.SUCCESS(this);
            }

            if (leftSpell.next == this) return new ExecutionResult(this, 1, false, null, null);
            // 还原 originalSequence
            var seq = ((LoopStartSpell) leftSpell).originalSequence;
            spellSequence.replaceSection(leftSpell.next, prev, seq.subSequence(seq.head, seq.tail));

            return new ExecutionResult(leftSpell.next, 1, false, null, null);
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }

    public static class BreakSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public BreakSpell() {
            super();
            this.name = "break";
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
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
            SpellExceptions.RUNTIME(Component.translatable("message.arcanism.error.break_not_found_end"), this).throwIt(caster);
            return ExecutionResult.ERRORED();
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }

    public static class ContinueSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public ContinueSpell() {
            super();
            this.name = "continue";
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 向右搜索到配对的 LoopEnd
            int count = 0;

            SpellItemLogic pointer = this;
            while (pointer != null) {
                pointer = pointer.next;
                if (pointer instanceof LoopEndSpell) {
                    if (count == 0) return ExecutionResult.SUCCESS(pointer.prev);
                    count--;
                } else if (pointer instanceof LoopStartSpell) {
                    count++;
                }
            }

            // 找不到则报错
            SpellExceptions.RUNTIME(Component.translatable("message.arcanism.error.continue_not_found_end"), this).throwIt(caster);
            return ExecutionResult.ERRORED();
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }

    public static class IfStartSpell extends SpellItemLogic.PairedLeftSpell implements SpellItemLogic.ControlMod {
        public IfStartSpell() {
            super();
            this.name = "if_start";
            this.rightSpellType = IfEndSpell.class;
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
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
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }

    public static class IfEndSpell extends SpellItemLogic.PairedRightSpell implements SpellItemLogic.ControlMod {
        public IfEndSpell() {
            super();
            this.name = "if_end";
            this.leftSpellType = IfStartSpell.class;
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
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
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }

    public static class StopSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public StopSpell() {
            super();
            this.name = "stop";
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
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
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }

    public static class RestartSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {
        public RestartSpell() {
            super();
            this.name = "restart";
            this.subCategory = "spell." + MODID + ".subcategory.flow_control";
            this.precedence = -99;
            this.bypassShunting = true;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {

            spellEntity.spellSequence = spellEntity.originalSpellSequence.subSequence(spellEntity.originalSpellSequence.head, spellEntity.originalSpellSequence.tail);

            return new ExecutionResult(spellEntity.spellSequence.head, 0, false, null, null);
        }

        @Override
        public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return true;
        }

        @Override
        public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new Mana();
        }
    }
}
