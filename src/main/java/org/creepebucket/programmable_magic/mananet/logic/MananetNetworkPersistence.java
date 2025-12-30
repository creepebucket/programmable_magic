package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;
import org.creepebucket.programmable_magic.registries.ModAttachments;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.UUID;

public final class MananetNetworkPersistence {

    private MananetNetworkPersistence() {}

    public static void onChunkLoad(ServerLevel level, ChunkAccess chunk) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        MananetNetworkSavedData saved = MananetNetworkSavedData.get(level);
        if (!manager.isPersistentLoaded()) manager.loadPersistentParent(saved.parent());

        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());
        boolean modified = false;
        for (var entry : nodes.entries()) {
            long posLong = entry.getLongKey();
            MananetChunkNodes.NodeData data = entry.getValue();
            BlockPos pos = BlockPos.of(posLong);
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof AbstractNodeBlock)) {
                nodes.remove(posLong);
                modified = true;
                continue;
            }

            if (manager.getBlockNode(posLong) != null) continue;

            MananetNodeState nodeState = new MananetNodeState();
            nodeState.networkId = data.networkId;
            nodeState.connectivityMask = data.connectivityMask;
            nodeState.cache = data.cache;
            nodeState.load = data.load;
            manager.putBlockNode(posLong, nodeState);

            if (nodeState.networkId == null) {
                manager.markDirty(pos);
                continue;
            }

            UUID root = manager.resolveNetworkId(nodeState.networkId);
            nodeState.networkId = root;
            if (data.networkId == null || !data.networkId.equals(root)) {
                data.networkId = root;
                modified = true;
            }

            MananetNetworkManager.NetworkState network = manager.getNetworkIfPresent(root);
            if (network == null) {
                network = manager.getOrCreate(root);
                network.mana = saved.getMana(root);
            }
            manager.applyContribution(root, nodeState.cache, nodeState.load, 1);
        }
        if (modified) chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
    }

    public static void onChunkUnload(ServerLevel level, ChunkAccess chunk) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        if (!chunk.hasData(ModAttachments.CHUNK_NODES.get())) return;
        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());
        for (var entry : nodes.entries()) {
            long posLong = entry.getLongKey();
            MananetNodeState state = manager.getBlockNode(posLong);
            if (state == null) continue;
            manager.removeBlockNode(posLong);
            if (state.networkId == null) continue;
            UUID root = manager.resolveNetworkId(state.networkId);
            manager.applyContribution(root, state.cache.negative(), state.load.negative(), -1);
        }
    }

    public static void upsertNode(ServerLevel level, BlockPos pos, MananetNodeState state) {
        var chunk = level.getChunkAt(pos);
        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());

        MananetNetworkManager manager = MananetNetworkManager.get(level);
        state.networkId = manager.resolveNetworkId(state.networkId);

        MananetChunkNodes.NodeData data = new MananetChunkNodes.NodeData();
        data.networkId = state.networkId;
        data.connectivityMask = state.connectivityMask;
        data.cache = state.cache;
        data.load = state.load;
        nodes.put(pos.asLong(), data);
        chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
    }

    public static void removeNode(ServerLevel level, BlockPos pos) {
        var chunk = level.getChunkAt(pos);
        if (!chunk.hasData(ModAttachments.CHUNK_NODES.get())) return;
        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());
        nodes.remove(pos.asLong());
        chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
    }

    public static void onLevelSave(ServerLevel level) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        MananetNetworkSavedData.get(level).writeFromManager(manager);
    }

    public static void updateNetworkIdBulk(ServerLevel level, LongArrayList positions, UUID networkId) {
        if (positions.isEmpty()) return;

        Long2ObjectOpenHashMap<LongArrayList> byChunk = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < positions.size(); i++) {
            long posLong = positions.getLong(i);
            int chunkX = BlockPos.getX(posLong) >> 4;
            int chunkZ = BlockPos.getZ(posLong) >> 4;
            long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
            byChunk.computeIfAbsent(chunkKey, ignored -> new LongArrayList()).add(posLong);
        }

        for (var entry : byChunk.long2ObjectEntrySet()) {
            long chunkKey = entry.getLongKey();
            int chunkX = ChunkPos.getX(chunkKey);
            int chunkZ = ChunkPos.getZ(chunkKey);
            var chunk = level.getChunk(chunkX, chunkZ);
            MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());
            LongArrayList list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                long posLong = list.getLong(i);
                MananetChunkNodes.NodeData data = nodes.get(posLong);
                if (data != null) data.networkId = networkId;
            }
            chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
        }
    }
}
