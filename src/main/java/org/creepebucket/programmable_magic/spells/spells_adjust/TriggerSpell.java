package org.creepebucket.programmable_magic.spells.spells_adjust;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class TriggerSpell extends SpellItemLogic implements SpellItemLogic.AdjustMod {
    public TriggerSpell() {
        subCategoryName = Component.translatable("spell." + MODID + ".subcategory.trigger");
        precedence = -99;
    }

    public static class ConditionInvertSpell extends TriggerSpell {
        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.SUCCESS(this);
        }
    }

    public static class TouchGroundSpell extends TriggerSpell {
        public TouchGroundSpell() {
            name = "touch_ground";
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 获取当前方块
            Block block = caster.level().getBlockState(caster.blockPosition()).getBlock();
            if (block.isEmpty(block.defaultBlockState())) {
                if (prev instanceof ConditionInvertSpell) return ExecutionResult.SUCCESS(this); else return ExecutionResult.FAILED(this);
            } else {
                if (prev instanceof ConditionInvertSpell) return ExecutionResult.FAILED(this); else return ExecutionResult.SUCCESS(this);
            }
        }
    }

    public static class TouchEntitySpell extends TriggerSpell {
        public TouchEntitySpell() {
            name = "touch_entity";
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            AABB aabb = new AABB(
                    spellEntity.getX() - 0.1,
                    spellEntity.getY() - 0.1,
                    spellEntity.getZ() - 0.1,
                    spellEntity.getX() + 0.1,
                    spellEntity.getY() + 0.1,
                    spellEntity.getZ() + 0.1
            );

            for (Entity entity : spellEntity.level().getEntities(spellEntity, aabb, e -> e != spellEntity)) {
                if (prev instanceof ConditionInvertSpell) return ExecutionResult.FAILED(this); else return ExecutionResult.SUCCESS(this);
            }

            if (prev instanceof ConditionInvertSpell) return ExecutionResult.SUCCESS(this); else return ExecutionResult.FAILED(this);
        }
    }

    public static class DelaySpell extends TriggerSpell {
        public DelaySpell() {
            name = "delay";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
        }

        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ExecutionResult(next, (int) Math.floor((Double) paramsList.get(0)), false, null, null);
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
