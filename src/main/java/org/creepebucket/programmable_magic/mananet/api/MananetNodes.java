package org.creepebucket.programmable_magic.mananet.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.creepebucket.programmable_magic.mananet.logic.MananetNetworkLogic;

/**
 * Mananet 的外部入口：按位置获取节点访问器。
 *
 * <p>该入口会在需要时触发“集成”（integrate）：如果该位置是节点方块但尚未分配 network_id，
 * 会尝试根据邻接关系接入已有网络或创建新网络。</p>
 */
public final class MananetNodes {

    private MananetNodes() {}

    /**
     * 获取指定位置的节点访问器。
     *
 * <p>返回值可能为：</p>
 * <ul>
 *     <li>方块实体节点：直接返回该方块实体本身（实现 {@link MananetNode}）。</li>
 *     <li>节点方块：返回一个“按位置封装”的访问器。</li>
 *     <li>非节点：返回 {@code null}。</li>
 * </ul>
 */
    public static MananetNode get(ServerLevel level, BlockPos pos) {
        MananetNetworkLogic.integrateIfNeeded(level, pos);
        return MananetNetworkLogic.getNodeAccess(level, pos);
    }
}
