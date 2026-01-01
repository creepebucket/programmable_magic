package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.creepebucket.programmable_magic.mananet.api.MananetNode;
import org.creepebucket.programmable_magic.mananet.logic.MananetNetworkLogic;

/**
 * 魔力网络的“线缆”节点方块。
 *
 * <p>线缆本身不提供容量（cache）也不提供持续产出/消耗（load），默认只参与连通性。</p>
 */
public class ManaCableBlock extends AbstractNodeBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape CORE_SHAPE = box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape NORTH_SHAPE = box(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
    private static final VoxelShape EAST_SHAPE = box(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
    private static final VoxelShape SOUTH_SHAPE = box(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
    private static final VoxelShape WEST_SHAPE = box(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);
    private static final VoxelShape UP_SHAPE = box(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape DOWN_SHAPE = box(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);

    private static final VoxelShape[] SHAPES = new VoxelShape[64];
    static {
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = CORE_SHAPE;
            if ((mask & 0b000001) != 0) shape = Shapes.or(shape, DOWN_SHAPE);
            if ((mask & 0b000010) != 0) shape = Shapes.or(shape, UP_SHAPE);
            if ((mask & 0b000100) != 0) shape = Shapes.or(shape, NORTH_SHAPE);
            if ((mask & 0b001000) != 0) shape = Shapes.or(shape, EAST_SHAPE);
            if ((mask & 0b010000) != 0) shape = Shapes.or(shape, SOUTH_SHAPE);
            if ((mask & 0b100000) != 0) shape = Shapes.or(shape, WEST_SHAPE);
            SHAPES[mask] = shape;
        }
    }

    /**
     * 资源 id（不含命名空间）。
     */
    public static final String ID = "mana_cable";

    public ManaCableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
        );
    }

    /**
     * 注册方块与对应物品。
     */
    public static DeferredBlock<ManaCableBlock> register(DeferredRegister.Blocks blocks, DeferredRegister.Items items) {
        DeferredBlock<ManaCableBlock> block = blocks.register(ID, registryName -> new ManaCableBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(1.0f)
                        .noOcclusion()
                        .setId(ResourceKey.create(Registries.BLOCK, registryName))
        ));
        items.registerSimpleBlockItem(ID, block::get);
        return block;
    }

    @Override
    protected String getNodeRegistryIdInternal() {
        return ID;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        LevelReader level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return this.defaultBlockState();
        return this.defaultBlockState()
                .setValue(NORTH, can_connect(serverLevel, pos, Direction.NORTH))
                .setValue(EAST, can_connect(serverLevel, pos, Direction.EAST))
                .setValue(SOUTH, can_connect(serverLevel, pos, Direction.SOUTH))
                .setValue(WEST, can_connect(serverLevel, pos, Direction.WEST))
                .setValue(UP, can_connect(serverLevel, pos, Direction.UP))
                .setValue(DOWN, can_connect(serverLevel, pos, Direction.DOWN));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource randomSource) {
        if (!(level instanceof ServerLevel serverLevel)) return state;
        boolean connected = can_connect(serverLevel, pos, direction);
        return switch (direction) {
            case NORTH -> state.setValue(NORTH, connected);
            case EAST -> state.setValue(EAST, connected);
            case SOUTH -> state.setValue(SOUTH, connected);
            case WEST -> state.setValue(WEST, connected);
            case UP -> state.setValue(UP, connected);
            case DOWN -> state.setValue(DOWN, connected);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[shape_index(state)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[shape_index(state)];
    }

    private static int shape_index(BlockState state) {
        int mask = 0;
        if (state.getValue(DOWN)) mask |= 0b000001;
        if (state.getValue(UP)) mask |= 0b000010;
        if (state.getValue(NORTH)) mask |= 0b000100;
        if (state.getValue(EAST)) mask |= 0b001000;
        if (state.getValue(SOUTH)) mask |= 0b010000;
        if (state.getValue(WEST)) mask |= 0b100000;
        return mask;
    }

    private static boolean can_connect(ServerLevel level, BlockPos pos, Direction direction) {
        MananetNode neighbor = MananetNetworkLogic.getNodeAccess(level, pos.relative(direction));
        if (neighbor == null) return false;
        return neighbor.getConnectivity(direction.getOpposite());
    }

    @Override
    public void init_node_state(ServerLevel level, BlockPos pos, BlockState state, MananetNodeState node_state) {
        // 线缆默认状态保持 MananetNodeState 的初始值（cache/load 为 0，connectivity 全开）。
        super.init_node_state(level, pos, state, node_state);
    }
}
