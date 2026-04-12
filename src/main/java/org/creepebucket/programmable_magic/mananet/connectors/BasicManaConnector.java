package org.creepebucket.programmable_magic.mananet.connectors;

import com.mojang.math.OctahedralGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.registries.ModAttachments;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class BasicManaConnector extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> ATTACH_FACE = BlockStateProperties.ATTACH_FACE;
    public static final Map<Direction, VoxelShape> SHAPES_GROUND =
            Shapes.rotateHorizontal(Shapes.or(Block.boxZ(12, 8.8, 9.6, 13), Block.boxZ(12, 2, 13, 14), Block.boxZ(4, 4, 14, 16)), OctahedralGroup.BLOCK_ROT_X_270);
    public static final Map<Direction, VoxelShape> SHAPES_WALL =
            Shapes.rotateHorizontal(Shapes.or(Block.boxZ(12, 8.8, 9.6, 13), Block.box(2, 8.4, 12, 14, 10.4, 14), Block.box(2, 10.4, 12, 14, 12.4, 15), Block.box(2, 12.4, 12, 14, 16, 16)), OctahedralGroup.BLOCK_ROT_X_270);
    public static final Map<AttachFace, Map<Direction, VoxelShape>> SHAPES = Map.of(
            AttachFace.FLOOR, SHAPES_GROUND,
            AttachFace.WALL, SHAPES_WALL,
            AttachFace.CEILING, SHAPES_GROUND
    );

    public BasicManaConnector(Properties properties) {
        super(properties);

        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACH_FACE, AttachFace.FLOOR));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACH_FACE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState state;
            if (direction.getAxis() == Direction.Axis.Y) {
                state = defaultBlockState()
                        .setValue(ATTACH_FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                        .setValue(FACING, context.getHorizontalDirection());
            } else {
                state = defaultBlockState().setValue(ATTACH_FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                return state;
            }
        }

        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new NetNodeBlockEntity(blockPos, blockState);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(ATTACH_FACE)).get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        var clickedFace = hitResult.getDirection();
        if (!clickedFace.getAxis().isHorizontal()) {
            player.displayClientMessage(Component.literal("拒绝连接: 只能点击水平面"), false);
            return InteractionResult.CONSUME;
        }

        // 检测玩家有没有等待连接的
        if (player.hasData(ModAttachments.PENDING_CONNECTION)) {
            // 连接双方

            var connectedPos = player.getData(ModAttachments.PENDING_CONNECTION);
            var connectedFace = player.getData(ModAttachments.PENDING_FACE);
            var selfFace = clickedFace;
            ((NetNodeBlockEntity) level.getBlockEntity(pos)).connect(level, connectedPos, connectedFace, selfFace);

            player.removeData(ModAttachments.PENDING_CONNECTION);
            player.removeData(ModAttachments.PENDING_FACE);
            player.displayClientMessage(Component.literal("已连接 " + connectedPos.toShortString() + " " + connectedFace.getName() + " -> " + pos.toShortString() + " " + selfFace.getName()), false);
            return InteractionResult.CONSUME;
        }

        player.setData(ModAttachments.PENDING_CONNECTION, pos);
        player.setData(ModAttachments.PENDING_FACE, clickedFace);
        player.displayClientMessage(Component.literal("连接起点已设置为 " + pos.toShortString() + " " + clickedFace.getName()), false);
        return InteractionResult.CONSUME;
    }
}
