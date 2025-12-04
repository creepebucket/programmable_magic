package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.component.DataComponentType;
import java.util.List;

/**
 * 抽象网络节点：描述一类“可连接机器”的行为与元数据。
 *
 * - tick：方块实体每刻回调（服务端/客户端根据实现自行判断）。
 * - getDataComponentTypes：需要的 DataComponentType 定义（默认空）。
 * - getRegistryData：注册所需的基础信息（注册名/本地化键/模型材质等）。
 */
public abstract class AbstractNetworkNode {

    /**
     * 每刻回调，由注册器绑定到对应方块实体的 ticker。
     */
    public void tick(NodeBoundBlockEntity be) {}

    /**
     * 需要的 DataComponentType 定义（如有）。
     */
    public List<DataComponentType<?>> getDataComponentTypes() { return List.of(); }

    /**
     * 注册与资源信息。
     */
    public abstract NodeRegistryData getRegistryData();
}

