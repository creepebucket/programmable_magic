package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class DynamicConstantSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    // 数字

    public static class TimestampSpell extends DynamicConstantSpell {
        public TimestampSpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.number";
            name = "timestamp";
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(caster.level().getGameTime()), List.of(SpellValueType.NUMBER));
        }
    }

    // 向量

    public static class CameraDirectionSpell extends DynamicConstantSpell {
        public CameraDirectionSpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.vector3";
            name = "camera_direction";
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(caster.getLookAngle().normalize()), List.of(SpellValueType.VECTOR3));
        }
    }

    public static class CasterPositionSpell extends DynamicConstantSpell {
        public CasterPositionSpell() {
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.vector3";
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
            subCategory = "spell." + MODID + ".subcategory.dynamic_constant.vector3";
            name = "spell_position";
            outputTypes = List.of(List.of(SpellValueType.VECTOR3));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(spellEntity.position()), List.of(SpellValueType.VECTOR3));
        }
    }

    // 实体

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

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }
}
