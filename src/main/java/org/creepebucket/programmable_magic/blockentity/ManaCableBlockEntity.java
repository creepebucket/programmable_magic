package org.creepebucket.programmable_magic.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.Connection;
import net.minecraft.nbt.CompoundTag;
import org.creepebucket.programmable_magic.mana.api.IManaNetNode;
import org.creepebucket.programmable_magic.mana.simple.SimpleNetManager;

/**
 * 魔力线缆方块实体
 *
 * 职责：
 * - 实现 IManaNetNode，向网络层暴露连接性（isManaConnectable/canConnectTo）。
 * - 在 onLoadServer/setRemoved 时触发 SimpleNetManager 做就地连通染色。
 * - 仅维护单一 simpleNetId 作为网络标识（稳定：连通块最小坐标编码）。
 */
public class ManaCableBlockEntity extends BlockEntity implements IManaNetNode {

    private long simpleNetId = 0L; // 单层网络ID（服务器真实使用）

    public ManaCableBlockEntity(BlockPos pos, BlockState state) {
        super(org.creepebucket.programmable_magic.registries.ModBlockEntities.MANA_CABLE_BE.get(), pos, state);
    }

    @Override
    public boolean isManaConnectable() {return true;}

    @Override
    public boolean canConnectTo(Direction side) {return true;}

    @Override
    public Level getLevel() {return super.getLevel();}

    @Override
    public BlockPos getBlockPos() {return super.getBlockPos();}

    // 生命周期与邻居变化钩子：只在放置/邻居变化时触发 SimpleNetManager；
    // 卸载/保存阶段不做任何网络处理（避免卡在“Saving worlds”）。
    public void onLoadServer() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl)) return;
        SimpleNetManager.get(sl).onTopologyChanged(getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // 不在卸载阶段重建网络，避免世界保存/卸载时排队大量任务
    }

    public void onNeighborChanged(Direction side) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel sl)) return;
        SimpleNetManager.get(sl).onTopologyChanged(getBlockPos());
    }

    // 供服务器更新客户端展示值
    // 新：供 SimpleNetManager 写入单层网络ID
    public void setSimpleNetId(long id) {
        if (this.simpleNetId == id) return;
        boolean wasZero = (this.simpleNetId == 0);
        this.simpleNetId = id;
        // 仅在首次从 0 -> 非 0 时标脏，确保世界加载后具备持久 netId，后续变动不打脏以避免保存抖动
        if (wasZero && id != 0 && level != null) setChanged();
    }

    public long getSimpleNetId() { return simpleNetId; }

    // --- 同步与存档 ---
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
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }
}
