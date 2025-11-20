package org.creepebucket.programmable_magic.mana.simple;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.creepebucket.programmable_magic.mana.api.IManaNetNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 极简单层网络管理器（KISS）。
 *
 * 设计要点：
 * - 无 L0/L1/L2 与全局图，仅按需对变化附近做“就地染色（Flood Fill）”。
 * - 网络 ID 由分量的“最小坐标”确定性编码，合并/拆分稳定且无需全局自增计数器。
 * - 分桶：后续将把一个 16x16x16 Section 切成 8 个 8x8x8 子桶，并以位集加速桶内 flood fill。
 *   本类先给出可工作的朴素版本，便于后续替换为位集实现（不改对外接口）。
 */
public final class SimpleNetManager {

    // 每个维度一个实例，避免跨维度引用泄漏
    private static final Map<ServerLevel, SimpleNetManager> INSTANCES = new HashMap<>();

    public static SimpleNetManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, SimpleNetManager::new);
    }

    private final ServerLevel level;
    // 合并同一tick的多次拓扑变更，避免重复刷
    private final it.unimi.dsi.fastutil.longs.LongOpenHashSet pending = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
    private boolean scheduled = false;

    private SimpleNetManager(ServerLevel level) {
        this.level = level;
    }

    /**
     * 拓扑变化统一入口：在 pos 及其 6 向邻居周围做一次就地染色。
     * KISS：朴素 BFS 收集连通块 → 计算分量最小坐标 → 得到确定性 netId → 批量写回。
     */
    public void onTopologyChanged(BlockPos pos) {
        synchronized (this) {
            pending.add(pos.asLong());
            for (Direction d : Direction.values()) pending.add(pos.relative(d).asLong());
            if (!scheduled) {
                scheduled = true;
                // 合并到本tick尾部处理，避免重复刷
                level.getServer().execute(this::processPending);
            }
        }
    }

    private void processPending() {
        it.unimi.dsi.fastutil.longs.LongOpenHashSet toProcess;
        synchronized (this) {
            toProcess = new it.unimi.dsi.fastutil.longs.LongOpenHashSet(pending);
            pending.clear();
            scheduled = false;
        }
        // 防重复遍历
        Set<BlockPos> visited = new HashSet<>();
        for (it.unimi.dsi.fastutil.longs.LongIterator it = toProcess.iterator(); it.hasNext(); ) {
            long l = it.nextLong();
            BlockPos seed = BlockPos.of(l);
            if (!isCable(seed) || visited.contains(seed)) continue;
            Component comp = collectComponent(seed, visited);
            if (comp.isEmpty()) continue;
            long netId = encodeNetId(comp.minPos);
            for (BlockPos p : comp.members) applyNetId(p, netId);
        }
    }

    /**
     * 朴素 BFS 收集一个连通分量，并把收集到的节点加入 visited。
     */
    private Component collectComponent(BlockPos start, Set<BlockPos> visited) {
        // 全局已访问（按 long）避免跨桶重复
        it.unimi.dsi.fastutil.longs.LongOpenHashSet visitedGlobal = new it.unimi.dsi.fastutil.longs.LongOpenHashSet(1024);
        for (BlockPos v : visited) visitedGlobal.add(v.asLong());

        ArrayDeque<long[]> bucketsToProcess = new ArrayDeque<>();
        // 入队：起点所在桶与其局部索引
        bucketsToProcess.add(bucketEntryFor(start));

        List<BlockPos> members = new ArrayList<>();
        BlockPos minPos = start;

        while (!bucketsToProcess.isEmpty()) {
            long[] entry = bucketsToProcess.pollFirst();
            int baseX = (int) entry[0];
            int baseY = (int) entry[1];
            int baseZ = (int) entry[2];
            int startIdx = (int) entry[3]; // [0,511]

            // 桶内 flood fill（基于数组/位集），并将跨桶邻居入队
            fillSingleBucket(baseX, baseY, baseZ, startIdx, visitedGlobal, members, bucketsToProcess);
        }

        // 更新外层 visited（BlockPos 集合仅用于后续 applyNetId 循环外的去重）
        for (BlockPos p : members) visited.add(p);
        // 计算 minPos
        for (BlockPos p : members) if (comparePos(p, minPos) < 0) minPos = p;
        return new Component(new HashSet<>(members), minPos);
    }

    private static long[] bucketEntryFor(BlockPos pos) {
        int bx = (pos.getX() >> 3) << 3; // 向下取 8 的倍数
        int by = (pos.getY() >> 3) << 3;
        int bz = (pos.getZ() >> 3) << 3;
        int lx = pos.getX() & 7;
        int ly = pos.getY() & 7;
        int lz = pos.getZ() & 7;
        int idx = (lx << 6) | (ly << 3) | lz;
        return new long[]{bx, by, bz, idx};
    }

    /**
     * 在一个 8x8x8 子桶内做堆栈式 flood fill；
     * - 仅当相邻单元两端都能互连时才扩张；
     * - 越界到相邻子桶的邻居，入队到 bucketsToProcess；
     * - 所有加入 members 的世界坐标都会同时写入 visitedGlobal。
     */
    private void fillSingleBucket(int baseX, int baseY, int baseZ, int startIdx,
                                  it.unimi.dsi.fastutil.longs.LongOpenHashSet visitedGlobal,
                                  List<BlockPos> members,
                                  ArrayDeque<long[]> bucketsToProcess) {
        // 若起点不是线缆，直接返回
        int sx = baseX + ((startIdx >>> 6) & 7);
        int sy = baseY + ((startIdx >>> 3) & 7);
        int sz = baseZ + (startIdx & 7);
        BlockPos startPos = new BlockPos(sx, sy, sz);
        if (!isCable(startPos)) return;

        boolean[] visitedLocal = new boolean[512]; // 已处理
        boolean[] pushedLocal = new boolean[512];  // 已入栈
        int[] stack = new int[512];               // 栈容量等于桶内最大节点数
        int sp = 0;
        pushedLocal[startIdx] = true;
        stack[sp++] = startIdx;

        while (sp > 0) {
            int idx = stack[--sp];
            if (visitedLocal[idx]) continue;
            int lx = (idx >>> 6) & 7;
            int ly = (idx >>> 3) & 7;
            int lz = idx & 7;
            int wx = baseX + lx;
            int wy = baseY + ly;
            int wz = baseZ + lz;
            long wlong = BlockPos.asLong(wx, wy, wz);
            if (visitedGlobal.contains(wlong)) { visitedLocal[idx] = true; continue; }
            BlockPos cur = new BlockPos(wx, wy, wz);
            if (!isCable(cur)) { visitedLocal[idx] = true; continue; }

            visitedLocal[idx] = true;
            visitedGlobal.add(wlong);
            members.add(cur);

            // 六向尝试扩张
            for (Direction d : Direction.values()) {
                int nwx = wx + d.getStepX();
                int nwy = wy + d.getStepY();
                int nwz = wz + d.getStepZ();
                BlockPos nb = new BlockPos(nwx, nwy, nwz);
                if (!canConnectBothWays(cur, nb, d)) continue;

                int nlx = lx + d.getStepX();
                int nly = ly + d.getStepY();
                int nlz = lz + d.getStepZ();
                if ((nlx | nly | nlz) >= 0 && nlx < 8 && nly < 8 && nlz < 8) {
                    int nidx = (nlx << 6) | (nly << 3) | nlz;
                    if (!visitedLocal[nidx] && !pushedLocal[nidx]) {
                        pushedLocal[nidx] = true;
                        if (sp < stack.length) stack[sp++] = nidx;
                    }
                } else {
                    // 跨桶：入队相邻子桶
                    int nbx = (nwx >> 3) << 3;
                    int nby = (nwy >> 3) << 3;
                    int nbz = (nwz >> 3) << 3;
                    int nlxi = nwx & 7;
                    int nlyi = nwy & 7;
                    int nlzi = nwz & 7;
                    int nidx2 = (nlxi << 6) | (nlyi << 3) | nlzi;
                    long nlong = BlockPos.asLong(nwx, nwy, nwz);
                    if (!visitedGlobal.contains(nlong)) {
                        bucketsToProcess.add(new long[]{nbx, nby, nbz, nidx2});
                    }
                }
            }
        }
    }

    private static final class Component {
        final Set<BlockPos> members;
        final BlockPos minPos;
        Component(Set<BlockPos> members, BlockPos minPos) {this.members = members; this.minPos = minPos;}
        boolean isEmpty() {return members.isEmpty();}
    }

    /**
     * 判断该位置是否是“可参与网络”的线缆。
     */
    private boolean isCable(BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IManaNetNode node)) return false;
        return node.isManaConnectable();
    }

    /**
     * 互相同意的连通性：两端都是线缆且各自声明对对方方向可连。
     */
    private boolean canConnectBothWays(BlockPos a, BlockPos b, Direction dirAB) {
        BlockEntity beA = level.getBlockEntity(a);
        BlockEntity beB = level.getBlockEntity(b);
        if (!(beA instanceof IManaNetNode na) || !(beB instanceof IManaNetNode nb)) return false;
        if (!na.isManaConnectable() || !nb.isManaConnectable()) return false;
        return na.canConnectTo(dirAB) && nb.canConnectTo(dirAB.getOpposite());
    }

    /**
     * 应用 netId 到世界中的方块实体。
     *
     * 说明：直接调用 ManaCableBlockEntity#setSimpleNetId(long)，不做旧代码兼容。
     */
    private void applyNetId(BlockPos pos, long netId) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof org.creepebucket.programmable_magic.blockentity.ManaCableBlockEntity cable) {
            cable.setSimpleNetId(netId);
        }
    }

    /**
     * 词典序比较：x, y, z 依次升序。
     */
    private static int comparePos(BlockPos a, BlockPos b) {
        if (a.getX() != b.getX()) return Integer.compare(a.getX(), b.getX());
        if (a.getY() != b.getY()) return Integer.compare(a.getY(), b.getY());
        return Integer.compare(a.getZ(), b.getZ());
    }

    /**
     * 把坐标编码为 64 位无符号 netId（21+21+21 位，支持坐标范围 [-2^20, 2^20)）。
     */
    public static long encodeNetId(BlockPos pos) {
        long ux = toUnsigned21(pos.getX());
        long uy = toUnsigned21(pos.getY());
        long uz = toUnsigned21(pos.getZ());
        return (ux << 42) | (uy << 21) | uz;
    }

    private static long toUnsigned21(int v) {
        // 偏移到 [0, 2^21) 区间
        long shifted = (long) v + (1L << 20);
        // 截断到 21 位
        return shifted & ((1L << 21) - 1);
    }
}
