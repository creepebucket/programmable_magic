package org.creepebucket.programmable_magic.mananet.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.logic.MananetNetworkLogic;

import java.util.UUID;
import javax.annotation.Nullable;

public abstract class AbstractNodeBlock extends Block implements MananetNode {

    protected AbstractNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public final void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && !state.hasBlockEntity()) MananetNetworkLogic.markDirty(serverLevel, pos);
    }

    @Override
    protected final void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (!state.hasBlockEntity()) MananetNetworkLogic.enqueueBlockRemoval(level, pos, this);
    }

    @Override
    protected final void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }

    public void init_node_state(ServerLevel level, BlockPos pos, BlockState state, MananetNodeState node_state) {}

    @Override
    public final Mana getCache() {
        throw new UnsupportedOperationException("node_block_has_no_instance_cache");
    }

    @Override
    public final void setCache(Mana mana) {
        throw new UnsupportedOperationException("node_block_has_no_instance_cache");
    }

    @Override
    public final Mana getLoad() {
        throw new UnsupportedOperationException("node_block_has_no_instance_load");
    }

    @Override
    public final void setLoad(Mana mana) {
        throw new UnsupportedOperationException("node_block_has_no_instance_load");
    }

    @Override
    public final void addMana(Mana mana) {
        throw new UnsupportedOperationException("node_block_has_no_instance_mana");
    }

    @Override
    public final boolean getConnectivity(Direction direction) {
        throw new UnsupportedOperationException("node_block_has_no_instance_connectivity");
    }

    @Override
    public final void setConnectivity(Direction direction, boolean connectivity) {
        throw new UnsupportedOperationException("node_block_has_no_instance_connectivity");
    }

    @Override
    public final Mana getMana() {
        throw new UnsupportedOperationException("node_block_has_no_instance_mana");
    }

    @Override
    public final boolean canProduce() {
        throw new UnsupportedOperationException("node_block_has_no_instance_can_produce");
    }

    @Override
    public final UUID getNetworkId() {
        throw new UnsupportedOperationException("node_block_has_no_instance_network_id");
    }

    @Override
    public final void setNetworkId(UUID networkId) {
        throw new UnsupportedOperationException("node_block_has_no_instance_network_id");
    }

    @Override
    public final Level getNodeLevel() {
        throw new UnsupportedOperationException("node_block_has_no_instance_level");
    }

    @Override
    public final BlockPos getNodePos() {
        throw new UnsupportedOperationException("node_block_has_no_instance_pos");
    }

    @Override
    public final String getNodeRegistryId() {
        return getNodeRegistryIdInternal();
    }

    protected abstract String getNodeRegistryIdInternal();
}
