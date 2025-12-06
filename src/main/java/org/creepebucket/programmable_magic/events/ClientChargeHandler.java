package org.creepebucket.programmable_magic.events;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.creepebucket.programmable_magic.items.wand.BaseWand;
import org.creepebucket.programmable_magic.network.wand.SpellReleasePacket;
import org.creepebucket.programmable_magic.registries.ModDataComponents;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientChargeHandler {

    private static boolean prevDown = false;
    private static long startNs = 0L;
    private static final double MAX_SECONDS = 3.0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // 打开界面时不处理左键释放（界面中用按钮）
        if (mc.screen != null) { prevDown = false; return; }

        var player = mc.player;
        if (player.isShiftKeyDown()) { prevDown = false; return; }

        // 仅在手持魔杖时生效
        ItemStack wand = getHeldWand(player);
        if (wand.isEmpty() || !(wand.getItem() instanceof BaseWand)) { prevDown = false; return; }

        boolean down = mc.options.keyAttack.isDown();
        if (down && !prevDown) {
            startNs = System.nanoTime();
        }
        if (down) {
            double seconds = Math.max(0.0, (System.nanoTime() - startNs) / 1_000_000_000.0);
            double chargeRate = 0.0;
            if (wand.getItem() instanceof BaseWand bw) chargeRate = bw.getChargeRate();
            double mana = (chargeRate * seconds) / 1000.0; // 1 mana = 1 kJ
            String text = String.format(java.util.Locale.ROOT, "charging %.2f mana", mana);
            mc.player.displayClientMessage(Component.literal(text), true);
        } else if (!down && prevDown) {
            double seconds = Math.max(0.0, (System.nanoTime() - startNs) / 1_000_000_000.0);
            List<ItemStack> stacks = wand.get(ModDataComponents.WAND_STACKS_SMALL.get());
            if (stacks != null && !stacks.isEmpty() && mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundCustomPayloadPacket(new SpellReleasePacket(stacks, seconds)));
            }
        }
        prevDown = down;
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.screen != null) return; // 只在 HUD 绘制
        if (!prevDown) return; // 未充能
        // 仅在持杖时绘制
        ItemStack wand = getHeldWand(mc.player);
        if (wand.isEmpty() || !(wand.getItem() instanceof BaseWand)) return;

        double secs = Math.max(0.0, (System.nanoTime() - startNs) / 1_000_000_000.0);
        double chargeRate = 0.0;
        if (wand.getItem() instanceof BaseWand bw) chargeRate = bw.getChargeRate();
        double mana = (chargeRate * secs) / 1000.0;
        String text = String.format(java.util.Locale.ROOT, "charging %.2f mana", mana);

        var g = event.getGuiGraphics();
        var font = mc.font;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x = width / 2 - font.width(text) / 2;
        int y = height - 30;
        g.drawString(font, text, x, y, 0xFFFFFF, false);
    }

    private static ItemStack getHeldWand(net.minecraft.world.entity.player.Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof BaseWand) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof BaseWand) return off;
        return ItemStack.EMPTY;
    }
}
