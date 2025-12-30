package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public final class MananetPersistenceHandler {

    private MananetPersistenceHandler() {}

    /**
     * chunk 加载：把 chunk 附件里的节点数据装入运行时 manager，并汇总到网络状态。
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            MananetNetworkPersistence.onChunkLoad(level, event.getChunk());
        }
    }

    /**
     * chunk 卸载：从运行时 manager 移除该 chunk 的节点状态，并扣除其对网络的贡献。
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            MananetNetworkPersistence.onChunkUnload(level, event.getChunk());
        }
    }

    /**
     * 世界保存：把运行时 union-find 与网络当前 mana 写入 SavedData。
     */
    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel level) {
            MananetNetworkPersistence.onLevelSave(level);
        }
    }
}
