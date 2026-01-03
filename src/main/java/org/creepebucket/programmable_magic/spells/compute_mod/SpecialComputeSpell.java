package org.creepebucket.programmable_magic.spells.compute_mod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.spells.SpellData;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellSequence;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.spells.SpellValueType.BLOCK;
import static org.creepebucket.programmable_magic.spells.SpellValueType.NUMBER;
import static org.creepebucket.programmable_magic.spells.SpellValueType.VECTOR3;

public abstract class SpecialComputeSpell extends BaseComputeModLogic {
    @Override
    public Component getSubCategory() { return Component.translatable("subcategory.programmable_magic.special_compute"); }

    public static class ViewBlockPosSpell extends SpecialComputeSpell {
        @Override
        public String getRegistryName() { return "compute_view_block_pos"; }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(List.of(VECTOR3, VECTOR3));
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Vec3 start = (Vec3) spellParams.get(0);
            Vec3 dir = ((Vec3) spellParams.get(1)).normalize();
            Vec3 end = start.add(dir.scale(64.0));

            HitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            BlockPos pos = (hit instanceof BlockHitResult bhr) ? bhr.getBlockPos() : BlockPos.containing(end);
            Vec3 out = new Vec3(pos.getX(), pos.getY(), pos.getZ());
            return Map.of("successful", true, "type", VECTOR3, "value", out);
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.view_block_pos.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.view_block_pos.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(VECTOR3)); }
    }

    public static class BlockAtPosSpell extends SpecialComputeSpell {
        @Override
        public String getRegistryName() { return "compute_block_at_pos"; }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() {
            return List.of(List.of(VECTOR3));
        }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Vec3 pos = (Vec3) spellParams.get(0);
            BlockPos blockPos = BlockPos.containing(pos);
            return Map.of("successful", true, "type", BLOCK, "value", player.level().getBlockState(blockPos));
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.block_at_pos.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.block_at_pos.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(BLOCK)); }
    }

    public static class VecLengthSpell extends SpecialComputeSpell {
        @Override
        public String getRegistryName() { return "compute_vec_length"; }

        @Override
        public List<List<SpellValueType>> getNeededParamsType() { return List.of(List.of(VECTOR3)); }

        @Override
        public Map<String, Object> run(Player player, SpellData data, SpellSequence spellSequence, List<SpellItemLogic> modifiers, List<Object> spellParams) {
            Vec3 v = (Vec3) spellParams.get(0);
            return Map.of("successful", true, "type", NUMBER, "value", v.length());
        }

        @Override
        public List<Component> getTooltip() {
            return List.of(
                    Component.translatable("tooltip.programmable_magic.spell.vec_length.desc1"),
                    Component.translatable("tooltip.programmable_magic.spell.vec_length.desc2")
            );
        }

        @Override
        public List<List<SpellValueType>> getReturnParamsType() { return List.of(List.of(NUMBER)); }
    }
}

