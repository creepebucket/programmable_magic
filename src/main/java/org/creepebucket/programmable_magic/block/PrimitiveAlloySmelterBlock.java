package org.creepebucket.programmable_magic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.creepebucket.programmable_magic.blockentity.PrimitiveAlloySmelterBlockEntity;

public class PrimitiveAlloySmelterBlock extends Block implements EntityBlock {
    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);
    public static final EnumProperty<net.minecraft.core.Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public PrimitiveAlloySmelterBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STATUS, Status.EMPTY)
                .setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS, FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(STATUS, Status.EMPTY);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrimitiveAlloySmelterBlockEntity(pos, state);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 定时从燃烧状态转为阻挡状态
        if (state.hasProperty(STATUS) && state.getValue(STATUS) == Status.BURN) {
            level.setBlock(pos, state.setValue(STATUS, Status.BLOCKED), 3);
        }
    }

    @Override
    public BlockState playerWillDestroy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, Player player) {
        BlockState ret = super.playerWillDestroy(level, pos, state, player);
        if (level instanceof ServerLevel sl) {
            // 所有状态都掉 3 个“砖”（Items.BRICK）
            popResource(sl, pos, new ItemStack(net.minecraft.world.item.Items.BRICK, 3));

            // 仅当阻挡状态且使用“镐类工具”时，额外掉合金与黑曜石
            if (state.hasProperty(STATUS) && state.getValue(STATUS) == Status.BLOCKED) {
                ItemStack held = player.getMainHandItem();
                Tool tool = held.get(DataComponents.TOOL);
                boolean pickaxeLike = tool != null && tool.isCorrectForDrops(Blocks.STONE.defaultBlockState());
                if (pickaxeLike) {
                    popResource(sl, pos, new ItemStack(org.creepebucket.programmable_magic.registries.ModItems.REDSTONE_GOLD_ALLOY.get(), 2));
                    popResource(sl, pos, new ItemStack(Blocks.OBSIDIAN.asItem(), 1));
                }
            }
        }
        return ret;
    }

    public enum Status implements StringRepresentable {
        EMPTY("empty"),
        BURN("burn"),
        BLOCKED("blocked");

        private final String name;
        Status(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
        @Override public String toString() { return name; }
    }
}
