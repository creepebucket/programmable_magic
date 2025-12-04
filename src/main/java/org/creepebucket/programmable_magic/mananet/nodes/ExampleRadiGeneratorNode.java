package org.creepebucket.programmable_magic.mananet.nodes;

import org.creepebucket.programmable_magic.mananet.*;

import java.util.Map;

/**
 * 示例：辐射型发电节点，每刻向网络注入固定辐射魔力，并申报自身缓存上限。
 */
public class ExampleRadiGeneratorNode extends AbstractNetworkNode {
    private static final String TYPE = "radi";
    private static final double GEN_PER_TICK = 5.0;
    private static final double CACHE_CAPACITY = 1000.0;

    @Override
    public void tick(NodeBoundBlockEntity be) {
        ManaNet net = be.getNet();
        long key = be.getBlockPos().asLong();
        net.setCache(key, Map.of(TYPE, CACHE_CAPACITY));
        net.setLoad(key, Map.of(TYPE, 0.0));
        net.addMana(TYPE, GEN_PER_TICK);
    }

    @Override
    public NodeRegistryData getRegistryData() {
        return new NodeRegistryData(
                "example_radi_generator",
                "", "", "",
                true
        );
    }
}

