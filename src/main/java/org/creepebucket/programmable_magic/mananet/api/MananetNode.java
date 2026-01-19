package org.creepebucket.programmable_magic.mananet.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.ModUtils.Mana;

import java.util.UUID;

/**
 * 魔力网络（Mananet）中的“节点”抽象。
 *
 * <p>节点可能来自两类对象：</p>
 * <ul>
 *     <li>方块实体：直接实现 {@link MananetNode}，拥有自身实例状态。</li>
 *     <li>普通方块：继承 {@link AbstractNodeBlock} 作为“节点方块”，
 *     其实际状态由运行时管理器维护，并通过访问器包装成 {@link MananetNode}。</li>
 * </ul>
 *
 * <p>本接口的核心是把“单个节点的贡献（cache/load/connectivity）”与“网络汇总状态（availableMana/union）”解耦：</p>
 * <ul>
 *     <li>{@code cache/load/connectivity} 属于节点自身（或节点状态）。</li>
 *     <li>{@code availableMana/network_id} 属于网络（或节点当前所属网络）。</li>
 * </ul>
 */
public interface MananetNode {

    /**
     * 本节点对网络“容量上限”的贡献（缓存上限）。
     *
     * <p>网络的总缓存上限通常为所有节点缓存的求和；网络当前魔力会被夹紧到该上限。</p>
     */
    Mana getCache();

    /**
     * 设置本节点缓存上限贡献。
     *
     * <p>对方块节点而言，该变更会反馈到网络聚合数据，并写入持久化数据。</p>
     */
    void setCache(Mana mana);

    /**
     * 本节点声明的“每秒净负载”。
     *
     * <p>约定：正值表示消耗（每秒从网络扣除），负值表示产出（每秒向网络注入）。</p>
     */
    Mana getLoad();

    /**
     * 设置本节点每秒净负载声明。
     *
     * <p>对方块节点而言，该变更会反馈到网络聚合数据，并写入持久化数据。</p>
     */
    void setLoad(Mana mana);

    /**
     * 直接对节点所属网络的当前魔力做增量修改。
     *
     * <p>该操作作用于“网络当前 availableMana”，与 {@link #getLoad()} 的“持续每秒变化”是两套机制。</p>
     */
    void addMana(Mana mana);

    /**
     * 查询指定方向是否允许连通。
     *
     * <p>连通需要双方都允许：{@code A.getConnectivity(dir)} 与 {@code B.getConnectivity(dir.getOpposite())}。</p>
     */
    boolean getConnectivity(Direction direction);

    /**
     * 设置指定方向的连通开关。
     *
     * <p>该方法只表达“本节点是否允许在该方向连边”；实际网络合并/拆分由逻辑层在 tick 中处理。</p>
     */
    void setConnectivity(Direction direction, boolean connectivity);

    default void setConnectivity(boolean connectivity, Direction direction) {
        setConnectivity(direction, connectivity);
    }

    /**
     * 获取节点所属网络的当前魔力快照。
     *
     * <p>对方块节点而言，读取的是网络汇总状态；对方块实体节点则由其自身决定实现方式。</p>
     */
    Mana getMana();

    default Mana getmana() {
        return getMana();
    }

    /**
     * 查询网络是否“能持续运行产出/消耗”。
     *
     * <p>当前实现用于判断网络是否能支付 {@link #getLoad()} 的正向消耗部分。</p>
     */
    boolean canProduce();

    /**
     * 当前节点所属网络 id；为 {@code null} 表示尚未完成集成（integrate）。
     */
    UUID getNetworkId();

    /**
     * 设置节点所属网络 id。
     *
     * <p>对方块节点而言，该值会被规范化为网络根 id，并写入持久化数据。</p>
     */
    void setNetworkId(UUID networkId);

    /**
     * 节点所在的世界（服务端）。
     */
    Level getNodeLevel();

    /**
     * 节点所在的方块坐标。
     */
    BlockPos getNodePos();

    /**
     * 节点“注册 id”（物品/方块 id 的字符串形式）。
     *
     * <p>用于在调试/展示层识别节点类型；与网络 id（UUID）是两套概念。</p>
     */
    String getNodeRegistryId();
}
