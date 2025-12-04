package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.creepebucket.programmable_magic.mana.api.IManaNetNode;
import org.creepebucket.programmable_magic.mana.simple.SimpleNetManager;

/**
 * 抽象网络节点方块实体：统一 simpleNetId 存储与网络拓扑触发。
 */
public abstract class AbstractNetNodeBlockEntity extends BlockEntity implements IManaNetNode {
    private long simpleNetId = 0L;

    protected AbstractNetNodeBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean isManaConnectable() { return true; }

    @Override
    public boolean canConnectTo(Direction side) { return true; }

    @Override
    public Level getLevel() { return super.getLevel(); }

    @Override
    public BlockPos getBlockPos() { return super.getBlockPos(); }

    public void onLoadServer() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl)) return;
        SimpleNetManager.get(sl).onTopologyChanged(getBlockPos());
    }

    public void onNeighborChanged(Direction side) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl)) return;
        SimpleNetManager.get(sl).onTopologyChanged(getBlockPos());
    }

    public void setSimpleNetId(long id) {
        if (this.simpleNetId == id) return;
        boolean wasZero = (this.simpleNetId == 0);
        this.simpleNetId = id;
        if (wasZero && id != 0 && level != null) setChanged();
    }

    public long getSimpleNetId() { return simpleNetId; }

    /**
     * 获取当前网络操作对象（仅服务端，netId==0 返回 null）。
     */
    public ManaNet getNet() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl)) return null;
        if (simpleNetId == 0L) return null;
        return ManaNetService.get(sl).getNet(simpleNetId);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.simpleNetId = input.getLongOr("simpleNetId", this.simpleNetId);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("simpleNetId", this.simpleNetId);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
