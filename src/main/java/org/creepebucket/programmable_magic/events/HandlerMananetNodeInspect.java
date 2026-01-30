package org.creepebucket.programmable_magic.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.MananetNode;
import org.creepebucket.programmable_magic.mananet.api.MananetNodes;
import org.creepebucket.programmable_magic.mananet.logic.MananetNetworkManager;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class HandlerMananetNodeInspect {

    /**
     * 调试用：右键节点方块时，把该节点所属网络的汇总信息发送给玩家。
     *
     * <p>输出包括 network_id、网络规模（size）、当前 availableMana、总 cache、总 load（标注为每秒）。</p>
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var pos = event.getPos();
        MananetNode node = MananetNodes.get(level, pos);
        if (node == null) return;
        if (node.getNetworkId() == null) return;

        MananetNetworkManager.NetworkInfo info = MananetNetworkManager.get(level).getNetworkInfo(node.getNetworkId());
        player.sendSystemMessage(Component.literal("mananet_node_info network_id=" + info.id() + " size=" + info.size()));
        player.sendSystemMessage(Component.literal("availableMana " + formatMana(info.mana())));
        player.sendSystemMessage(Component.literal("cache " + formatMana(info.cache())));
        player.sendSystemMessage(Component.literal("load_per_s " + formatMana(info.load())));
    }

    /**
     * 把四分量魔力格式化成一行易读文本。
     */
    private static String formatMana(Mana mana) {
        return "radiation=" + ModUtils.formattedNumber(mana.getRadiation())
                + " temperature=" + ModUtils.formattedNumber(mana.getTemperature())
                + " momentum=" + ModUtils.formattedNumber(mana.getMomentum())
                + " pressure=" + ModUtils.formattedNumber(mana.getPressure());
    }
}
