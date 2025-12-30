package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.ManaMath;
import org.creepebucket.programmable_magic.mananet.api.MananetNode;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

import java.util.UUID;

final class MananetBlockNode implements MananetNode {

    private final ServerLevel level;
    private final BlockPos pos;
    private final AbstractNodeBlock block;
    private final MananetNetworkManager manager;
    private final MananetNodeState state;

    MananetBlockNode(ServerLevel level, BlockPos pos, AbstractNodeBlock block) {
        this.level = level;
        this.pos = pos;
        this.block = block;
        this.manager = MananetNetworkManager.get(level);
        BlockState state = level.getBlockState(pos);
        this.state = manager.getOrCreateBlockNode(pos, () -> {
            MananetNodeState node_state = new MananetNodeState();
            block.init_node_state(level, pos, state, node_state);
            return node_state;
        });
    }

    @Override
    public Mana getCache() {
        return state.cache;
    }

    @Override
    public void setCache(Mana mana) {
        Mana prev = state.cache;
        state.cache = mana;
        UUID id = getNetworkId();
        if (id != null) manager.applyContribution(id, ManaMath.delta(mana, prev), new Mana(), 0);
        else MananetNetworkLogic.markDirty(level, pos);
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public Mana getLoad() {
        return state.load;
    }

    @Override
    public void setLoad(Mana mana) {
        Mana prev = state.load;
        state.load = mana;
        UUID id = getNetworkId();
        if (id != null) manager.applyContribution(id, new Mana(), ManaMath.delta(mana, prev), 0);
        else MananetNetworkLogic.markDirty(level, pos);
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public void addMana(Mana mana) {
        manager.addMana(getNetworkId(), mana);
    }

    @Override
    public boolean getConnectivity(Direction direction) {
        int bit = 1 << direction.ordinal();
        return (state.connectivityMask & bit) != 0;
    }

    @Override
    public void setConnectivity(Direction direction, boolean connectivity) {
        boolean old = getConnectivity(direction);
        if (old == connectivity) return;
        int bit = 1 << direction.ordinal();
        if (connectivity) state.connectivityMask |= bit; else state.connectivityMask &= ~bit;
        MananetNetworkLogic.enqueueConnectivityChange(level, pos, direction, old, connectivity);
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public Mana getMana() {
        return manager.getMana(getNetworkId());
    }

    @Override
    public boolean canProduce() {
        return manager.canProduce(getNetworkId());
    }

    @Override
    public UUID getNetworkId() {
        return manager.resolveNetworkId(state.networkId);
    }

    @Override
    public void setNetworkId(UUID networkId) {
        state.networkId = networkId;
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    @Override
    public Level getNodeLevel() {
        return level;
    }

    @Override
    public BlockPos getNodePos() {
        return pos;
    }

    @Override
    public String getNodeRegistryId() {
        return block.getNodeRegistryId();
    }
}
