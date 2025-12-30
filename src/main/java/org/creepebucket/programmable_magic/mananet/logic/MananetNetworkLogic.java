package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.ManaMath;
import org.creepebucket.programmable_magic.mananet.api.MananetNode;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MananetNetworkLogic {

    private MananetNetworkLogic() {}

    public static MananetNode getNodeAccess(ServerLevel level, BlockPos pos) {
        return getNodeOrBlockNode(level, pos);
    }

    public static void markDirty(ServerLevel level, BlockPos pos) {
        MananetNetworkManager.get(level).markDirty(pos);
    }

    public static void enqueueConnectivityChange(ServerLevel level, BlockPos pos, Direction dir, boolean oldValue, boolean newValue) {
        MananetNetworkManager.get(level).enqueueEdgeChange(pos, dir, oldValue, newValue);
    }

    public static void enqueueRemoval(ServerLevel level, BlockPos pos, UUID oldNetworkId, int connectivityMask) {
        MananetNetworkManager.get(level).enqueueRemoval(pos, oldNetworkId, connectivityMask, new Mana(), new Mana(), 0);
    }

    public static void enqueueBlockRemoval(ServerLevel level, BlockPos pos, AbstractNodeBlock block) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        MananetNodeState state = manager.getBlockNode(pos);
        MananetNetworkPersistence.removeNode(level, pos);
        manager.removeBlockNode(pos);
        if (state == null || state.networkId == null) return;
        UUID root = manager.resolveNetworkId(state.networkId);
        int connected = 0;
        for (Direction dir : Direction.values()) {
            int bit = 1 << dir.ordinal();
            if ((state.connectivityMask & bit) == 0) continue;
            BlockPos np = pos.relative(dir);
            MananetNodeState ns = getOrCreateNodeState(level, np);
            if (ns == null || ns.networkId == null) continue;
            if (!manager.resolveNetworkId(ns.networkId).equals(root)) continue;
            int obit = 1 << dir.getOpposite().ordinal();
            if ((ns.connectivityMask & obit) == 0) continue;
            connected++;
        }
        manager.enqueueRemoval(pos, root, state.connectivityMask, state.cache, state.load, connected);
    }

    public static void processPending(ServerLevel level) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);

        java.util.HashMap<UUID, java.util.ArrayList<MananetNetworkManager.NodeRemoval>> removalGroups = new java.util.HashMap<>();
        while (true) {
            MananetNetworkManager.NodeRemoval removal = manager.pollRemoval();
            if (removal == null) break;
            UUID root = manager.resolveNetworkId(removal.oldNetworkId());
            removalGroups.computeIfAbsent(root, ignored -> new java.util.ArrayList<>()).add(removal);
        }
        for (var entry : removalGroups.entrySet()) processRemovalGroup(level, entry.getKey(), entry.getValue());

        while (true) {
            MananetNetworkManager.EdgeChange change = manager.pollEdgeChange();
            if (change == null) break;
            onNodeConnectivityChanged(level, BlockPos.of(change.posLong()), change.dir(), change.oldValue(), change.newValue());
        }

        while (true) {
            Long posLong = manager.pollDirty();
            if (posLong == null) break;
            integrateIfNeeded(level, BlockPos.of(posLong));
        }
    }

    public static void onNodeAdded(ServerLevel level, BlockPos pos) {
        markDirty(level, pos);
    }

    private static void processRemovalGroup(ServerLevel level, UUID oldRoot, java.util.ArrayList<MananetNetworkManager.NodeRemoval> removals) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        MananetNetworkManager.NetworkState oldNetwork = manager.getOrCreate(oldRoot);
        Mana oldMana = ManaMath.copy(oldNetwork.mana);

        if (removals.size() >= oldNetwork.size) {
            manager.setNetwork(oldRoot, new Mana(), new Mana(), new Mana(), 0);
            return;
        }

        boolean needsSplit = false;
        for (MananetNetworkManager.NodeRemoval removal : removals) {
            if (removal.connectedNeighbors() > 1) { needsSplit = true; break; }
        }
        if (!needsSplit) {
            for (MananetNetworkManager.NodeRemoval removal : removals) {
                manager.applyContribution(oldRoot, removal.cache().negative(), removal.load().negative(), -1);
            }
            return;
        }

        LongOpenHashSet excluded = new LongOpenHashSet();
        LongArrayList seeds = new LongArrayList();
        LongOpenHashSet seedSet = new LongOpenHashSet();

        for (MananetNetworkManager.NodeRemoval removal : removals) {
            excluded.add(removal.posLong());
        }

        for (MananetNetworkManager.NodeRemoval removal : removals) {
            BlockPos removedPos = BlockPos.of(removal.posLong());
            for (Direction dir : Direction.values()) {
                int bit = 1 << dir.ordinal();
                if ((removal.connectivityMask() & bit) == 0) continue;
                BlockPos np = removedPos.relative(dir);
                MananetNodeState ns = manager.getBlockNode(np);
                if (ns == null) continue;
                if (ns.networkId == null) continue;
                if (!manager.resolveNetworkId(ns.networkId).equals(oldRoot)) continue;
                int obit = 1 << dir.getOpposite().ordinal();
                if ((ns.connectivityMask & obit) == 0) continue;
                long key = np.asLong();
                if (excluded.contains(key)) continue;
                if (seedSet.add(key)) seeds.add(key);
            }
        }

        LongOpenHashSet visited = new LongOpenHashSet();
        java.util.ArrayList<Component> components = new java.util.ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            long seed = seeds.getLong(i);
            if (excluded.contains(seed)) continue;
            if (visited.contains(seed)) continue;
            Component component = collectComponentByState(level, oldRoot, seed, excluded, visited, 0L, null);
            if (component.size > 0) components.add(component);
        }

        if (components.isEmpty()) {
            manager.setNetwork(oldRoot, new Mana(), new Mana(), new Mana(), 0);
            return;
        }

        if (components.size() == 1) {
            Component c = components.get(0);
            LongArrayList positions = c.positions;
            for (int i = 0; i < positions.size(); i++) {
                long posLong = positions.getLong(i);
                MananetNodeState ns = manager.getBlockNode(posLong);
                if (ns != null) ns.networkId = oldRoot;
            }
            MananetNetworkPersistence.updateNetworkIdBulk(level, positions, oldRoot);
            manager.setNetwork(oldRoot, oldMana, c.cache, c.load, c.size);
            return;
        }

        Component a = components.get(0);
        Mana cacheB = new Mana();
        Mana loadB = new Mana();
        int sizeB = 0;
        LongArrayList positionsB = new LongArrayList();
        for (int i = 1; i < components.size(); i++) {
            Component c = components.get(i);
            cacheB.add(c.cache);
            loadB.add(c.load);
            sizeB += c.size;
            positionsB.addAll(c.positions);
        }

        UUID idB = UUID.randomUUID();

        for (int i = 0; i < a.positions.size(); i++) {
            long posLong = a.positions.getLong(i);
            MananetNodeState ns = manager.getBlockNode(posLong);
            if (ns != null) ns.networkId = oldRoot;
        }
        for (int i = 0; i < positionsB.size(); i++) {
            long posLong = positionsB.getLong(i);
            MananetNodeState ns = manager.getBlockNode(posLong);
            if (ns != null) ns.networkId = idB;
        }
        MananetNetworkPersistence.updateNetworkIdBulk(level, a.positions, oldRoot);
        MananetNetworkPersistence.updateNetworkIdBulk(level, positionsB, idB);

        Mana totalCache = ManaMath.copy(a.cache);
        totalCache.add(cacheB);
        Mana clamped = ManaMath.clampToCache(ManaMath.clampNonNegative(oldMana), totalCache);
        ManaSplit split = splitManaByCache(clamped, a.cache, cacheB);

        manager.setNetwork(oldRoot, split.manaA, a.cache, a.load, a.size);
        manager.setNetwork(idB, split.manaB, cacheB, loadB, sizeB);
    }

    public static void integrateIfNeeded(ServerLevel level, BlockPos pos) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        BlockState selfState = level.getBlockState(pos);
        if (selfState.hasBlockEntity()) return;
        if (!(selfState.getBlock() instanceof AbstractNodeBlock selfBlock)) return;

        MananetNodeState state = manager.getOrCreateBlockNode(pos, () -> {
            MananetNodeState node_state = new MananetNodeState();
            selfBlock.init_node_state(level, pos, selfState, node_state);
            return node_state;
        });
        if (state.networkId != null) return;

        UUID[] neighborRoots = new UUID[6];
        int neighborRootCount = 0;
        UUID chosen = null;
        int bestSize = -1;

        for (Direction dir : Direction.values()) {
            int bit = 1 << dir.ordinal();
            if ((state.connectivityMask & bit) == 0) continue;

            BlockPos np = pos.relative(dir);
            BlockState ns = level.getBlockState(np);
            if (ns.hasBlockEntity()) continue;
            if (!(ns.getBlock() instanceof AbstractNodeBlock nb)) continue;

            MananetNodeState neighborState = manager.getOrCreateBlockNode(np, () -> {
                MananetNodeState node_state = new MananetNodeState();
                nb.init_node_state(level, np, ns, node_state);
                return node_state;
            });
            int obit = 1 << dir.getOpposite().ordinal();
            if ((neighborState.connectivityMask & obit) == 0) continue;

            if (neighborState.networkId == null) continue;

            UUID root = manager.resolveNetworkId(neighborState.networkId);
            boolean exists = false;
            for (int i = 0; i < neighborRootCount; i++) {
                if (neighborRoots[i].equals(root)) { exists = true; break; }
            }
            if (exists) continue;
            neighborRoots[neighborRootCount++] = root;

            int size = manager.getSize(root);
            if (size > bestSize) {
                chosen = root;
                bestSize = size;
            }
        }

        if (chosen == null) {
            chosen = UUID.randomUUID();
            manager.setNetwork(chosen, new Mana(), new Mana(), new Mana(), 0);
        }

        for (int i = 0; i < neighborRootCount; i++) chosen = manager.union(chosen, neighborRoots[i]);
        chosen = manager.resolveNetworkId(chosen);

        state.networkId = chosen;
        manager.applyContribution(chosen, state.cache, state.load, 1);
        MananetNetworkPersistence.upsertNode(level, pos, state);
    }

    public static void onNodeGraphUpdated(ServerLevel level, BlockPos pos) {
        markDirty(level, pos);
    }

    public static void onNodeContributionChanged(ServerLevel level, BlockPos pos) {
        markDirty(level, pos);
    }

    public static void onNodeConnectivityChanged(ServerLevel level, BlockPos pos, Direction dir, boolean oldValue, boolean newValue) {
        BlockPos np = pos.relative(dir);
        MananetNode a = getNodeOrBlockNode(level, pos);
        MananetNode b = getNodeOrBlockNode(level, np);
        if (a == null || b == null) return;

        boolean wasConnected = oldValue && b.getConnectivity(dir.getOpposite());
        boolean isConnected = newValue && b.getConnectivity(dir.getOpposite());

        if (!wasConnected && isConnected) {
            integrateIfNeeded(level, pos);
            integrateIfNeeded(level, np);
            MananetNodeState sa = MananetNetworkManager.get(level).getBlockNode(pos);
            MananetNodeState sb = MananetNetworkManager.get(level).getBlockNode(np);
            if (sa != null && sb != null && sa.networkId != null && sb.networkId != null) {
                MananetNetworkManager.get(level).union(sa.networkId, sb.networkId);
            }
            return;
        }

        if (wasConnected && !isConnected) splitByRemovedEdge(level, pos, np, dir);
    }

    private static void splitByRemovedEdge(ServerLevel level, BlockPos aPos, BlockPos bPos, Direction dir) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        MananetNodeState aState = getOrCreateNodeState(level, aPos);
        MananetNodeState bState = getOrCreateNodeState(level, bPos);
        if (aState == null || bState == null) return;
        if (aState.networkId == null || bState.networkId == null) return;

        UUID oldId = manager.resolveNetworkId(aState.networkId);
        if (!oldId.equals(manager.resolveNetworkId(bState.networkId))) return;

        MananetNetworkManager.NetworkState oldNetwork = manager.getOrCreate(oldId);
        Mana oldMana = ManaMath.copy(oldNetwork.mana);

        LongOpenHashSet excluded = new LongOpenHashSet();
        LongOpenHashSet visited = new LongOpenHashSet();
        long aPosLong = aPos.asLong();
        long bPosLong = bPos.asLong();
        Component compA = collectComponentByState(level, oldId, aPosLong, excluded, visited, aPosLong, dir);
        if (visited.contains(bPosLong)) return;

        UUID idB = UUID.randomUUID();
        for (int i = 0; i < compA.positions.size(); i++) {
            long posLong = compA.positions.getLong(i);
            MananetNodeState ns = manager.getBlockNode(posLong);
            if (ns != null) ns.networkId = oldId;
        }

        Mana cacheB = new Mana();
        Mana loadB = new Mana();
        int sizeB = 0;
        LongArrayList positionsB = new LongArrayList();
        for (var entry : manager.iterateBlockNodes()) {
            long posLong = entry.getLongKey();
            MananetNodeState ns = entry.getValue();
            if (ns.networkId == null) continue;
            if (!manager.resolveNetworkId(ns.networkId).equals(oldId)) continue;
            if (visited.contains(posLong)) continue;
            ns.networkId = idB;
            cacheB.add(ns.cache);
            loadB.add(ns.load);
            sizeB++;
            positionsB.add(posLong);
        }
        MananetNetworkPersistence.updateNetworkIdBulk(level, compA.positions, oldId);
        MananetNetworkPersistence.updateNetworkIdBulk(level, positionsB, idB);

        Mana totalCache = ManaMath.copy(compA.cache);
        totalCache.add(cacheB);
        Mana clamped = ManaMath.clampToCache(ManaMath.clampNonNegative(oldMana), totalCache);
        ManaSplit split = splitManaByCache(clamped, compA.cache, cacheB);

        manager.setNetwork(oldId, split.manaA, compA.cache, compA.load, compA.size);
        manager.setNetwork(idB, split.manaB, cacheB, loadB, sizeB);
    }

    private record ManaSplit(Mana manaA, Mana manaB) {}

    private static ManaSplit splitManaByCache(Mana mana, Mana cacheA, Mana cacheB) {
        double denomR = cacheA.getRadiation() + cacheB.getRadiation();
        double denomT = cacheA.getTemperature() + cacheB.getTemperature();
        double denomM = cacheA.getMomentum() + cacheB.getMomentum();
        double denomP = cacheA.getPressure() + cacheB.getPressure();

        double rA = mana.getRadiation() == 0.0 ? 0.0 : mana.getRadiation() * (cacheA.getRadiation() / denomR);
        double tA = mana.getTemperature() == 0.0 ? 0.0 : mana.getTemperature() * (cacheA.getTemperature() / denomT);
        double mA = mana.getMomentum() == 0.0 ? 0.0 : mana.getMomentum() * (cacheA.getMomentum() / denomM);
        double pA = mana.getPressure() == 0.0 ? 0.0 : mana.getPressure() * (cacheA.getPressure() / denomP);

        Mana manaA = new Mana(rA, tA, mA, pA);
        Mana manaB = ManaMath.copy(mana);
        manaB.add(manaA.negative());
        return new ManaSplit(manaA, manaB);
    }

    private static RebuildResult assignComponentId(ServerLevel level, BlockPos start, UUID targetId, BlockPos excludedPos, BlockPos edgeA, Direction edgeDir) {
        Set<Long> visited = new HashSet<>();
        RebuildResult component = collectComponent(level, start, visited, excludedPos, edgeA, edgeDir);
        for (BlockPos pos : component.positions) {
            MananetNode node = getNodeOrBlockNode(level, pos);
            if (node != null) node.setNetworkId(targetId);
        }
        return new RebuildResult(start, component.positions, component.oldNetworkIds, component.cache, component.load, component.size);
    }

    private static RebuildResult collectComponent(ServerLevel level, BlockPos start, Set<Long> visited, BlockPos excludedPos, BlockPos edgeA, Direction edgeDir) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);

        Set<BlockPos> positions = new HashSet<>();
        Set<UUID> oldIds = new HashSet<>();
        Mana cache = new Mana();
        Mana load = new Mana();
        int size = 0;

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (excludedPos != null && pos.equals(excludedPos)) continue;
            long key = pos.asLong();
            if (!visited.add(key)) continue;

            MananetNode node = getNodeOrBlockNode(level, pos);
            if (node == null) continue;

            positions.add(pos);
            size++;
            cache.add(node.getCache());
            load.add(node.getLoad());
            if (node.getNetworkId() != null) oldIds.add(node.getNetworkId());

            for (Direction dir : Direction.values()) {
                BlockPos np = pos.relative(dir);
                if (excludedPos != null && np.equals(excludedPos)) continue;
                if (edgeA != null && edgeDir != null && isForbiddenEdge(pos, dir, edgeA, edgeDir)) continue;

                MananetNode neighbor = getNodeOrBlockNode(level, np);
                if (neighbor == null) continue;
                if (!node.getConnectivity(dir)) continue;
                if (!neighbor.getConnectivity(dir.getOpposite())) continue;
                queue.addLast(np);
            }
        }

        return new RebuildResult(start, positions, oldIds, cache, load, size);
    }

    private static boolean isForbiddenEdge(BlockPos from, Direction dir, BlockPos edgeA, Direction edgeDir) {
        BlockPos edgeB = edgeA.relative(edgeDir);
        if (from.equals(edgeA) && dir == edgeDir) return true;
        return from.equals(edgeB) && dir == edgeDir.getOpposite();
    }

    private static MananetNode getNodeOrBlockNode(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MananetNode node) return node;
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity()) return null;
        if (state.getBlock() instanceof AbstractNodeBlock nodeBlock) return new MananetBlockNode(level, pos, nodeBlock);
        return null;
    }

    private static MananetNodeState getOrCreateNodeState(ServerLevel level, BlockPos pos) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity()) return null;
        if (!(state.getBlock() instanceof AbstractNodeBlock block)) return null;
        return manager.getOrCreateBlockNode(pos, () -> {
            MananetNodeState node_state = new MananetNodeState();
            block.init_node_state(level, pos, state, node_state);
            return node_state;
        });
    }

    private static Component collectComponentByState(
            ServerLevel level,
            UUID oldRoot,
            long startPosLong,
            LongOpenHashSet excluded,
            LongOpenHashSet visited,
            long edgeA,
            Direction edgeDir
    ) {
        MananetNetworkManager manager = MananetNetworkManager.get(level);
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        queue.enqueue(startPosLong);

        LongArrayList positions = new LongArrayList();
        Mana cache = new Mana();
        Mana load = new Mana();
        int size = 0;

        long edgeB = edgeDir == null ? 0L : BlockPos.of(edgeA).relative(edgeDir).asLong();
        while (!queue.isEmpty()) {
            long posLong = queue.dequeueLong();
            if (excluded.contains(posLong)) continue;
            if (!visited.add(posLong)) continue;

            MananetNodeState state = manager.getBlockNode(posLong);
            if (state == null) continue;
            if (state.networkId == null) continue;
            if (!manager.resolveNetworkId(state.networkId).equals(oldRoot)) continue;
            if (!oldRoot.equals(state.networkId)) state.networkId = oldRoot;

            positions.add(posLong);
            size++;
            cache.add(state.cache);
            load.add(state.load);

            for (Direction dir : Direction.values()) {
                int bit = 1 << dir.ordinal();
                if ((state.connectivityMask & bit) == 0) continue;
                if (edgeDir != null) {
                    if (posLong == edgeA && dir == edgeDir) continue;
                    if (posLong == edgeB && dir == edgeDir.getOpposite()) continue;
                }

                int x = BlockPos.getX(posLong) + dir.getStepX();
                int y = BlockPos.getY(posLong) + dir.getStepY();
                int z = BlockPos.getZ(posLong) + dir.getStepZ();
                long npLong = BlockPos.asLong(x, y, z);
                if (excluded.contains(npLong)) continue;

                MananetNodeState ns = manager.getBlockNode(npLong);
                if (ns == null) continue;
                int obit = 1 << dir.getOpposite().ordinal();
                if ((ns.connectivityMask & obit) == 0) continue;
                if (ns.networkId == null) continue;
                if (!manager.resolveNetworkId(ns.networkId).equals(oldRoot)) continue;
                queue.enqueue(npLong);
            }
        }

        return new Component(positions, cache, load, size);
    }

    private record Component(LongArrayList positions, Mana cache, Mana load, int size) {}

    private record RebuildResult(BlockPos startPos, Set<BlockPos> positions, Set<UUID> oldNetworkIds, Mana cache, Mana load, int size) {}
}
