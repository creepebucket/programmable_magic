package org.creepebucket.programmable_magic.spells.spells_base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.entities.SpellEntity;
import org.creepebucket.programmable_magic.spells.SpellValueType;
import org.creepebucket.programmable_magic.spells.api.ExecutionResult;
import org.creepebucket.programmable_magic.spells.api.SpellExceptions;
import org.creepebucket.programmable_magic.spells.api.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.api.SpellSequence;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class WorldInterationSpell extends SpellItemLogic implements SpellItemLogic.BaseSpell {

    public WorldInterationSpell() {
        subCategory = "spell." + MODID + ".subcategory.block";
        precedence = -99;
        bypassShunting = true;
    }

    @Override
    public boolean canRun(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return true;
    }

    @Override
    public ModUtils.Mana getManaCost(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
        return new ModUtils.Mana();
    }

    public static class BreakBlockSpell extends WorldInterationSpell {
        public BreakBlockSpell() {
            name = "break_block";
            outputTypes = List.of(List.of(SpellValueType.ITEM));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            BlockPos pos = spellEntity.blockPosition();
            BlockState state = spellEntity.level().getBlockState(pos);
            spellEntity.level().destroyBlock(pos, false, caster);
            return ExecutionResult.RETURNED(this, List.of(new ItemStack(state.getBlock().asItem())), List.of(SpellValueType.ITEM));
        }
    }

    public static class PlaceBlockSpell extends WorldInterationSpell {
        public PlaceBlockSpell() {
            name = "place_block";
            inputTypes = List.of(List.of(SpellValueType.ITEM));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            ItemStack stack = (ItemStack) paramsList.get(0);
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                SpellExceptions.INVALID_INPUT(this).throwIt(caster);
                return ExecutionResult.ERRORED();
            }

            BlockPos pos = BlockPos.containing(spellEntity.getX(), spellEntity.getY(), spellEntity.getZ());
            spellEntity.level().setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());
            return ExecutionResult.SUCCESS(this);
        }
    }

    public static class ExplosionSpell extends WorldInterationSpell {
        public ExplosionSpell() {
            name = "explosion";
            inputTypes = List.of(List.of(SpellValueType.NUMBER));
        }

        @Override
        public ExecutionResult run(Player caster, SpellSequence spellSequence, List<Object> paramsList, SpellEntity spellEntity) {
            float radius = (float) Math.cbrt((Double) paramsList.get(0));
            spellEntity.level().explode(spellEntity, spellEntity.getX(), spellEntity.getY(), spellEntity.getZ(), radius, false, Level.ExplosionInteraction.BLOCK);
            return ExecutionResult.SUCCESS(this);
        }
    }
}
