package org.creepebucket.arcanism.spells.spells_compute;

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

public abstract class DynamicConstantSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    // 数字

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    // 向量

    @Override
    public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new Mana();
    }

    public static class TimestampSpell extends DynamicConstantSpell {
        public TimestampSpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.number";
            name = "timestamp";
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((double) caster.level().getGameTime()), List.of(SpellValueType.NUMBER));
        }
    }

    public static class CameraDirectionSpell extends DynamicConstantSpell {
        public CameraDirectionSpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.vector";
            name = "camera_direction";
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(caster.getLookAngle().normalize()), List.of(SpellValueType.VECTOR3));
        }
    }

    // 实体

    public static class CasterPositionSpell extends DynamicConstantSpell {
        public CasterPositionSpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.vector";
            name = "caster_position";
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(caster.position()), List.of(SpellValueType.VECTOR3));
        }
    }

    public static class SpellPositionSpell extends DynamicConstantSpell {
        public SpellPositionSpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.vector";
            name = "spell_position";
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(spellEntity.position()), List.of(SpellValueType.VECTOR3));
        }
    }

    public static class CasterEntitySpell extends DynamicConstantSpell {
        public CasterEntitySpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.entity";
            name = "caster_entity";
            outputTypes = List.of(List.of(SpellValueType.ENTITY));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(caster), List.of(SpellValueType.ENTITY));
        }
    }

    public static class SpellEntitySpell extends DynamicConstantSpell {
        public SpellEntitySpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.entity";
            name = "spell_entity";
            outputTypes = List.of(List.of(SpellValueType.ENTITY));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(spellEntity), List.of(SpellValueType.ENTITY));
        }
    }

    public static class NearestEntitySpell extends DynamicConstantSpell {
        public NearestEntitySpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.entity";
            name = "nearest_entity";
            outputTypes = List.of(List.of(SpellValueType.ENTITY));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 半径
            double radius = 4;

            AABB aabb = new AABB(
                    spellEntity.getX() - radius,
                    spellEntity.getY() - radius,
                    spellEntity.getZ() - radius,
                    spellEntity.getX() + radius,
                    spellEntity.getY() + radius,
                    spellEntity.getZ() + radius
            );

            Entity nearest = spellEntity;
            double nearestDist2 = Double.MAX_VALUE;
            double rangeDist2 = radius * radius;

            for (Entity entity : spellEntity.level().getEntities(spellEntity, aabb, e -> e != spellEntity)) {
                double dist2 = spellEntity.distanceToSqr(entity);

                // 检查
                if (dist2 <= rangeDist2 && dist2 < nearestDist2) {
                    nearest = entity;
                    nearestDist2 = dist2;
                }
            }

            return ExecutionResult.RETURNED(this, List.of(nearest), List.of(SpellValueType.ENTITY));
        }
    }
}
