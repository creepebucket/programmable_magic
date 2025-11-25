package org.creepebucket.programmable_magic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.creepebucket.programmable_magic.blockentity.ManaCableBlockEntity;

import javax.annotation.Nullable;

/**
 * 魔力线缆方块
 *
 * 职责：
 * - 作为载体承载方块实体（ManaCableBlockEntity）。
 * - 放置/邻接变化时触发 SimpleNetManager 做就地染色；
 * - 空手右键时在聊天框显示 simpleNetId（调试用途）。
 */
public class ManaCableBlock extends Block implements EntityBlock {

    // 六向连接可视属性 + 中心是否有连接（仅用于模型显示）
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty CENTER = BooleanProperty.create("center");

    public ManaCableBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(CENTER, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, CENTER);
    }

    // 精简：右键仅发送聊天消息，不再写入日志

    @Override
    public RenderShape getRenderShape(BlockState state) {return RenderShape.MODEL;}

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        boolean u = shouldConnectTo(level, pos, Direction.UP);
        boolean d = shouldConnectTo(level, pos, Direction.DOWN);
        boolean n = shouldConnectTo(level, pos, Direction.NORTH);
        boolean s = shouldConnectTo(level, pos, Direction.SOUTH);
        boolean e = shouldConnectTo(level, pos, Direction.EAST);
        boolean w = shouldConnectTo(level, pos, Direction.WEST);
        return this.defaultBlockState()
                .setValue(UP, u)
                .setValue(DOWN, d)
                .setValue(NORTH, n)
                .setValue(SOUTH, s)
                .setValue(EAST, e)
                .setValue(WEST, w)
                .setValue(CENTER, (u || d || n || s || e || w));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {return new ManaCableBlockEntity(pos, state);}    

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level instanceof ServerLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaCableBlockEntity cable) cable.onLoadServer();
        }
    }

    // 统一在 updateShape 钩子上处理邻居变化（1.21.8 签名）
    /**
     * 邻居变化时：
     * - 仅当相邻方块也是线缆时才触发一次拓扑重建；
     * - 若两端 simpleNetId 已经非 0 且相等（已收敛），则跳过重建，避免加载阶段重复刷；
     * - 所有拓扑任务在同一 tick 尾部合并处理（见 SimpleNetManager）。
     */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos currentPos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        // 仅在相邻也是线缆时，触发一次邻接连通性刷新，避免无关更新导致的过度触发
        if (level instanceof ServerLevel sl && neighborState.getBlock() instanceof ManaCableBlock) {
            BlockEntity be = sl.getBlockEntity(currentPos);
            BlockEntity nbe = sl.getBlockEntity(neighborPos);
            if (be instanceof ManaCableBlockEntity cable && nbe instanceof ManaCableBlockEntity neighbor) {
                long a = cable.getSimpleNetId();
                long b = neighbor.getSimpleNetId();
                // 若两端已有非零且一致的网络ID，则视为已收敛，跳过拓扑重建，减少世界初次加载的开销
                if (a != 0 && b != 0 && a == b) {
                    // 同步全部方向 + 中心连接属性
                    return recomputeConnections(state, level, currentPos);
                }
                cable.onNeighborChanged(direction);
            }
        }
        // 同步全部方向 + 中心连接属性
        return recomputeConnections(state, level, currentPos);
    }

    private BooleanProperty propOf(Direction d) {
        return switch (d) {
            case UP -> UP;
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
        };
    }

    private boolean shouldConnectTo(LevelReader level, BlockPos pos, Direction dir) {
        BlockPos np = pos.relative(dir);
        BlockState ns = level.getBlockState(np);
        // KISS：仅与相同线缆方块相连时显示连接模型
        return ns.getBlock() instanceof ManaCableBlock;
    }

    private BlockState recomputeConnections(BlockState base, LevelReader level, BlockPos pos) {
        boolean u = shouldConnectTo(level, pos, Direction.UP);
        boolean d = shouldConnectTo(level, pos, Direction.DOWN);
        boolean n = shouldConnectTo(level, pos, Direction.NORTH);
        boolean s = shouldConnectTo(level, pos, Direction.SOUTH);
        boolean e = shouldConnectTo(level, pos, Direction.EAST);
        boolean w = shouldConnectTo(level, pos, Direction.WEST);
        return base
            .setValue(UP, u)
            .setValue(DOWN, d)
            .setValue(NORTH, n)
            .setValue(SOUTH, s)
            .setValue(EAST, e)
            .setValue(WEST, w)
            .setValue(CENTER, (u || d || n || s || e || w));
    }

    // 选择与碰撞箱：与几何一致（中心 6×6×6，连接臂厚度 5）
    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext ctx) {
        return shapeFor(state);
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext ctx) {
        return shapeFor(state);
    }

    private static final net.minecraft.world.phys.shapes.VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final net.minecraft.world.phys.shapes.VoxelShape ARM_UP = Block.box(5, 11, 5, 11, 16, 11);
    private static final net.minecraft.world.phys.shapes.VoxelShape ARM_DOWN = Block.box(5, 0, 5, 11, 5, 11);
    private static final net.minecraft.world.phys.shapes.VoxelShape ARM_NORTH = Block.box(5, 5, 0, 11, 11, 5);
    private static final net.minecraft.world.phys.shapes.VoxelShape ARM_SOUTH = Block.box(5, 5, 11, 11, 11, 16);
    private static final net.minecraft.world.phys.shapes.VoxelShape ARM_WEST = Block.box(0, 5, 5, 5, 11, 11);
    private static final net.minecraft.world.phys.shapes.VoxelShape ARM_EAST = Block.box(11, 5, 5, 16, 11, 11);

    private static net.minecraft.world.phys.shapes.VoxelShape shapeFor(BlockState s) {
        net.minecraft.world.phys.shapes.VoxelShape sh = CORE;
        if (s.getValue(UP)) sh = net.minecraft.world.phys.shapes.Shapes.or(sh, ARM_UP);
        if (s.getValue(DOWN)) sh = net.minecraft.world.phys.shapes.Shapes.or(sh, ARM_DOWN);
        if (s.getValue(NORTH)) sh = net.minecraft.world.phys.shapes.Shapes.or(sh, ARM_NORTH);
        if (s.getValue(SOUTH)) sh = net.minecraft.world.phys.shapes.Shapes.or(sh, ARM_SOUTH);
        if (s.getValue(EAST)) sh = net.minecraft.world.phys.shapes.Shapes.or(sh, ARM_EAST);
        if (s.getValue(WEST)) sh = net.minecraft.world.phys.shapes.Shapes.or(sh, ARM_WEST);
        return sh;
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaCableBlockEntity cable) {
                String msg = "ManaCable at "+pos+" netId="+Long.toUnsignedString(cable.getSimpleNetId());
                ((ServerPlayer)player).sendSystemMessage(net.minecraft.network.chat.Component.literal(msg));
            }
        }
        return InteractionResult.SUCCESS;
    }

}
