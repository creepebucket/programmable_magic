package org.creepebucket.programmable_magic.mananet;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.registries.ModAttachments;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class NetworkManaManager {
    public static Map<Level, Map<Long, Map<String, ModUtils.Mana>>> data = new WeakHashMap<>();
    public static Map<Level, Map<Long, Long>> touched_tick = new WeakHashMap<>();
    public static Map<Level, Map<Long, ModUtils.Mana>> cached_load = new WeakHashMap<>();
    public static Map<Level, Map<Long, ModUtils.Mana>> cached_cache = new WeakHashMap<>();
    public static int nextSave = 0, tickCount = 99999999;
    public static long logic_tick = 0;

    public static void touch(Level level, Long id) {
        if (!touched_tick.containsKey(level)) touched_tick.put(level, new HashMap<>());
        touched_tick.get(level).put(id, logic_tick);
    }

    public static ModUtils.Mana getCached(Level level, Long id, String key, ModUtils.Mana value) {
        if (touched_tick.get(level).getOrDefault(id, -1L) != logic_tick) {
            if (key.equals("load")) return cached_load.get(level).getOrDefault(id, value);
            if (key.equals("cache")) return cached_cache.get(level).getOrDefault(id, value);
        }
        return value;
    }

    /**
     * 为你的魔力网络获取魔力数据
     *
     * @param level 网络所在维度
     * @param id    网络id
     * @return 当前网络的魔力数据
     */
    public static NetworkManaData getManaData(Level level, Long id) {
        if (data.containsKey(level)) {
            if (!touched_tick.containsKey(level)) touched_tick.put(level, new HashMap<>());
            if (!cached_load.containsKey(level)) cached_load.put(level, new HashMap<>());
            if (!cached_cache.containsKey(level)) cached_cache.put(level, new HashMap<>());
            var levelData = data.get(level);
            if (!levelData.containsKey(id)) {
                // 需要判断新网络的情况
                var fresh = new HashMap<>(Map.of("current", new ModUtils.Mana(), "cache", new ModUtils.Mana(), "load", new ModUtils.Mana()));
                levelData.put(id, fresh);
                cached_load.get(level).put(id, new ModUtils.Mana());
                cached_cache.get(level).put(id, new ModUtils.Mana());
                return new NetworkManaData(id, level, fresh);
            } else {
                return new NetworkManaData(id, level, levelData.get(id));
            }
        } else {
            // 获取世界的魔力信息, 然后再查找
            var copy = new HashMap<Long, Map<String, ModUtils.Mana>>();
            level.getData(ModAttachments.DIMENSIONAL_MANA_DATA).forEach((k, v) -> copy.put(k, new HashMap<>(v)));
            data.put(level, copy);
            touched_tick.put(level, new HashMap<>());
            cached_load.put(level, new HashMap<>());
            cached_cache.put(level, new HashMap<>());
            return getManaData(level, id);
        }

    }

    /**
     * 更新全局魔力数据表中的数据
     *
     * @param manaData 需要更新的数据
     */
    public static void update(NetworkManaData manaData) {
        data.get(manaData.level).put(manaData.id, manaData.data);
    }

    /**
     * 计算魔力网络中的消耗和产出
     */
    public static void calculate() {
        logic_tick++;
        tickCount++;

        for (Level level : new HashMap<>(data).keySet()) {
            var levelData = data.get(level);
            var levelTouched = touched_tick.get(level);
            if (!cached_load.containsKey(level)) cached_load.put(level, new HashMap<>());
            if (!cached_cache.containsKey(level)) cached_cache.put(level, new HashMap<>());
            for (Long id : new HashMap<>(levelData).keySet()) {
                if (levelTouched.getOrDefault(id, -1L) != logic_tick - 1) {
                    levelData.remove(id);
                    levelTouched.remove(id);
                    cached_load.get(level).remove(id);
                    cached_cache.get(level).remove(id);
                    continue;
                }
                var networkData = levelData.get(id);
                var current = networkData.get("current");
                var load = networkData.get("load");
                var cache = networkData.get("cache");

                networkData.put("current", current.subtract(load).min(cache));
                cached_load.get(level).put(id, load);
                cached_cache.get(level).put(id, cache);
                networkData.put("load", new ModUtils.Mana());
                networkData.put("cache", new ModUtils.Mana());
            }
        }

        // 半秒到一秒保存一次
        if (tickCount > nextSave) {
            tickCount = 0;
            nextSave = ModUtils.simpleRandInt(10, 20);

            for (Level level:data.keySet()) {
                level.setData(ModAttachments.DIMENSIONAL_MANA_DATA, data.get(level));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        calculate();
    }
}
