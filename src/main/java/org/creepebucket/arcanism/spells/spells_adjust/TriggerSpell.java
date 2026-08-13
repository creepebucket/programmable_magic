package org.creepebucket.arcanism.spells.spells_adjust;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.creepebucket.arcanism.entities.SpellEntity;
import org.creepebucket.arcanism.spells.SpellValueType;
import org.creepebucket.arcanism.spells.api.ExecutionResult;
import org.creepebucket.arcanism.spells.api.SpellItemLogic;
import org.creepebucket.arcanism.spells.api.SpellSequence;
import org.creepebucket.arcanism.utils.Mana;

import java.util.List;

import static org.creepebucket.arcanism.Arcanism.MODID;

public abstract class TriggerSpell extends SpellItemLogic implements SpellItemLogic.AdjustMod {
    public TriggerSpell() {
        subCategory = "spell." + MODID + ".subcategory.trigger";
        bypassShunting = true;
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

    public static class ConditionInvertSpell extends TriggerSpell {
        public ConditionInvertSpell() {
            super();
            name = "condition_invert";
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.SUCCESS(this);
        }
    }

    public static class TouchGroundSpell extends TriggerSpell {
        public TouchGroundSpell() {
            super();
            name = "touch_ground";
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            AABB aabb = new AABB(spellEntity.getX() - 0.1, spellEntity.getY() - 0.1, spellEntity.getZ() - 0.1, spellEntity.getX() + 0.1, spellEntity.getY() + 0.1, spellEntity.getZ() + 0.1)
                    .expandTowards(-spellEntity.getDeltaMovement().x, -spellEntity.getDeltaMovement().y, -spellEntity.getDeltaMovement().z);
            if (caster.level().getBlockStates(aabb).allMatch(state -> state.isAir())) {
                if (prev instanceof ConditionInvertSpell) return ExecutionResult.SUCCESS(this);
                else return ExecutionResult.FAILED(this);
            } else {
                if (prev instanceof ConditionInvertSpell) return ExecutionResult.FAILED(this);
                else return ExecutionResult.SUCCESS(this);
            }
        }
    }

    public static class TouchEntitySpell extends TriggerSpell {
        public TouchEntitySpell() {
            super();
            name = "touch_entity";
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            AABB aabb = new AABB(spellEntity.getX() - 0.1, spellEntity.getY() - 0.1, spellEntity.getZ() - 0.1, spellEntity.getX() + 0.1, spellEntity.getY() + 0.1, spellEntity.getZ() + 0.1)
                    .expandTowards(-spellEntity.getDeltaMovement().x, -spellEntity.getDeltaMovement().y, -spellEntity.getDeltaMovement().z);

            for (Entity entity : spellEntity.level().getEntities(spellEntity, aabb, e -> e != spellEntity)) {
                if (prev instanceof ConditionInvertSpell) return ExecutionResult.FAILED(this);
                else return ExecutionResult.SUCCESS(this);
            }

            if (prev instanceof ConditionInvertSpell) return ExecutionResult.SUCCESS(this);
            else return ExecutionResult.FAILED(this);
        }
    }

    public static class DelaySpell extends TriggerSpell {
        public DelaySpell() {
            super();
            name = "delay";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
        }

        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return new ExecutionResult(next, (int) Math.floor((Double) paramsList.get(0)), false, null, null);
        }
    }
}
