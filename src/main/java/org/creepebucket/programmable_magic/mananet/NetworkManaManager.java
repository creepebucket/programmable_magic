package org.creepebucket.programmable_magic.mananet;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.registries.ModAttachments;

import java.util.HashMap;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class NetworkManaManager {
    public static Map<Level, Map<Long, Map<String, ModUtils.Mana>>> data = new HashMap<>();
    public static int nextSave = 0, tickCount = 99999999;

    /**
     * 为你的魔力网络获取魔力数据
     *
     * @param level 网络所在维度
     * @param id    网络id
     * @return 当前网络的魔力数据
     */
    public static NetworkManaData getManaData(Level level, Long id) {
        if (data.containsKey(level)) {
            var levelData = data.get(level);
            if (levelData.containsKey(id)) {
                // 需要判断新网络的情况
                return new NetworkManaData(id, level, new HashMap<>(
                        Map.of("current", new ModUtils.Mana(), "cache", new ModUtils.Mana(), "load", new ModUtils.Mana())));
            } else {
                return new NetworkManaData(id, level, levelData.get(id));
            }
        } else {
            // 获取世界的魔力信息, 然后再查找
            data.put(level, level.getData(ModAttachments.DIMENSIONAL_MANA_DATA));
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
        tickCount++;

        for(Map<Long, Map<String, ModUtils.Mana>> levelData:data.values()) {
            for (Map<String, ModUtils.Mana> networkData:levelData.values()) {
                var current = networkData.get("current");
                var load = networkData.get("load");
                var cache = networkData.get("cache");

                // 计算并清空值
                networkData.put("load", new ModUtils.Mana());
                networkData.put("cache", new ModUtils.Mana());

                networkData.put("current", current.subtract(load).min(cache));
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
