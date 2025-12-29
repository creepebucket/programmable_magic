package org.creepebucket.programmable_magic.mananet.nodes;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.creepebucket.programmable_magic.mananet.*;

import java.util.Map;
import java.util.function.Supplier;

public class ManaBufferNode extends AbstractNetworkNode {
    private static final String TYPE = "radi";
    private static final double CACHE_CAPACITY = 1000.0;

    protected ManaBufferNode(Properties props, Supplier<BlockEntityType<NodeBoundBlockEntity>> beType) {
        super(props, beType);
    }

    public static void register() {
        NetworkNodeRegistrar.register(
                new NodeRegistryData("mana_buffer", "", "", "", true),
                ManaBufferNode::new
        );
    }

    @Override
    public void tick(NodeBoundBlockEntity be) {
        ManaNet net = be.getNet();
        long key = be.getBlockPos().asLong();
        net.setCache(key, Map.of(TYPE, CACHE_CAPACITY));
    }
}
