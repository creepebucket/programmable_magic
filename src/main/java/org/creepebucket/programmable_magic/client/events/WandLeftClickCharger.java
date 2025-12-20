package org.creepebucket.programmable_magic.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.creepebucket.programmable_magic.items.Wand;
import org.creepebucket.programmable_magic.network.dataPackets.SpellReleasePacket;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.ModUtils;
import org.lwjgl.glfw.GLFW;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 客户端左键长按充能与释放：
 * - 玩家在未打开界面时按住左键开始充能，松开后发送法术释放数据包（仅带充能时长）。
 * - 仅当主手或副手持有 Wand 时生效。
 */
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class WandLeftClickCharger {

    private static boolean charging = false;
    private static int chargeTicks = 0;

    /**
     * 每个客户端 tick 检查左键状态并更新充能/释放。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        Player player = mc.player;
        if (player == null) return;

        boolean mouseDown = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        // 需要手持魔杖（主/副手之一）
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        boolean hasWand = (main.getItem() instanceof Wand) || (off.getItem() instanceof Wand);
        if (!hasWand || mc.screen != null) {
            if (charging) charging = false;
            return;
        }

        if (mouseDown) {
            if (!charging) {
                charging = true;
                chargeTicks = 0;
            } else {
                chargeTicks++;
            }

            // HUD：动作条显示 “|>>> XXX J <<<|”
            ItemStack wand = main.getItem() instanceof Wand ? main : off;
            java.util.List<net.minecraft.world.item.ItemStack> plugins = new java.util.ArrayList<>();
            {
                java.util.List<net.minecraft.world.item.ItemStack> saved = wand.get(ModDataComponents.WAND_PLUGINS.get());
                if (saved != null) for (var it : saved) { if (it != null && !it.isEmpty()) plugins.add(it.copy()); }
            }
            var values = ModUtils.computeWandValues(plugins);
            double rate = values.chargeRateW;
            double mana = ((double) chargeTicks / 20.0) * (rate / 1000.0);
            String bar = "|>>> " + ModUtils.FormattedManaString(mana) + " <<<|";
            player.displayClientMessage(Component.literal(bar), true);
        } else if (charging) {
            double chargeSec = Math.max(0, chargeTicks) / 20.0;
            java.util.List<net.minecraft.world.item.ItemStack> spells = java.util.List.of();
            java.util.List<net.minecraft.world.item.ItemStack> plugins = new java.util.ArrayList<>();
            {
                net.minecraft.world.item.ItemStack wand = main.getItem() instanceof Wand ? main : off;
                java.util.List<net.minecraft.world.item.ItemStack> saved = wand.get(ModDataComponents.WAND_PLUGINS.get());
                if (saved != null) for (var it : saved) { if (it != null && !it.isEmpty()) plugins.add(it.copy()); }
            }
            var payload = new SpellReleasePacket(spells, chargeSec, plugins);
            var connection = mc.getConnection();
            if (connection != null) connection.send(new ServerboundCustomPayloadPacket(payload));
            charging = false;
        }
    }
}
