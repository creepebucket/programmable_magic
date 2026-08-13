package org.creepebucket.arcanism.spells.spells_compute;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.arcanism.entities.SpellEntity;
import org.creepebucket.arcanism.spells.SpellValueType;
import org.creepebucket.arcanism.spells.api.ExecutionResult;
import org.creepebucket.arcanism.spells.api.SpellItemLogic;
import org.creepebucket.arcanism.spells.api.SpellSequence;
import org.creepebucket.arcanism.utils.Mana;

import java.util.List;

import static org.creepebucket.arcanism.Arcanism.MODID;

public abstract class Vector3OperationsSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    public Vector3OperationsSpell() {
        subCategory = "spell." + MODID + ".subcategory.operations.vector";
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new Mana();
    }

    public static class BuildVectorSpell extends Vector3OperationsSpell {
        public BuildVectorSpell() {
            name = "build_vector";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER, SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(
                    new Vec3((double) paramsList.get(0), (double) paramsList.get(1), (double) paramsList.get(2))), List.of(SpellValueType.VECTOR3));
        }
    }

    public static class EntityPositionSpell extends Vector3OperationsSpell {
        public EntityPositionSpell() {
            name = "entity_position";
            inputTypes = List.of(List.of(SpellValueType.ENTITY));
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((Entity) paramsList.get(0)).position()), List.of(SpellValueType.VECTOR3));
        }
    }

    public static class EntityVelocitySpell extends Vector3OperationsSpell {
        public EntityVelocitySpell() {
            name = "entity_velocity";
            inputTypes = List.of(List.of(SpellValueType.ENTITY));
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((LivingEntity) paramsList.get(0)).getDeltaMovement()), List.of(SpellValueType.VECTOR3));
        }
    }
}
