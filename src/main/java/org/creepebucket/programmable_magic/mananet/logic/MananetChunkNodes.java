package org.creepebucket.programmable_magic.mananet.logic;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.creepebucket.programmable_magic.ModUtils.Mana;

import java.util.UUID;

/**
 * chunk 附件：保存“节点方块”的按位置数据。
 *
 * <p>该数据用于跨加载周期保留：</p>
 * <ul>
 *     <li>节点所属 network_id（UUID）</li>
 *     <li>连通掩码 connectivity_mask</li>
 *     <li>cache/load 两个贡献量</li>
 * </ul>
 *
 * <p>序列化字段名保持稳定，便于后续数据迁移或排查存档内容。</p>
 */
public final class MananetChunkNodes implements ValueIOSerializable {

    private final Long2ObjectOpenHashMap<NodeData> nodes = new Long2ObjectOpenHashMap<>();

    /**
     * 按 {@code BlockPos.asLong()} 读取节点数据。
     */
    public NodeData get(long posLong) {
        return nodes.get(posLong);
    }

    public void put(long posLong, NodeData data) {
        nodes.put(posLong, data);
    }

    public void remove(long posLong) {
        nodes.remove(posLong);
    }

    /**
     * 迭代所有位置 key。
     */
    public LongIterator positions() {
        return nodes.keySet().iterator();
    }

    /**
     * 迭代所有条目（posLong -> NodeData）。
     */
    public Iterable<Long2ObjectOpenHashMap.Entry<NodeData>> entries() {
        return nodes.long2ObjectEntrySet();
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("nodes");
        for (Long2ObjectOpenHashMap.Entry<NodeData> entry : nodes.long2ObjectEntrySet()) {
            ValueOutput child = list.addChild();
            child.putLong("pos_long", entry.getLongKey());
            entry.getValue().serialize(child);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        nodes.clear();
        for (ValueInput child : input.childrenListOrEmpty("nodes")) {
            long posLong = child.getLongOr("pos_long", 0L);
            NodeData data = new NodeData();
            data.deserialize(child);
            nodes.put(posLong, data);
        }
    }

    /**
     * 单个节点方块的持久化数据（随 chunk 保存/加载）。
     */
    public static final class NodeData implements ValueIOSerializable {
        /**
         * 节点所属 network_id；为 null 表示尚未集成（integrate）。
         */
        public UUID networkId;
        /**
         * 6 个方向连通掩码，位序与 {@code Direction.ordinal()} 一致。
         */
        public int connectivityMask = 0b111111;
        /**
         * 本节点 cache 贡献。
         */
        public Mana cache = new Mana();
        /**
         * 本节点 load 贡献（每秒净负载）。
         */
        public Mana load = new Mana();

        @Override
        public void serialize(ValueOutput output) {
            if (networkId != null) output.putString("network_id", networkId.toString());
            output.putInt("connectivity_mask", connectivityMask);
            output.putDouble("cache_radiation", cache.getRadiation());
            output.putDouble("cache_temperature", cache.getTemperature());
            output.putDouble("cache_momentum", cache.getMomentum());
            output.putDouble("cache_pressure", cache.getPressure());
            output.putDouble("load_radiation", load.getRadiation());
            output.putDouble("load_temperature", load.getTemperature());
            output.putDouble("load_momentum", load.getMomentum());
            output.putDouble("load_pressure", load.getPressure());
        }

        @Override
        public void deserialize(ValueInput input) {
            networkId = input.getString("network_id").map(UUID::fromString).orElse(null);
            connectivityMask = input.getIntOr("connectivity_mask", 0b111111);
            cache = new Mana(
                    input.getDoubleOr("cache_radiation", 0.0),
                    input.getDoubleOr("cache_temperature", 0.0),
                    input.getDoubleOr("cache_momentum", 0.0),
                    input.getDoubleOr("cache_pressure", 0.0)
            );
            load = new Mana(
                    input.getDoubleOr("load_radiation", 0.0),
                    input.getDoubleOr("load_temperature", 0.0),
                    input.getDoubleOr("load_momentum", 0.0),
                    input.getDoubleOr("load_pressure", 0.0)
            );
        }
    }
}
