package org.creepebucket.programmable_magic.mananet.api;

import org.creepebucket.programmable_magic.ModUtils.Mana;

import java.util.UUID;

/**
 * 节点方块（非方块实体）在 Mananet 中的持久化/运行时状态。
 *
 * <p>该状态会：</p>
 * <ul>
 *     <li>在运行时由 {@code MananetNetworkManager} 缓存（key 为 {@code BlockPos.asLong()}）。</li>
 *     <li>在磁盘上随 chunk 附件保存（见 {@code MananetChunkNodes}）。</li>
 * </ul>
 *
 * <p>它只是一份“数据快照”，不负责网络计算；网络汇总由管理器维护，网络合并/拆分由逻辑层驱动。</p>
 */
public class MananetNodeState {

    /**
     * 当前所属网络 id（根 id）。
     *
     * <p>为 {@code null} 表示尚未集成到任何网络；在集成时会被分配 UUID。</p>
     */
    public UUID networkId;
    /**
     * 本节点提供的缓存上限贡献（容量）。
     */
    public Mana cache = new Mana();
    /**
     * 本节点声明的每秒净负载：正数消耗，负数产出。
     */
    public Mana load = new Mana();
    /**
     * 6 个方向的连通掩码，位序与 {@code Direction.ordinal()} 一致。
     *
     * <p>默认全开（{@code 0b111111}）。</p>
     */
    public int connectivityMask = 0b111111;
}
