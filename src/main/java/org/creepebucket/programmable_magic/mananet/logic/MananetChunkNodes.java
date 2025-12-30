package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.creepebucket.programmable_magic.ModUtils.Mana;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.UUID;

public final class MananetChunkNodes implements ValueIOSerializable {

    private final Long2ObjectOpenHashMap<NodeData> nodes = new Long2ObjectOpenHashMap<>();

    public NodeData get(long posLong) {
        return nodes.get(posLong);
    }

    public void put(long posLong, NodeData data) {
        nodes.put(posLong, data);
    }

    public void remove(long posLong) {
        nodes.remove(posLong);
    }

    public LongIterator positions() {
        return nodes.keySet().iterator();
    }

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

    public static final class NodeData implements ValueIOSerializable {
        public UUID networkId;
        public int connectivityMask = 0b111111;
        public Mana cache = new Mana();
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

