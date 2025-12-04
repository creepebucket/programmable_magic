package org.creepebucket.programmable_magic.mananet;

import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * 每个维度一个 ManaNetService，按 netId 管理 ManaNet 实例，并负责每刻结算时钟。
 */
public final class ManaNetService {
    private static final Map<ServerLevel, ManaNetService> INSTANCES = new HashMap<>();
    public static ManaNetService get(ServerLevel level) { return INSTANCES.computeIfAbsent(level, ManaNetService::new); }

    private final ServerLevel level;
    private final Map<Long, ManaNetImpl> nets = new HashMap<>();

    private ManaNetService(ServerLevel level) { this.level = level; }

    public ManaNet getNet(long netId) {
        if (netId == 0) return null;
        return nets.computeIfAbsent(netId, id -> new ManaNetImpl(level));
    }

    // 实现：简单的逐刻结算逻辑
    private static final class ManaNetImpl implements ManaNet {
        private final ServerLevel level;
        private long lastTick = Long.MIN_VALUE;

        // 累计存量（跨刻持久）
        private final Map<String, Double> stored = new HashMap<>();

        // 本刻节点贡献与注入（逐刻清空）
        private final Map<Long, Map<String, Double>> loadByNode = new HashMap<>();
        private final Map<Long, Map<String, Double>> cacheByNode = new HashMap<>();
        private final Map<String, Double> addedThisTick = new HashMap<>();

        // 结算缓存
        private final Map<String, Double> totalLoad = new HashMap<>();
        private final Map<String, Double> totalCache = new HashMap<>();
        private final Map<String, Double> available = new HashMap<>();
        private boolean canAll = true;

        ManaNetImpl(ServerLevel level) { this.level = level; }

        private void ensureTick() {
            long now = level.getGameTime();
            if (now == lastTick) return;
            // 新刻：做一次结算
            lastTick = now;

            totalLoad.clear();
            totalCache.clear();
            available.clear();
            canAll = true;

            // 汇总负载与容量
            for (Map<String, Double> m : loadByNode.values()) m.forEach((k,v)-> totalLoad.merge(k, v, Double::sum));
            for (Map<String, Double> m : cacheByNode.values()) m.forEach((k,v)-> totalCache.merge(k, v, Double::sum));

            // 逐类型结算
            for (Map.Entry<String, Double> e : unionKeys(stored, addedThisTick, totalLoad, totalCache).entrySet()) {
                String t = e.getKey();
                double s = stored.getOrDefault(t, 0.0);
                double add = addedThisTick.getOrDefault(t, 0.0);
                double cap = totalCache.getOrDefault(t, 0.0);
                double need = totalLoad.getOrDefault(t, 0.0);

                double avail = s + add;
                if (cap > 0) avail = Math.min(avail, cap);
                available.put(t, avail);

                boolean ok = avail >= need;
                if (!ok) canAll = false;

                double consumed = Math.min(avail, need);
                double remain = avail - consumed;
                stored.put(t, (cap > 0) ? Math.min(remain, cap) : remain);
            }

            // 清空本刻注入，负载/容量由节点每刻重报
            addedThisTick.clear();
        }

        private static Map<String, Double> unionKeys(Map<String, Double>... maps) {
            Map<String, Double> r = new HashMap<>();
            for (Map<String, Double> m : maps) r.putAll(m);
            return r;
        }

        @Override
        public double getTotalMana(String type) { ensureTick(); return stored.getOrDefault(type, 0.0); }

        @Override
        public Map<String, Double> getTotalManaAll() { ensureTick(); return java.util.Map.copyOf(stored); }

        @Override
        public void setLoad(long nodeKey, String type, double amount) {
            loadByNode.computeIfAbsent(nodeKey, k -> new HashMap<>()).put(type, amount);
        }

        @Override
        public void setLoad(long nodeKey, Map<String, Double> byType) {
            loadByNode.computeIfAbsent(nodeKey, k -> new HashMap<>()).putAll(byType);
        }

        @Override
        public void setCache(long nodeKey, String type, double capacity) {
            cacheByNode.computeIfAbsent(nodeKey, k -> new HashMap<>()).put(type, capacity);
        }

        @Override
        public void setCache(long nodeKey, Map<String, Double> byType) {
            cacheByNode.computeIfAbsent(nodeKey, k -> new HashMap<>()).putAll(byType);
        }

        @Override
        public void addMana(String type, double amount) { addedThisTick.merge(type, amount, Double::sum); }

        @Override
        public void addMana(Map<String, Double> byType) { byType.forEach((k,v)-> addedThisTick.merge(k, v, Double::sum)); }

        @Override
        public boolean canProduce() { ensureTick(); return canAll; }

        @Override
        public boolean canProduce(String type) {
            ensureTick();
            double need = totalLoad.getOrDefault(type, 0.0);
            return available.getOrDefault(type, 0.0) >= need;
        }
    }
}

