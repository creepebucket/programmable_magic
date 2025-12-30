package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.server.level.ServerLevel;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.ManaMath;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import java.util.function.Supplier;
import net.minecraft.core.Direction;

public final class MananetNetworkManager {

    private static final Map<ServerLevel, MananetNetworkManager> MANAGERS = new HashMap<>();

    public static MananetNetworkManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, ignored -> new MananetNetworkManager());
    }

    public static final class NetworkState {
        public final UUID id;
        public Mana mana = new Mana();
        public Mana cache = new Mana();
        public Mana load = new Mana();
        public int size = 0;

        private NetworkState(UUID id) {
            this.id = id;
        }
    }

    private final Map<UUID, NetworkState> networks = new HashMap<>();
    private final Long2ObjectOpenHashMap<MananetNodeState> blockNodes = new Long2ObjectOpenHashMap<>();
    private final Map<UUID, UUID> parent = new HashMap<>();
    private boolean persistentLoaded = false;

    private final ArrayDeque<Long> dirtyQueue = new ArrayDeque<>();
    private final HashSet<Long> dirtySet = new HashSet<>();

    public record EdgeChange(long posLong, Direction dir, boolean oldValue, boolean newValue) {}
    private final ArrayDeque<EdgeChange> edgeChangeQueue = new ArrayDeque<>();

    public record NodeRemoval(long posLong, UUID oldNetworkId, int connectivityMask, Mana cache, Mana load, int connectedNeighbors) {}
    private final ArrayDeque<NodeRemoval> removalQueue = new ArrayDeque<>();
    private final HashSet<Long> removalSet = new HashSet<>();

    public record NetworkInfo(UUID id, Mana mana, Mana cache, Mana load, int size) {}

    public NetworkState getOrCreate(UUID id) {
        return networks.computeIfAbsent(id, NetworkState::new);
    }

    public UUID resolveNetworkId(UUID id) {
        if (id == null) return null;
        UUID p = parent.get(id);
        if (p == null) return id;
        UUID root = resolveNetworkId(p);
        if (!root.equals(p)) parent.put(id, root);
        return root;
    }

    public boolean isPersistentLoaded() {
        return persistentLoaded;
    }

    public void loadPersistentParent(Map<UUID, UUID> persistedParent) {
        parent.clear();
        parent.putAll(persistedParent);
        persistentLoaded = true;
    }

    public Map<UUID, UUID> exportParent() {
        return new HashMap<>(parent);
    }

    public UUID union(UUID a, UUID b) {
        UUID ra = resolveNetworkId(a);
        UUID rb = resolveNetworkId(b);
        if (ra.equals(rb)) return ra;

        NetworkState sa = networks.get(ra);
        NetworkState sb = networks.get(rb);
        if (sa == null) sa = getOrCreate(ra);
        if (sb == null) sb = getOrCreate(rb);

        if (sa.size < sb.size) {
            UUID tmp = ra;
            ra = rb;
            rb = tmp;
            NetworkState ts = sa;
            sa = sb;
            sb = ts;
        }

        parent.put(rb, ra);
        sa.mana.add(sb.mana);
        sa.cache.add(sb.cache);
        sa.load.add(sb.load);
        sa.size += sb.size;
        networks.remove(rb);
        sa.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(sa.mana), sa.cache);
        return ra;
    }

    public MananetNodeState getOrCreateBlockNode(BlockPos pos, Supplier<MananetNodeState> init) {
        return blockNodes.computeIfAbsent(pos.asLong(), ignored -> init.get());
    }

    public MananetNodeState getBlockNode(BlockPos pos) {
        return blockNodes.get(pos.asLong());
    }

    public MananetNodeState getBlockNode(long posLong) {
        return blockNodes.get(posLong);
    }

    public void putBlockNode(long posLong, MananetNodeState state) {
        blockNodes.put(posLong, state);
    }

    public void removeBlockNode(long posLong) {
        blockNodes.remove(posLong);
    }

    public void removeBlockNode(BlockPos pos) {
        blockNodes.remove(pos.asLong());
    }

    public Iterable<Long2ObjectMap.Entry<MananetNodeState>> iterateBlockNodes() {
        return blockNodes.long2ObjectEntrySet();
    }

    public NetworkState getNetworkIfPresent(UUID id) {
        return networks.get(resolveNetworkId(id));
    }

    public Iterable<NetworkState> iterateNetworks() {
        return networks.values();
    }

    public void markDirty(BlockPos pos) {
        long key = pos.asLong();
        if (dirtySet.add(key)) dirtyQueue.addLast(key);
    }

    public Long pollDirty() {
        while (true) {
            Long key = dirtyQueue.pollFirst();
            if (key == null) return null;
            if (dirtySet.remove(key)) return key;
        }
    }

    public void enqueueEdgeChange(BlockPos pos, Direction dir, boolean oldValue, boolean newValue) {
        edgeChangeQueue.addLast(new EdgeChange(pos.asLong(), dir, oldValue, newValue));
    }

    public EdgeChange pollEdgeChange() {
        return edgeChangeQueue.pollFirst();
    }

    public void enqueueRemoval(BlockPos pos, UUID oldNetworkId, int connectivityMask, Mana cache, Mana load, int connectedNeighbors) {
        long key = pos.asLong();
        if (!removalSet.add(key)) return;
        removalQueue.addLast(new NodeRemoval(key, oldNetworkId, connectivityMask, cache, load, connectedNeighbors));
    }

    public NodeRemoval pollRemoval() {
        NodeRemoval removal = removalQueue.pollFirst();
        if (removal == null) return null;
        removalSet.remove(removal.posLong);
        return removal;
    }

    public int getSize(UUID id) {
        UUID root = resolveNetworkId(id);
        return networks.get(root) != null ? networks.get(root).size : 0;
    }

    public Mana getMana(UUID id) {
        UUID root = resolveNetworkId(id);
        return ManaMath.copy(getOrCreate(root).mana);
    }

    public NetworkInfo getNetworkInfo(UUID id) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        return new NetworkInfo(root, ManaMath.copy(state.mana), ManaMath.copy(state.cache), ManaMath.copy(state.load), state.size);
    }

    public void setNetwork(UUID id, Mana mana, Mana cache, Mana load, int size) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(mana), cache);
        state.cache = cache;
        state.load = load;
        state.size = size;
    }

    public void removeNetwork(UUID id) {
        UUID root = resolveNetworkId(id);
        networks.remove(root);
        parent.remove(root);
    }

    public void removeNetworks(Iterable<UUID> ids) {
        for (UUID id : ids) removeNetwork(id);
    }

    public void addMana(UUID id, Mana delta) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        state.mana.add(delta);
        state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(state.mana), state.cache);
    }

    public boolean canProduce(UUID id) {
        UUID root = resolveNetworkId(id);
        NetworkState state = getOrCreate(root);
        Mana perTick = ManaMath.scale(state.load, 1.0 / 20.0);
        Mana cost = ManaMath.positivePart(perTick);
        return ManaMath.canAfford(state.mana, cost);
    }

    public void applyContribution(UUID networkId, Mana cacheDelta, Mana loadDelta, int sizeDelta) {
        UUID root = resolveNetworkId(networkId);
        NetworkState state = getOrCreate(root);
        state.cache.add(cacheDelta);
        state.load.add(loadDelta);
        state.size += sizeDelta;
        state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(state.mana), state.cache);
    }

    public void tick() {
        for (NetworkState state : networks.values()) {
            Mana perTick = ManaMath.scale(state.load, 1.0 / 20.0);
            Mana cost = ManaMath.positivePart(perTick);
            if (ManaMath.canAfford(state.mana, cost)) state.mana.add(ManaMath.scale(state.load, -1.0 / 20.0));
            state.mana = ManaMath.clampToCache(ManaMath.clampNonNegative(state.mana), state.cache);
        }
    }
}
