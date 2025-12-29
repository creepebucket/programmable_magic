package org.creepebucket.programmable_magic.mananet.simple;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.creepebucket.programmable_magic.mananet.AbstractNetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.ManaNetService;
import org.creepebucket.programmable_magic.mananet.api.IManaNetNode;

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
 * 思想：任何拓扑变化（放置/拆除/邻居变化）都只在变化附近做一次“就地染色”，
 * 用 BFS/Flood Fill 收集该连通分量的所有线缆，然后把“分量最小坐标编码”作为网络 ID
 * 写回每个成员。
 *
 * 不做的事：
 * - 不维护全局图/多层网络（L0/L1/L2）；
 * - 不在世界保存/卸载阶段重建网络；
 * - 不把网络 ID 的每次变化持久化（只在 0->非0 首次落盘）。
 *
 * 关键实现点：
 * - 8x8x8 子桶 flood fill：把世界划分为 8 对齐的子块，桶内用位集记录已访问/已入栈，
 *   常数小、缓存友好；跨桶时把相邻桶入队继续处理。
 * - 本 tick 合并：把本 tick 多个变化点合并到一个任务末尾执行，避免重复刷。
 * - 网络 ID 稳定：采用“分量词典序最小坐标”的 64 位编码（21+21+21），合并/拆分稳定。
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
     * 拓扑变化统一入口：积累变化位置，并在本 tick 尾部批处理一次。
     *
     * 为什么不立即做：
     * - updateShape/邻居变化可能在同一 tick 高频触发（例如 /fill 放置）；
     * - 合并后只做一次 flood fill，可大幅减少重复工作。
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

    /**
     * 本 tick 尾部处理所有积累的种子位置：
     * - 为每个种子做一次连通分量收集（已访问去重，避免交叉分量重复）
     * - 计算网络 ID（最小坐标编码），并回写每个成员。
     */
    private void processPending() {
        it.unimi.dsi.fastutil.longs.LongOpenHashSet toProcess;
        synchronized (this) {
            toProcess = new it.unimi.dsi.fastutil.longs.LongOpenHashSet(pending);
            pending.clear();
            scheduled = false;
        }
        // 防重复遍历
        Set<BlockPos> visited = new HashSet<>();
        List<ComponentUpdate> updates = new ArrayList<>();
        for (it.unimi.dsi.fastutil.longs.LongIterator it = toProcess.iterator(); it.hasNext(); ) {
            long l = it.nextLong();
            BlockPos seed = BlockPos.of(l);
            if (!isCable(seed) || visited.contains(seed)) continue;
            Component comp = collectComponent(seed, visited);
            if (comp.isEmpty()) continue;
            long oldNetId = readNetId(seed);
            long netId = hashNetId(encodeNetId(comp.minPos));
            updates.add(new ComponentUpdate(oldNetId, netId, comp.members));
        }

        for (ComponentUpdate u : updates) {
            for (BlockPos p : u.members) applyNetId(p, u.newNetId);
        }

        Map<Long, Map<Long, it.unimi.dsi.fastutil.longs.LongOpenHashSet>> byOld = new HashMap<>();
        for (ComponentUpdate u : updates) {
            if (u.oldNetId == 0L) continue;
            Map<Long, it.unimi.dsi.fastutil.longs.LongOpenHashSet> byNew = byOld.computeIfAbsent(u.oldNetId, k -> new HashMap<>());
            it.unimi.dsi.fastutil.longs.LongOpenHashSet keys = byNew.computeIfAbsent(u.newNetId, k -> new it.unimi.dsi.fastutil.longs.LongOpenHashSet());
            for (BlockPos p : u.members) keys.add(p.asLong());
        }

        ManaNetService svc = ManaNetService.get(level);
        for (Map.Entry<Long, Map<Long, it.unimi.dsi.fastutil.longs.LongOpenHashSet>> e : byOld.entrySet()) {
            if (e.getValue().size() == 1) {
                long newNetId = e.getValue().keySet().iterator().next();
                svc.moveStored(e.getKey(), newNetId);
                continue;
            }
            svc.redistributeStoredByCacheOnSplit(e.getKey(), e.getValue());
        }
    }

    /**
     * 收集“以 start 为种子”的连通分量：
     * - visited 用于全局去重（BlockPos 层面），避免处理到相邻分量时重复遍历；
     * - visitedGlobal 用于跨桶去重（long 层面），避免在子桶之间反复入队；
     * - 桶内 flood fill 使用位集表示已访问/已入栈，常数低；
     * - 碰到跨桶邻居时，把相邻子桶的基坐标与起点局部索引入队。
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

    /**
     * 计算“pos 所在子桶”的基坐标（8 对齐）与其在子桶内的局部线性索引 [0, 511]。
     */
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
     * 在一个 8x8x8 子桶内做“堆栈式 flood fill”。
     * 约定：
     * - 仅当相邻单元“两端都能互连”时才扩张（连通性由 IManaNetNode.canConnectTo 决定）；
     * - 越界到相邻子桶的邻居，打包为条目入队，稍后独立处理；
     * - visitedBits/pushedBits 使用位集记录桶内访问状态，避免 boolean[512] 带来的缓存 miss；
     * - 每个被纳入 members 的世界坐标，也会同步写入 visitedGlobal 用于跨桶去重。
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

        long[] visitedBits = new long[8]; // x 层各 64 位：已处理
        long[] pushedBits = new long[8];  // x 层各 64 位：已入栈
        int[] stack = new int[512];       // 栈容量等于桶内最大节点数
        int sp = 0;
        setPushed(pushedBits, startIdx);
        stack[sp++] = startIdx;

        while (sp > 0) {
            int idx = stack[--sp];
            // 已处理则跳过
            if (isVisited(visitedBits, idx)) continue;
            int lx = (idx >>> 6) & 7;
            int ly = (idx >>> 3) & 7;
            int lz = idx & 7;
            int wx = baseX + lx;
            int wy = baseY + ly;
            int wz = baseZ + lz;
            long wlong = BlockPos.asLong(wx, wy, wz);
            // 跨桶去重：相邻子桶已处理过当前位置，直接标记并跳过
            if (visitedGlobal.contains(wlong)) { setVisited(visitedBits, idx); continue; }
            BlockPos cur = new BlockPos(wx, wy, wz);
            // 当前格不是线缆，标记并跳过
            if (!isCable(cur)) { setVisited(visitedBits, idx); continue; }

            setVisited(visitedBits, idx);
            visitedGlobal.add(wlong);
            members.add(cur);

            // 六向尝试扩张（桶内优先，跨桶入队）
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
                    if (!isVisited(visitedBits, nidx) && !isPushed(pushedBits, nidx)) {
                        setPushed(pushedBits, nidx);
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

    /** 桶内：查询是否“已处理” */
    private static boolean isVisited(long[] visitedBits, int idx) {
        int lx = (idx >>> 6) & 7;
        int bit = idx & 63;
        return ((visitedBits[lx] >>> bit) & 1L) != 0L;
    }

    /** 桶内：标记为“已处理” */
    private static void setVisited(long[] visitedBits, int idx) {
        int lx = (idx >>> 6) & 7;
        int bit = idx & 63;
        visitedBits[lx] |= (1L << bit);
    }

    /** 桶内：查询是否“已入栈待处理” */
    private static boolean isPushed(long[] pushedBits, int idx) {
        int lx = (idx >>> 6) & 7;
        int bit = idx & 63;
        return ((pushedBits[lx] >>> bit) & 1L) != 0L;
    }

    /** 桶内：标记为“已入栈待处理” */
    private static void setPushed(long[] pushedBits, int idx) {
        int lx = (idx >>> 6) & 7;
        int bit = idx & 63;
        pushedBits[lx] |= (1L << bit);
    }

    private static final class Component {
        final Set<BlockPos> members;
        final BlockPos minPos;
        Component(Set<BlockPos> members, BlockPos minPos) {this.members = members; this.minPos = minPos;}
        boolean isEmpty() {return members.isEmpty();}
    }

    private static final class ComponentUpdate {
        final long oldNetId;
        final long newNetId;
        final Set<BlockPos> members;

        ComponentUpdate(long oldNetId, long newNetId, Set<BlockPos> members) {
            this.oldNetId = oldNetId;
            this.newNetId = newNetId;
            this.members = members;
        }
    }

    private long readNetId(BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AbstractNetNodeBlockEntity node) return node.getSimpleNetId();
        return 0L;
    }

    /** 判断该位置是否是“可参与网络”的线缆。 */
    private boolean isCable(BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IManaNetNode node)) return false;
        return node.isManaConnectable();
    }

    /** 互相同意的连通性：两端都是线缆且各自声明对对方方向可连。 */
    private boolean canConnectBothWays(BlockPos a, BlockPos b, Direction dirAB) {
        BlockEntity beA = level.getBlockEntity(a);
        BlockEntity beB = level.getBlockEntity(b);
        if (!(beA instanceof IManaNetNode na) || !(beB instanceof IManaNetNode nb)) return false;
        if (!na.isManaConnectable() || !nb.isManaConnectable()) return false;
        return na.canConnectTo(dirAB) && nb.canConnectTo(dirAB.getOpposite());
    }

    /**
     * 应用 netId 到世界中的方块实体。
     */
    private void applyNetId(BlockPos pos, long netId) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AbstractNetNodeBlockEntity node) {
            node.setSimpleNetId(netId);
        }
    }

    /** 词典序比较：x, y, z 依次升序。 */
    private static int comparePos(BlockPos a, BlockPos b) {
        if (a.getX() != b.getX()) return Integer.compare(a.getX(), b.getX());
        if (a.getY() != b.getY()) return Integer.compare(a.getY(), b.getY());
        return Integer.compare(a.getZ(), b.getZ());
    }

    /** 把坐标编码为 64 位无符号 netId（21+21+21 位，支持坐标范围 [-2^20, 2^20)）。 */
    public static long encodeNetId(BlockPos pos) {
        long ux = toUnsigned21(pos.getX());
        long uy = toUnsigned21(pos.getY());
        long uz = toUnsigned21(pos.getZ());
        return (ux << 42) | (uy << 21) | uz;
    }

    private static long hashNetId(long v) {
        long z = v + 0x9e3779b97f4a7c15L;
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    private static long toUnsigned21(int v) {
        // 偏移到 [0, 2^21) 区间
        long shifted = (long) v + (1L << 20);
        // 截断到 21 位
        return shifted & ((1L << 21) - 1);
    }
}
