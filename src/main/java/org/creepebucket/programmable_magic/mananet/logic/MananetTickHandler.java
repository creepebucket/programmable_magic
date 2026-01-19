package org.creepebucket.programmable_magic.mananet.logic;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class MananetTickHandler {

    /**
     * 服务器世界 tick 后置阶段：处理 Mananet 的延迟队列并推进网络“每秒负载”。
     *
     * <p>这里拆成两步：</p>
     * <ul>
     *     <li>{@link MananetNetworkLogic#processPending(ServerLevel)}：处理放置/移除/连通变化的结构更新。</li>
     *     <li>{@link MananetNetworkManager#tick()}：按网络汇总的 load 对当前 availableMana 做每 tick 积分。</li>
     * </ul>
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            MananetNetworkLogic.processPending(serverLevel);
            MananetNetworkManager.get(serverLevel).tick();
        }
    }
}
