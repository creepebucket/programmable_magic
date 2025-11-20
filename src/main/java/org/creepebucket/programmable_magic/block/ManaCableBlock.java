package org.creepebucket.programmable_magic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    public ManaCableBlock(Properties props) {super(props);}

    // 精简：右键仅发送聊天消息，不再写入日志

    @Override
    public RenderShape getRenderShape(BlockState state) {return RenderShape.MODEL;}

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
                    return super.updateShape(state, level, scheduledTickAccess, currentPos, direction, neighborPos, neighborState, random);
                }
                cable.onNeighborChanged(direction);
            }
        }
        return super.updateShape(state, level, scheduledTickAccess, currentPos, direction, neighborPos, neighborState, random);
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
