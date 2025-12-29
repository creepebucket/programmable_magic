package org.creepebucket.programmable_magic.mananet.nodes;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import org.creepebucket.programmable_magic.mananet.AbstractNetworkNode;
import org.creepebucket.programmable_magic.mananet.NetworkNodeRegistrar;
import org.creepebucket.programmable_magic.mananet.NodeBoundBlockEntity;
import org.creepebucket.programmable_magic.mananet.NodeRegistryData;

import java.util.function.Supplier;

public class ManaCableNode extends AbstractNetworkNode {
    public ManaCableNode(Block.Properties props, Supplier<BlockEntityType<NodeBoundBlockEntity>> beType) {
        super(props, beType);
    }

    public static void register() {
        NetworkNodeRegistrar.register(
                new NodeRegistryData("mana_cable", "", "", "", true),
                ManaCableNode::new
        );
    }
}
