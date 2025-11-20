package org.creepebucket.programmable_magic.mana.api;

import net.minecraft.core.Direction;

import java.util.Map;

/**
 * L2 母线接口：抽象整个连通网络的魔力存储与 I/O。
 *
 * 设计说明：
 * - 本接口不关心拓扑，仅代表“一个全局连通网络”的存取入口；
 * - 由持久化层提供具体实现（当前项目中的 {@code ManaNetworkSavedData.BusState}）；
 * - 建议调用方优先走聚合后的网络 I/O，而非逐块实体直接交互，以降低开销。
 */
public interface IManaBus {

    /**
     * 获取该网络的唯一 id。
     */
    int getNetId();

    /**
     * 查询网络内各类型魔力存量（快照）。
     * 注意：返回的是副本，修改不会回写到网络。
     */
    Map<String, Long> getManaSnapshot();

    /**
     * 向网络注入指定类型的魔力，返回实际写入量（可能因容量/策略受限）。
     */
    long insertMana(String type, long amount);

    /**
     * 从网络提取指定类型的魔力，返回实际提取量。
     */
    long extractMana(String type, long amount);

    /**
     * 可选：按面向（方向）进行策略区分（默认忽略）。
     */
    default long insertMana(String type, long amount, Direction face) {return insertMana(type, amount);}    

    default long extractMana(String type, long amount, Direction face) {return extractMana(type, amount);}    
}


