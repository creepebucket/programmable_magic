package org.creepebucket.programmable_magic.mananet.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.creepebucket.programmable_magic.mananet.logic.MananetNetworkLogic;

public final class MananetNodes {

    private MananetNodes() {}

    public static MananetNode get(ServerLevel level, BlockPos pos) {
        MananetNetworkLogic.integrateIfNeeded(level, pos);
        return MananetNetworkLogic.getNodeAccess(level, pos);
    }
}
