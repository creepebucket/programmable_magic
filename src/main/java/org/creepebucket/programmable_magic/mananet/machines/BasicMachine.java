package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.registries.MananetNodeBlocks;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasicMachine extends Block implements EntityBlock {

    public final VoxelShape HITBOX = hitbox();
    public List<RelativeBlockPos> DUMMY_OFFSETS;
    public Map<RelativeBlockPos, String> IO_DEFINITION = new HashMap<>(); // v = "item_input"/"item_output"/"fluid_output"/"fluid_input"
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public VoxelShape[] HITBOXES = {rotate(HITBOX, Direction.SOUTH), rotate(HITBOX, Direction.WEST), HITBOX, rotate(HITBOX, Direction.EAST)};

    {
        var offsets = new ArrayList<RelativeBlockPos>();
        for (var offset : BlockPos.betweenClosed(-4, -4, -4, 4, 4, 4)) {
            if (offset.getX() == 0 && offset.getY() == 0 && offset.getZ() == 0) continue;
            if (Shapes.joinIsNotEmpty(HITBOX, Shapes.block().move(offset), BooleanOp.AND)) {
                offsets.add(new RelativeBlockPos(-offset.getZ(), offset.getY(), offset.getX()));
            }
        }
        DUMMY_OFFSETS = List.copyOf(offsets);
    }

    public BasicMachine(Properties p_49795_) {
        super(p_49795_);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public abstract VoxelShape hitbox();

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var dir = context.getHorizontalDirection().getOpposite();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) dir = dir.getOpposite();

        var level = context.getLevel();
        var pos = context.getClickedPos();
        for (var offset : DUMMY_OFFSETS) {
            var rotated = offset.toAbsolutePos(dir);
            if (!level.getBlockState(pos.offset(rotated)).canBeReplaced()) return null;
        }
        return defaultBlockState().setValue(FACING, dir);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;

        // 放置dummy
        var facing = state.getValue(FACING);
        var dummy_block = MananetNodeBlocks.DUMMY_BLOCK.get();
        for (var offset : DUMMY_OFFSETS) {
            var rotated = offset.toAbsolutePos(facing);
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
        var ioDummyBlock = MananetNodeBlocks.IO_DUMMY_BLOCK.get();
        for (var offset : IO_DEFINITION.keySet()) {
            var rotated = offset.toAbsolutePos(facing);
            if (rotated.equals(BlockPos.ZERO)) continue;
            level.setBlock(
                    pos.offset(rotated),
                    ioDummyBlock.defaultBlockState()
                            .setValue(DummyBlock.X_OFFSET, -rotated.getX())
                            .setValue(DummyBlock.Y_OFFSET, -rotated.getY())
                            .setValue(DummyBlock.Z_OFFSET, -rotated.getZ()),
                    Block.UPDATE_ALL
            );
        }
    }

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof Level actual_level && !actual_level.isClientSide()) {
			var dir = state.getValue(FACING);
			for (var offset : DUMMY_OFFSETS) {
				var rotated = offset.toAbsolutePos(dir);
				var dummy_pos = pos.offset(rotated);
				var dummy_state = actual_level.getBlockState(dummy_pos);
				if (!(dummy_state.getBlock() instanceof DummyBlock)) continue;
				if (!DummyBlock.get_main_pos(dummy_pos, dummy_state).equals(pos)) continue;
				actual_level.setBlock(
						dummy_pos,
						actual_level.getFluidState(dummy_pos).createLegacyBlock(),
						Block.UPDATE_ALL
				);
			}
		}
	}

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HITBOXES[state.getValue(FACING).get2DDataValue()];
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

    public void addItemInput(RelativeBlockPos pos) {
        IO_DEFINITION.put(pos, "item_input");
    }

    public void addItemOutput(RelativeBlockPos pos) {
        IO_DEFINITION.put(pos, "item_output");
    }

    public void addFluidInput(RelativeBlockPos pos) {
        IO_DEFINITION.put(pos, "fluid_input");
    }

    public void addFluidOutput(RelativeBlockPos pos) {
        IO_DEFINITION.put(pos, "fluid_output");
    }

    public InteractionResult openIoMenu(Level level, BlockPos pos, Player player) {
        if (!IO_DEFINITION.containsKey(RelativeBlockPos.ZERO)) return InteractionResult.PASS;
        return IoDummyBlock.openIoMenu(level, pos, RelativeBlockPos.ZERO, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isCrouching()) {
            var result = openIoMenu(level, pos, player);
            if (result != InteractionResult.PASS) return result;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var provider = state.getMenuProvider(level, pos);
            if (provider == null) return InteractionResult.PASS;
            serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isCrouching()) {
            var result = openIoMenu(level, pos, player);
            if (result != InteractionResult.PASS) return result;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

}
