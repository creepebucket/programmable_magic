package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.registries.ModAttachments;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NetNodeBlockEntity extends BlockEntity {
    public NetNodeBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntities.BASIC_MANA_CONNECTOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    public NetNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void connect(Level level, BlockPos connectedPos, Direction connectedFace, Direction selfFace) {
        var connected = level.getBlockEntity(connectedPos);

        var connections = new HashMap<>(connected.getData(ModAttachments.CONNECTIONS));
        connections.put(connectedFace, getBlockPos());
        connected.setData(ModAttachments.CONNECTIONS, connections);

        var selfConnections = new HashMap<>(getData(ModAttachments.CONNECTIONS));
        selfConnections.put(selfFace, connectedPos);
        setData(ModAttachments.CONNECTIONS, selfConnections);

        rebuildNetworkId(level, getBlockPos());
    }

    public void disconnect(BlockPos connectedPos) {
        var connections = new HashMap<>(getData(ModAttachments.CONNECTIONS));
        connections.entrySet().removeIf(e -> e.getValue().equals(connectedPos));
        setData(ModAttachments.CONNECTIONS, connections);

        // 若对方不是方块实体, 跳过断开对方连接的步骤
        if (getLevel().getBlockState(connectedPos).hasBlockEntity()
                && getLevel().getBlockEntity(connectedPos).hasData(ModAttachments.CONNECTIONS)) {
            var connected = getLevel().getBlockEntity(connectedPos);
            var connectedConnections = new HashMap<>(connected.getData(ModAttachments.CONNECTIONS));
            connectedConnections.entrySet().removeIf(e -> e.getValue().equals(getBlockPos()));
            connected.setData(ModAttachments.CONNECTIONS, connectedConnections);

            rebuildNetworkId(getLevel(), getBlockPos());
            rebuildNetworkId(getLevel(), connectedPos);
            }
    }

    public void disconnect(Direction selfFace) {
        var connections = new HashMap<>(getData(ModAttachments.CONNECTIONS));
        var connectedPos = connections.remove(selfFace);
        setData(ModAttachments.CONNECTIONS, connections);

        var connected = getLevel().getBlockEntity(connectedPos);
        var connectedConnections = new HashMap<>(connected.getData(ModAttachments.CONNECTIONS));
        connectedConnections.entrySet().removeIf(e -> e.getValue().equals(getBlockPos()));
        connected.setData(ModAttachments.CONNECTIONS, connectedConnections);

        rebuildNetworkId(getLevel(), getBlockPos());
        rebuildNetworkId(getLevel(), connectedPos);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!hasData(ModAttachments.CONNECTIONS)) setData(ModAttachments.CONNECTIONS, new HashMap<>());
        if (!getLevel().isClientSide() && getData(ModAttachments.NETWORK_ID) == 0L) setData(ModAttachments.NETWORK_ID, getBlockPos().asLong());
    }

    @Override
    public void setRemoved() {
        if (!getLevel().isClientSide()) {
            // 移除对向连接

            for (var connectedPos : new HashMap<>(getData(ModAttachments.CONNECTIONS)).values()) {
                disconnect(connectedPos);
            }
        }

        super.setRemoved();
    }

    public static void rebuildNetworkId(Level level, BlockPos startPos) {
        if (!(level.getBlockEntity(startPos) instanceof NetNodeBlockEntity)) return;

        var queue = new ArrayDeque<BlockPos>();
        var visited = new HashSet<BlockPos>();
        var nodes = new ArrayList<NetNodeBlockEntity>();

        queue.add(startPos);
        visited.add(startPos);

        var dimensionId = (long) level.dimension().identifier().hashCode() << 32;
        var networkId = dimensionId ^ startPos.asLong();

        while (!queue.isEmpty()) {
            var pos = queue.removeFirst();
            var blockEntity = (NetNodeBlockEntity) level.getBlockEntity(pos);
            nodes.add(blockEntity);

            var id = dimensionId ^ pos.asLong();
            if (id < networkId) networkId = id;

            for (var nextPos : blockEntity.getData(ModAttachments.CONNECTIONS).values()) {
                if (!visited.add(nextPos)) continue;
                queue.addLast(nextPos);
            }
        }

        for (var node : nodes) {
            node.setData(ModAttachments.NETWORK_ID, networkId);
        }
    }

    public NetworkManaData getNetworkData() {
        return NetworkManaManager.getManaData(getLevel(), getData(ModAttachments.NETWORK_ID));
    }
}
