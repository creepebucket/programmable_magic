package org.creepebucket.programmable_magic.mananet;

import java.util.Map;

/**
 * 魔力网络操作对象（面向节点）。
 *
 * 约定：
 * - 本对象代表某个 netId 的实时网络视图；
 * - setLoad/setCache 为“本节点贡献”的本刻申报值；
 * - addMana 为本刻外源注入；
 * - 每刻第一次访问时会完成一次结算（处理上一刻累计的 add/load/cache）。
 */
public interface ManaNet {

    // 读取网络总存量（按类型）
    double getTotalMana(String type);
    Map<String, Double> getTotalManaAll();

    // 节点贡献：负载（本刻期望消耗，按类型）
    void setLoad(long nodeKey, String type, double amount);
    void setLoad(long nodeKey, Map<String, Double> byType);

    // 节点贡献：缓存/容量（本刻可承载上限，按类型；<=0 视为不限制）
    void setCache(long nodeKey, String type, double capacity);
    void setCache(long nodeKey, Map<String, Double> byType);

    // 本刻外源注入（按类型，立即计入本刻可用）
    void addMana(String type, double amount);
    void addMana(Map<String, Double> byType);

    // 当前刻是否可满足“全网络负载”（所有类型都满足）
    boolean canProduce();
    // 指定类型是否可满足
    boolean canProduce(String type);
}

