package org.creepebucket.programmable_magic.mananet.mechines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.registries.MananetNodeBlocks;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicMachine extends Block implements EntityBlock {

    public final VoxelShape HITBOX = hitbox();
    public final List<BlockPos> DUMMY_OFFSETS;

    {
        var offsets = new ArrayList<BlockPos>();
        for (var offset : BlockPos.betweenClosed(-4, -4, -4, 4, 4, 4)) {
            if (offset.getX() == 0 && offset.getY() == 0 && offset.getZ() == 0) continue;
            if (Shapes.joinIsNotEmpty(HITBOX, Shapes.block().move(offset), BooleanOp.AND)) {
                offsets.add(offset.immutable());
            }
        }
        DUMMY_OFFSETS = List.copyOf(offsets);
    }

    public BasicMachine(Properties p_49795_) {
        super(p_49795_);
    }

    public abstract VoxelShape hitbox();

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        for (var offset : DUMMY_OFFSETS) {
            if (!level.getBlockState(pos.offset(offset)).canBeReplaced()) return null;
        }
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;

        var dummy_block = MananetNodeBlocks.DUMMY_BLOCK.get();
        for (var offset : DUMMY_OFFSETS) {
            var dummy_pos = pos.offset(offset);
            level.setBlock(
                    dummy_pos,
                    dummy_block.defaultBlockState()
                            .setValue(DummyBlock.X_OFFSET, -offset.getX())
                            .setValue(DummyBlock.Y_OFFSET, -offset.getY())
                            .setValue(DummyBlock.Z_OFFSET, -offset.getZ()),
                    Block.UPDATE_ALL
            );
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level actual_level && !actual_level.isClientSide()) {
            for (var offset : DUMMY_OFFSETS) {
                var dummy_pos = pos.offset(offset);
                var dummy_state = actual_level.getBlockState(dummy_pos);
                if (!dummy_state.is(MananetNodeBlocks.DUMMY_BLOCK)) continue;
                if (!DummyBlock.get_main_pos(dummy_pos, dummy_state).equals(pos)) continue;
                actual_level.setBlock(
                        dummy_pos,
                        actual_level.getFluidState(dummy_pos).createLegacyBlock(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS
                );
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HITBOX;
    }
}