package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.creepebucket.programmable_magic.registries.MananetNodeBlocks;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicMachine extends Block implements EntityBlock {

    public final VoxelShape HITBOX = hitbox();
    public List<BlockPos> DUMMY_OFFSETS, IO_OFFSETS = new ArrayList<>();
    public List<DeferredBlock<?>> IO_TYPES = new ArrayList<>();
    public List<Integer> IO_SIZES = new ArrayList<>();
    public List<Integer> IO_CAPACITIES = new ArrayList<>();

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
        var dir = context.getHorizontalDirection().getOpposite();
        for (var offset : DUMMY_OFFSETS) {
            var rotated = rotateOffset(offset, dir);
            if (!level.getBlockState(pos.offset(rotated)).canBeReplaced()) return null;
        }
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;

        // 放置dummy
        var facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        var dummy_block = MananetNodeBlocks.DUMMY_BLOCK.get();
        for (var offset : DUMMY_OFFSETS) {
            var rotated = rotateOffset(offset, facing);
            var dummy_pos = pos.offset(rotated);
            level.setBlock(
                    dummy_pos,
                    dummy_block.defaultBlockState()
                            .setValue(DummyBlock.X_OFFSET, -rotated.getX())
                            .setValue(DummyBlock.Y_OFFSET, -rotated.getY())
                            .setValue(DummyBlock.Z_OFFSET, -rotated.getZ()),
                    Block.UPDATE_ALL
            );
        }

        // 放置io
        for (int i = 0; i < IO_OFFSETS.size(); i++) {
            var offset = IO_OFFSETS.get(i);
            var block = IO_TYPES.get(i);
            var absoluteOffset = DummyBlock.transformOffset(facing, offset.getX(), offset.getY(), offset.getZ());
            var dummyPos = pos.offset(absoluteOffset);

            level.setBlock(
                    dummyPos,
                    block.get().defaultBlockState()
                            .setValue(DummyBlock.X_OFFSET, -absoluteOffset.getX())
                            .setValue(DummyBlock.Y_OFFSET, -absoluteOffset.getY())
                            .setValue(DummyBlock.Z_OFFSET, -absoluteOffset.getZ()),
                    Block.UPDATE_ALL
            );

            int size = IO_SIZES.get(i);
            int capacity = IO_CAPACITIES.get(i);
            var be = level.getBlockEntity(dummyPos);
            if (be instanceof DummyBlockEntities.ItemInput ii) {
                ii.container = new SimpleContainer(size);
                ii.wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(ii.container), true, false);
            }
            if (be instanceof DummyBlockEntities.ItemOutput io) {
                io.container = new SimpleContainer(size);
                io.wrapper = new FlowControlHandler<>(VanillaContainerWrapper.of(io.container), false, true);
            }
            if (be instanceof DummyBlockEntities.FluidInput fi) {
                fi.fluidHandler = new FluidStacksResourceHandler(size, capacity);
                fi.wrapper = new FlowControlHandler<>(fi.fluidHandler, true, false);
            }
            if (be instanceof DummyBlockEntities.FluidOutput fo) {
                fo.fluidHandler = new FluidStacksResourceHandler(size, capacity);
                fo.wrapper = new FlowControlHandler<>(fo.fluidHandler, false, true);
            }
            if (be != null) {
                be.setChanged();
            }
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level actual_level && !actual_level.isClientSide()) {
            var facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
            for (var offset : DUMMY_OFFSETS) {
                var rotated = rotateOffset(offset, facing);
                var dummy_pos = pos.offset(rotated);
                var dummy_state = actual_level.getBlockState(dummy_pos);
                if (!(dummy_state.getBlock() instanceof DummyBlock)) continue;
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
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return rotate(HITBOX, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        return HITBOX;
    }

    public static VoxelShape rotate(VoxelShape shape, Direction direction) {
        if (direction == Direction.NORTH) return shape;
        VoxelShape result = Shapes.empty();
        for (AABB aabb : shape.toAabbs()) {
            result = Shapes.or(result, switch (direction) {
                case SOUTH -> Shapes.box(1 - aabb.maxX, aabb.minY, 1 - aabb.maxZ, 1 - aabb.minX, aabb.maxY, 1 - aabb.minZ);
                case WEST -> Shapes.box(aabb.minZ, aabb.minY, 1 - aabb.maxX, aabb.maxZ, aabb.maxY, 1 - aabb.minX);
                case EAST -> Shapes.box(1 - aabb.maxZ, aabb.minY, aabb.minX, 1 - aabb.minZ, aabb.maxY, aabb.maxX);
                default -> Shapes.box(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
            });
        }
        return result;
    }

    public static BlockPos rotateOffset(BlockPos offset, Direction direction) {
        return switch (direction) {
            case SOUTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            case WEST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
            case EAST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
            default -> offset;
        };
    }

    public void addItemInput(int facingOff, int yOff, int cw90Off, int size) {
        IO_OFFSETS.add(new BlockPos(facingOff, yOff, cw90Off));
        IO_TYPES.add(MananetNodeBlocks.ITEM_INPUT);
        IO_SIZES.add(size);
        IO_CAPACITIES.add(0);
    }

    public void addItemOutput(int facingOff, int yOff, int cw90Off, int size) {
        IO_OFFSETS.add(new BlockPos(facingOff, yOff, cw90Off));
        IO_TYPES.add(MananetNodeBlocks.ITEM_OUTPUT);
        IO_SIZES.add(size);
        IO_CAPACITIES.add(0);
    }

    public void addFluidInput(int facingOff, int yOff, int cw90Off, int size, int capacity) {
        IO_OFFSETS.add(new BlockPos(facingOff, yOff, cw90Off));
        IO_TYPES.add(MananetNodeBlocks.FLUID_INPUT);
        IO_SIZES.add(size);
        IO_CAPACITIES.add(capacity);
    }

    public void addFluidOutput(int facingOff, int yOff, int cw90Off, int size, int capacity) {
        IO_OFFSETS.add(new BlockPos(facingOff, yOff, cw90Off));
        IO_TYPES.add(MananetNodeBlocks.FLUID_OUTPUT);
        IO_SIZES.add(size);
        IO_CAPACITIES.add(capacity);
    }

}