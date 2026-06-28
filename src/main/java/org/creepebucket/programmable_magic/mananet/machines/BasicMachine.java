package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import java.util.HashSet;
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

    public final List<IoEntry> IO_ENTRIES = new ArrayList<>();

    public BasicMachine(Properties p_49795_) {
        super(p_49795_);
    }

    public abstract VoxelShape hitbox();

    public void addItemInput(int facing_off, int y_off, int cw90_off) {
        IO_ENTRIES.add(new IoEntry(DummyBlock.ioOffset(Direction.NORTH, facing_off, y_off, cw90_off), DummyBlockEntity.IoType.ITEM_INPUT));
    }

    public void addItemOutput(int facing_off, int y_off, int cw90_off) {
        IO_ENTRIES.add(new IoEntry(DummyBlock.ioOffset(Direction.NORTH, facing_off, y_off, cw90_off), DummyBlockEntity.IoType.ITEM_OUTPUT));
    }

    public void addFluidInput(int facing_off, int y_off, int cw90_off) {
        IO_ENTRIES.add(new IoEntry(DummyBlock.ioOffset(Direction.NORTH, facing_off, y_off, cw90_off), DummyBlockEntity.IoType.FLUID_INPUT));
    }

    public void addFluidOutput(int facing_off, int y_off, int cw90_off) {
        IO_ENTRIES.add(new IoEntry(DummyBlock.ioOffset(Direction.NORTH, facing_off, y_off, cw90_off), DummyBlockEntity.IoType.FLUID_OUTPUT));
    }

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

        var io_offsets = new HashSet<BlockPos>();
        for (var entry : IO_ENTRIES) {
            io_offsets.add(entry.offset());
        }

        var dummy_block = MananetNodeBlocks.DUMMY_BLOCK.get();
        var io_dummy_block = MananetNodeBlocks.IO_DUMMY_BLOCK.get();
        for (var offset : DUMMY_OFFSETS) {
            var dummy_pos = pos.offset(offset);
            if (io_offsets.contains(offset)) {
                var io_type = IO_ENTRIES.stream().filter(e -> e.offset().equals(offset)).findFirst().get().ioType();
                level.setBlock(
                        dummy_pos,
                        io_dummy_block.defaultBlockState()
                                .setValue(DummyBlock.X_OFFSET, -offset.getX())
                                .setValue(DummyBlock.Y_OFFSET, -offset.getY())
                                .setValue(DummyBlock.Z_OFFSET, -offset.getZ()),
                        Block.UPDATE_ALL
                );
                var be = level.getBlockEntity(dummy_pos);
                if (be instanceof DummyBlockEntity dummy_be) {
                    dummy_be.ioType = io_type;
                    dummy_be.setChanged();
                }
            } else {
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
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level actual_level && !actual_level.isClientSide()) {
            for (var offset : DUMMY_OFFSETS) {
                var dummy_pos = pos.offset(offset);
                var dummy_state = actual_level.getBlockState(dummy_pos);
                if (!dummy_state.is(MananetNodeBlocks.DUMMY_BLOCK) && !dummy_state.is(MananetNodeBlocks.IO_DUMMY_BLOCK)) continue;
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

    public record IoEntry(BlockPos offset, DummyBlockEntity.IoType ioType) {}
}