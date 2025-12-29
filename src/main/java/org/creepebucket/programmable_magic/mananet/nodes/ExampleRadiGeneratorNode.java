package org.creepebucket.programmable_magic.mananet.nodes;

import org.creepebucket.programmable_magic.mananet.*;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 示例：辐射型发电节点，每刻向网络注入固定辐射魔力，并申报自身缓存上限。
 */
public class ExampleRadiGeneratorNode extends AbstractNetworkNode {
    private static final String TYPE = "radi";
    private static final double GEN_PER_TICK = 5.0;
    private static final double CACHE_CAPACITY = 1000.0;

    public ExampleRadiGeneratorNode(Block.Properties props, Supplier<BlockEntityType<NodeBoundBlockEntity>> beType) {
        super(props, beType);
    }

    public static void register() {
        NetworkNodeRegistrar.register(
                new NodeRegistryData("example_radi_generator", "", "", "", true),
                ExampleRadiGeneratorNode::new
        );
    }

    @Override
    public void tick(NodeBoundBlockEntity be) {
        ManaNet net = be.getNet();
        long key = be.getBlockPos().asLong();
        net.setCache(key, Map.of(TYPE, CACHE_CAPACITY));
        net.setLoad(key, Map.of(TYPE, -GEN_PER_TICK));
    }
}
