package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.creepebucket.programmable_magic.mananet.AbstractNetNodeBlock;

import javax.annotation.Nullable;

/**
 * 绑定到某个 AbstractNetworkNode 的方块实现。
 */
public class NodeBoundBlock extends AbstractNetNodeBlock implements EntityBlock {
    private final AbstractNetworkNode node;
    private final java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<NodeBoundBlockEntity>> beType;

    public NodeBoundBlock(Properties props, AbstractNetworkNode node,
                          java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<NodeBoundBlockEntity>> beType) {
        super(props);
        this.node = node;
        this.beType = beType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        var type = beType.get();
        return new NodeBoundBlockEntity(type, pos, state, node);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, be) -> {
            if (lvl.isClientSide) return;
            if (be instanceof NodeBoundBlockEntity nbe) NodeBoundBlockEntity.tick(lvl, p, st, nbe);
        };
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof NodeBoundBlockEntity nbe)) return InteractionResult.SUCCESS;
        if (!"example_radi_generator".equals(nbe.getNode().getRegistryData().name())) return InteractionResult.SUCCESS;
        ManaNet net = nbe.getNet();
        if (net == null) return InteractionResult.SUCCESS;

        var totals = net.getTotalManaAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Net ").append(Long.toUnsignedString(nbe.getSimpleNetId())).append(" total: ");
        if (totals.isEmpty()) {
            sb.append("{}");
        } else {
            boolean first = true;
            for (var e : totals.entrySet()) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(e.getKey()).append("=");
                sb.append(String.format(java.util.Locale.ROOT, "%.2f", e.getValue()));
            }
        }
        sb.append(" | canProduce=").append(net.canProduce());

        ((ServerPlayer) player).sendSystemMessage(Component.literal(sb.toString()));
        return InteractionResult.SUCCESS;
    }
}
