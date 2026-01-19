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

/**
 * Mananet 的持久化桥接层。
 *
 * <p>这里连接了三层数据：</p>
 * <ul>
 *     <li>运行时：{@link MananetNetworkManager}（blockNodes、networks、parent）。</li>
 *     <li>chunk：{@code ModAttachments.CHUNK_NODES}（每个节点方块的 network_id/cache/load/connectivity）。</li>
 *     <li>世界：{@link MananetNetworkSavedData}（parent + 每个网络的当前 availableMana）。</li>
 * </ul>
 *
 * <p>加载时从 chunk 附件恢复节点状态，并把节点贡献汇总进网络；保存时把网络当前 availableMana 与 parent 写回 SavedData。</p>
 */
public final class MananetNetworkPersistence {

    private MananetNetworkPersistence() {}

    /**
     * chunk 加载：装载节点状态并汇总网络贡献。
     *
     * <p>额外会清理附件中“已不再是节点方块”的条目，避免存档残留污染运行时。</p>
     */
    public static void onChunkLoad(ServerLevel level, ChunkAccess chunk) {
        // 获取运行时管理器：用于装载节点状态、维护网络汇总、以及延迟 integrate 的 dirty 队列。
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        // 获取世界级 SavedData：用于装载 union-find parent 与每个网络的当前 availableMana。
        MananetNetworkSavedData saved = MananetNetworkSavedData.get(level);
        // parent 只需要装载一次：后续 chunk load 只做节点恢复与贡献汇总。
        if (!manager.isPersistentLoaded()) manager.loadPersistentParent(saved.parent());

        // 读取该 chunk 的节点附件数据（posLong -> NodeData）。
        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());
        boolean modified = false;
        for (var entry : nodes.entries()) {
            long posLong = entry.getLongKey();
            MananetChunkNodes.NodeData data = entry.getValue();
            BlockPos pos = BlockPos.of(posLong);
            BlockState state = level.getBlockState(pos);
            // 附件里记录的点不再是节点方块：清理掉，避免存档残留污染网络。
            if (!(state.getBlock() instanceof AbstractNodeBlock)) {
                nodes.remove(posLong);
                modified = true;
                continue;
            }

            // 该节点已被装入运行时缓存：跳过（避免重复累计贡献）。
            if (manager.getBlockNode(posLong) != null) continue;

            // 用附件数据恢复运行时节点状态。
            MananetNodeState nodeState = new MananetNodeState();
            nodeState.networkId = data.networkId;
            nodeState.connectivityMask = data.connectivityMask;
            nodeState.cache = data.cache;
            nodeState.load = data.load;
            manager.putBlockNode(posLong, nodeState);

            // 附件里还没有 network_id：标记 dirty，等 tick 中 integrate 自动接网并回写。
            if (nodeState.networkId == null) {
                manager.markDirty(pos);
                continue;
            }

            // 规范化为根 id，并确保附件中的 network_id 也同步为根 id。
            UUID root = manager.resolveNetworkId(nodeState.networkId);
            nodeState.networkId = root;
            if (data.networkId == null || !data.networkId.equals(root)) {
                data.networkId = root;
                modified = true;
            }

            // 确保网络对象存在：首次见到该网络时，把当前 availableMana 从 SavedData 装载到运行时。
            MananetNetworkManager.NetworkState network = manager.getNetworkIfPresent(root);
            if (network == null) {
                network = manager.getOrCreate(root);
                network.mana = saved.getMana(root);
            }
            // 把该节点贡献汇总到网络（cache/load/size）。
            manager.applyContribution(root, nodeState.cache, nodeState.load, 1);
        }
        // 如对附件做过清理/规范化，则回写到 chunk，保证持久化数据与运行时一致。
        if (modified) chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
    }

    /**
     * chunk 卸载：移除运行时缓存的节点状态，并扣除其对网络汇总（cache/load/size）的贡献。
     */
    public static void onChunkUnload(ServerLevel level, ChunkAccess chunk) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        if (!chunk.hasData(ModAttachments.CHUNK_NODES.get())) return;
        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());
        for (var entry : nodes.entries()) {
            long posLong = entry.getLongKey();
            // 只处理运行时里确实存在的节点状态；不存在则说明未装载或已被移除。
            MananetNodeState state = manager.getBlockNode(posLong);
            if (state == null) continue;
            // 从运行时缓存移除该节点，避免下次装载时重复累计。
            manager.removeBlockNode(posLong);
            if (state.networkId == null) continue;
            // 扣除该节点对网络 cache/load/size 的贡献（用负增量）。
            UUID root = manager.resolveNetworkId(state.networkId);
            manager.applyContribution(root, state.cache.negative(), state.load.negative(), -1);
        }
    }

    /**
     * 写入或更新某位置节点的持久化数据（chunk 附件）。
     *
     * <p>在写入前会把 {@link MananetNodeState#networkId} 规范化为网络根 id，保证持久化数据一致。</p>
     */
    public static void upsertNode(ServerLevel level, BlockPos pos, MananetNodeState state) {
        // 附件按 chunk 归档：先取得目标 chunk。
        var chunk = level.getChunkAt(pos);
        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());

        MananetNetworkManager manager = MananetNetworkManager.get(level);
        // 写入前把 network_id 规范化为根 id，避免把 union-find 的非根 id 持久化到存档里。
        state.networkId = manager.resolveNetworkId(state.networkId);

        // 组装附件节点数据（只存纯数据：network_id/connectivity/cache/load）。
        MananetChunkNodes.NodeData data = new MananetChunkNodes.NodeData();
        data.networkId = state.networkId;
        data.connectivityMask = state.connectivityMask;
        data.cache = state.cache;
        data.load = state.load;
        // 写入/覆盖该位置的节点数据，并回写 chunk 附件。
        nodes.put(pos.asLong(), data);
        chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
    }

    /**
     * 从 chunk 附件中移除某位置节点数据。
     */
    public static void removeNode(ServerLevel level, BlockPos pos) {
        var chunk = level.getChunkAt(pos);
        if (!chunk.hasData(ModAttachments.CHUNK_NODES.get())) return;
        MananetChunkNodes nodes = chunk.getData(ModAttachments.CHUNK_NODES.get());
        // 从附件中移除该位置的节点记录，并回写 chunk。
        nodes.remove(pos.asLong());
        chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
    }

    /**
     * 世界保存：把运行时 manager 写回世界级 SavedData。
     */
    public static void onLevelSave(ServerLevel level) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        // 只保存 parent + 当前 availableMana（cache/load/size 会在 chunk load 时由节点贡献重新汇总）。
        MananetNetworkSavedData.get(level).writeFromManager(manager);
    }

    /**
     * 批量更新多个位置的 network_id（用于网络拆分/合并后的回写）。
     *
     * <p>该方法按 chunk 分组后逐 chunk 更新附件，避免频繁跨 chunk 读写。</p>
     */
    public static void updateNetworkIdBulk(ServerLevel level, LongArrayList positions, UUID networkId) {
        if (positions.isEmpty()) return;

        // 先按 chunk 分组：避免对同一 chunk 重复 get/set 附件。
        Long2ObjectOpenHashMap<LongArrayList> byChunk = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < positions.size(); i++) {
            long posLong = positions.getLong(i);
            int chunkX = BlockPos.getX(posLong) >> 4;
            int chunkZ = BlockPos.getZ(posLong) >> 4;
            long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
            byChunk.computeIfAbsent(chunkKey, ignored -> new LongArrayList()).add(posLong);
        }

        // 逐 chunk 回写 network_id。
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
            // 回写该 chunk 的附件（触发持久化脏标记）。
            chunk.setData(ModAttachments.CHUNK_NODES.get(), nodes);
        }
    }
}
