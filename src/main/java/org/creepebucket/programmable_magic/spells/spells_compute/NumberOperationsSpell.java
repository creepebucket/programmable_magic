package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellExceptions;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class NumberOperationsSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    public NumberOperationsSpell() {
        subCategory = "spell." + MODID + ".subcategory.operations.number";
    }

    // 返回为数字的运算统一放在这里

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }

    public static class AdditionSpell extends NumberOperationsSpell {

        public AdditionSpell() {
            super();
            name = "addition";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 1;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) + (Double) paramsList.get(1)), List.of(SpellValueType.NUMBER));
        }
    }

    public static class SubtractionSpell extends NumberOperationsSpell {
        public SubtractionSpell() {
            super();
            name = "subtraction";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 1;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) - (Double) paramsList.get(1)), List.of(SpellValueType.NUMBER));
        }
    }

    public static class MultiplicationSpell extends NumberOperationsSpell {
        public MultiplicationSpell() {
            super();
            name = "multiplication";
            inputTypes = List.of(
                    List.of(SpellValueType.NUMBER, SpellValueType.NUMBER),
                    List.of(SpellValueType.NUMBER, SpellValueType.VECTOR3),
                    List.of(SpellValueType.VECTOR3, SpellValueType.NUMBER),
                    List.of(SpellValueType.VECTOR3, SpellValueType.VECTOR3));

            outputTypes = List.of(List.of(SpellValueType.NUMBER), List.of(SpellValueType.VECTOR3), List.of(SpellValueType.VECTOR3), List.of(SpellValueType.NUMBER));
            precedence = 2;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {

            // 数字*数字
            if (paramsList.get(0) instanceof Double && paramsList.get(1) instanceof Double) {
                return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) * (Double) paramsList.get(1)), List.of(SpellValueType.NUMBER));
            }
            // 数字*向量 将向量每个分量乘以数字
            else if (paramsList.get(0) instanceof Double && paramsList.get(1) instanceof Vec3) {
                return ExecutionResult.RETURNED(this, List.of(new Vec3(
                                ((Vec3) paramsList.get(1)).x * (Double) paramsList.get(0),
                                ((Vec3) paramsList.get(1)).y * (Double) paramsList.get(0),
                                ((Vec3) paramsList.get(1)).z * (Double) paramsList.get(0))),
                        List.of(SpellValueType.VECTOR3));
            } else if (paramsList.get(0) instanceof Vec3 && paramsList.get(1) instanceof Double) {
                return ExecutionResult.RETURNED(this, List.of(new Vec3(
                                ((Vec3) paramsList.get(0)).x * (Double) paramsList.get(1),
                                ((Vec3) paramsList.get(0)).y * (Double) paramsList.get(1),
                                ((Vec3) paramsList.get(0)).z * (Double) paramsList.get(1))),
                        List.of(SpellValueType.VECTOR3)
                );
            }
            // 向量*向量 返回点积
            else if (paramsList.get(0) instanceof Vec3 && paramsList.get(1) instanceof Vec3) {
                return ExecutionResult.RETURNED(this, List.of(((Vec3) paramsList.get(0)).dot((Vec3) paramsList.get(1))), List.of(SpellValueType.NUMBER));
            }
            // 剩下情况需要报错
            SpellExceptions.INVALID_INPUT(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }
    }

    public static class DivisionSpell extends NumberOperationsSpell {
        public DivisionSpell() {
            super();
            name = "division";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER),
                    List.of(SpellValueType.NUMBER, SpellValueType.VECTOR3),
                    List.of(SpellValueType.VECTOR3, SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER), List.of(SpellValueType.VECTOR3), List.of(SpellValueType.VECTOR3));
            precedence = 2;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {

            // 数字/数字
            if (paramsList.get(0) instanceof Double && paramsList.get(1) instanceof Double) {
                return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) / (Double) paramsList.get(1)), List.of(SpellValueType.NUMBER));
            }
            // 数字/向量 每个分量除以数字
            else if (paramsList.get(0) instanceof Double && paramsList.get(1) instanceof Vec3) {
                return ExecutionResult.RETURNED(this, List.of(new Vec3(
                                ((Vec3) paramsList.get(1)).x / (Double) paramsList.get(0),
                                ((Vec3) paramsList.get(1)).y / (Double) paramsList.get(0),
                                ((Vec3) paramsList.get(1)).z / (Double) paramsList.get(0))),
                        List.of(SpellValueType.VECTOR3));
            } else if (paramsList.get(0) instanceof Vec3 && paramsList.get(1) instanceof Double) {
                return ExecutionResult.RETURNED(this, List.of(new Vec3(
                                ((Vec3) paramsList.get(0)).x / (Double) paramsList.get(1),
                                ((Vec3) paramsList.get(0)).y / (Double) paramsList.get(1),
                                ((Vec3) paramsList.get(0)).z / (Double) paramsList.get(1))),
                        List.of(SpellValueType.VECTOR3)
                );
            }
            // 剩下情况需要报错
            SpellExceptions.INVALID_INPUT(caster, this).throwIt();
            return ExecutionResult.ERRORED();
        }
    }

    // 剩下的是一元操作

    public static class RemainderSpell extends NumberOperationsSpell {
        public RemainderSpell() {
            super();
            name = "remainder";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 2;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) % (Double) paramsList.get(1)), List.of(SpellValueType.NUMBER));
        }
    }

    public static class ExponentSpell extends NumberOperationsSpell {
        public ExponentSpell() {
            super();
            name = "exponent";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            rightConnectivity = true;
            precedence = 4;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.pow((Double) paramsList.get(0), (Double) paramsList.get(1))), List.of(SpellValueType.NUMBER));
        }
    }

    public static class SinSpell extends NumberOperationsSpell {
        public SinSpell() {
            super();
            name = "sin";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.sin((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    public static class CosSpell extends NumberOperationsSpell {
        public CosSpell() {
            super();
            name = "cos";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.cos((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    public static class TanSpell extends NumberOperationsSpell {
        public TanSpell() {
            super();
            name = "tan";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.tan((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    public static class AsinSpell extends NumberOperationsSpell {
        public AsinSpell() {
            super();
            name = "asin";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.asin((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    public static class AcosSpell extends NumberOperationsSpell {
        public AcosSpell() {
            super();
            name = "acos";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.acos((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    public static class AtanSpell extends NumberOperationsSpell {
        public AtanSpell() {
            super();
            name = "atan";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.atan((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    // 其他复杂运算

    public static class CeilSpell extends NumberOperationsSpell {
        public CeilSpell() {
            super();
            name = "ceil";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.ceil((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    // 向量运算

    public static class FloorSpell extends NumberOperationsSpell {
        public FloorSpell() {
            super();
            name = "floor";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(Math.floor((Double) paramsList.get(0))), List.of(SpellValueType.NUMBER));
        }
    }

    public static class RandomNumberSpell extends NumberOperationsSpell {
        public RandomNumberSpell() {
            super();
            name = "random";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER)); // [上界 .. 下界]
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            double min = (Double) paramsList.get(0);
            double max = (Double) paramsList.get(1);
            return ExecutionResult.RETURNED(this, List.of(Math.random() * (max - min) + min), List.of(SpellValueType.NUMBER));
        }
    }

    public static class VectorLengthSpell extends NumberOperationsSpell {
        public VectorLengthSpell() {
            super();
            name = "vector_length";
            inputTypes = List.of(List.of(SpellValueType.VECTOR3));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((Vec3) paramsList.get(0)).length()), List.of(SpellValueType.NUMBER));
        }
    }

    public static class VectorXSpell extends NumberOperationsSpell {
        public VectorXSpell() {
            super();
            name = "vector_x";
            inputTypes = List.of(List.of(SpellValueType.VECTOR3));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((Vec3) paramsList.get(0)).x), List.of(SpellValueType.NUMBER));
        }
    }

    // 生物信息

    public static class VectorYSpell extends NumberOperationsSpell {
        public VectorYSpell() {
            super();
            name = "vector_y";
            inputTypes = List.of(List.of(SpellValueType.VECTOR3));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((Vec3) paramsList.get(0)).y), List.of(SpellValueType.NUMBER));
        }
    }

    public static class VectorZSpell extends NumberOperationsSpell {
        public VectorZSpell() {
            super();
            name = "vector_z";
            inputTypes = List.of(List.of(SpellValueType.VECTOR3));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((Vec3) paramsList.get(0)).z), List.of(SpellValueType.NUMBER));
        }
    }

    public static class EntityHealthSpell extends NumberOperationsSpell {
        public EntityHealthSpell() {
            super();
            name = "entity_health";
            inputTypes = List.of(List.of(SpellValueType.ENTITY));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((LivingEntity) paramsList.get(0)).getHealth()), List.of(SpellValueType.NUMBER));
        }
    }

    public static class EntityMaxHealthSpell extends NumberOperationsSpell {
        public EntityMaxHealthSpell() {
            super();
            name = "entity_max_health";
            inputTypes = List.of(List.of(SpellValueType.ENTITY));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((LivingEntity) paramsList.get(0)).getMaxHealth()), List.of(SpellValueType.NUMBER));
        }
    }

    public static class EntityArmorSpell extends NumberOperationsSpell {
        public EntityArmorSpell() {
            super();
            name = "entity_armor";
            inputTypes = List.of(List.of(SpellValueType.ENTITY));
            outputTypes = List.of(List.of(SpellValueType.NUMBER));
            precedence = 3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((LivingEntity) paramsList.get(0)).getArmorValue()), List.of(SpellValueType.NUMBER));
        }
    }
}
