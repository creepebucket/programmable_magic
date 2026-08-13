package org.creepebucket.arcanism.spells.spells_compute;

import net.minecraft.core.BlockPos;
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

public abstract class BlockOperationsSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    public BlockOperationsSpell() {
        subCategory = "spell." + MODID + ".subcategory.operations.block";
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new Mana();
    }

    public static class BlockPositionSpell extends BlockOperationsSpell {
        public BlockPositionSpell() {
            name = "block_position";
            inputTypes = List.of(List.of(SpellValueType.VECTOR3));
            outputTypes = List.of(List.of(SpellValueType.BLOCK));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            // 将方块坐标和对应方块进行转换

            // Vec3 -> pos
            Vec3 vec = (Vec3) paramsList.get(0);
            BlockPos pos = new BlockPos((int) Math.floor(vec.x), (int) Math.floor(vec.y), (int) Math.floor(vec.z));

            return ExecutionResult.RETURNED(this, List.of(caster.level().getBlockState(pos).getBlock()), List.of(SpellValueType.BLOCK));
        }
    }
}
