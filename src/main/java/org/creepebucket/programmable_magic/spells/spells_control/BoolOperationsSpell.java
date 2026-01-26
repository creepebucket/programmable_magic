package org.creepebucket.programmable_magic.spells.spells_control;

import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class BoolOperationsSpell extends SpellItemLogic implements SpellItemLogic.ControlMod {

    public BoolOperationsSpell() {
        outputTypes = List.of(List.of(SpellValueType.BOOLEAN));
        subCategory = "spell." + MODID + ".subcategory.bool_operations";
    }

    // 返回值是布尔值

    public static class GreaterThanSpell extends BoolOperationsSpell {
        public GreaterThanSpell() {
            name = "greater_than";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) > (Double) paramsList.get(1)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class LessThanSpell extends BoolOperationsSpell {
        public LessThanSpell() {
            name = "less_than";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) < (Double) paramsList.get(1)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class EqualToSpell extends BoolOperationsSpell {
        public EqualToSpell() {
            name = "equal_to";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) == (Double) paramsList.get(1)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class GreaterEqualToSpell extends BoolOperationsSpell {
        public GreaterEqualToSpell() {
            name = "greater_equal_to";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) >= (Double) paramsList.get(0)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class LessEqualToSpell extends BoolOperationsSpell {
        public LessEqualToSpell() {
            name = "less_equal_to";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) <= (Double) paramsList.get(1)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class NotEqualToSpell extends BoolOperationsSpell {
        public NotEqualToSpell() {
            name = "not_equal_to";
            inputTypes = List.of(List.of(SpellValueType.NUMBER, SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Double) paramsList.get(0) != (Double) paramsList.get(1)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class AndSpell extends BoolOperationsSpell {
        public AndSpell() {
            name = "and";
            inputTypes = List.of(List.of(SpellValueType.BOOLEAN, SpellValueType.BOOLEAN));
            precedence = -3;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Boolean) paramsList.get(0) && (Boolean) paramsList.get(1)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class OrSpell extends BoolOperationsSpell {
        public OrSpell() {
            name = "or";
            inputTypes = List.of(List.of(SpellValueType.BOOLEAN, SpellValueType.BOOLEAN));
            precedence = -2;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((Boolean) paramsList.get(0) || (Boolean) paramsList.get(1)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class NotSpell extends BoolOperationsSpell {
        public NotSpell() {
            name = "not";
            inputTypes = List.of(List.of(SpellValueType.BOOLEAN));
            precedence = -1;
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(!(Boolean) paramsList.get(0)), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class BlockIsAirSpell extends BoolOperationsSpell {
        public BlockIsAirSpell() {
            name = "block_is_air";
            inputTypes = List.of(List.of(SpellValueType.BLOCK));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of(((Block) paramsList.get(0)).defaultBlockState().isAir()), List.of(SpellValueType.BOOLEAN));
        }
    }

    public static class BlockHasGravitySpell extends BoolOperationsSpell {
        public BlockHasGravitySpell() {
            name = "block_has_gravity";
            inputTypes = List.of(List.of(SpellValueType.BLOCK));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            return ExecutionResult.RETURNED(this, List.of((paramsList.get(0) instanceof FallingBlock)), List.of(SpellValueType.BOOLEAN));
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
