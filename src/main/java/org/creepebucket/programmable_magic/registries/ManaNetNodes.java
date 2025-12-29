package org.creepebucket.programmable_magic.registries;

import org.creepebucket.programmable_magic.mananet.nodes.ExampleRadiGeneratorNode;
import org.creepebucket.programmable_magic.mananet.nodes.ManaBufferNode;
import org.creepebucket.programmable_magic.mananet.nodes.ManaCableNode;

public class ManaNetNodes {
    public static void main() {
        ExampleRadiGeneratorNode.register();
        ManaCableNode.register();
        ManaBufferNode.register();
    }
}
