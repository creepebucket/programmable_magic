package org.creepebucket.programmable_magic.mana.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * L0/L1 节点接口：供方块实体实现，用于与网络层交互。
 *
 * 设计说明：
 * - 接口仅暴露“连接性与位置信息”，不涉及 I/O 或存储；
 * - L0 管理器会在服务端根据 canConnectTo 的六向结果进行邻域传播合并；
 * - L1/L2 的计算由网络管理器代管，节点只需提供基本信息即可。
 */
public interface IManaNetNode {

    /**
     * 当前节点是否可参与网络（如被红石禁用则返回 false）。
     */
    boolean isManaConnectable();

    /**
     * 是否能与指定方向的相邻方块建立网络连接。
     */
    boolean canConnectTo(Direction side);

    /**
     * 获取世界与位置。
     */
    Level getLevel();
    BlockPos getBlockPos();
}


