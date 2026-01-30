package org.creepebucket.programmable_magic.spells.spells_compute;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class BlockOperationsSpell extends SpellItemLogic implements SpellItemLogic.ComputeMod {

    public BlockOperationsSpell() {
        subCategory = "spell." + MODID + ".subcategory.operations.block";
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
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
