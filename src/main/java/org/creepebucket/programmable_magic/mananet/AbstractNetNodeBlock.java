package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.creepebucket.programmable_magic.ModUtils;

import javax.annotation.Nullable;

/**
 * 抽象网络节点方块：只封装网络相关钩子，不包含模型/blockstate/碰撞箱细节。
 */
public abstract class AbstractNetNodeBlock extends Block implements EntityBlock {

    public AbstractNetNodeBlock(Properties props) { super(props); }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level instanceof ServerLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractNetNodeBlockEntity node) node.onLoadServer();
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos currentPos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (level instanceof ServerLevel sl) {
            BlockEntity be = sl.getBlockEntity(currentPos);
            if (be instanceof AbstractNetNodeBlockEntity node) node.onNeighborChanged(direction);
        }
        return state;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractNetNodeBlockEntity node)) return InteractionResult.SUCCESS;
        ManaNet net = node.getNet();
        if (net == null) return InteractionResult.SUCCESS;

        var totals = net.getTotalManaAll();
        StringBuilder sb = new StringBuilder();
        sb.append("net ").append(node.getNetworkIdDisplayName()).append(" total: ");
        if (totals.isEmpty()) {
            sb.append("{}");
        } else {
            boolean first = true;
            for (var e : totals.entrySet()) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(e.getKey()).append("=");
                sb.append(ModUtils.FormattedManaString(e.getValue()));
            }
        }
        sb.append(" | can_produce=").append(net.canProduce());

        ((ServerPlayer) player).sendSystemMessage(Component.literal(sb.toString()));
        return InteractionResult.SUCCESS;
    }
}
