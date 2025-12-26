package org.creepebucket.programmable_magic.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.creepebucket.programmable_magic.items.Wand;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 客户端 HUD：在未按住使用时，基于“上次释放时间戳”显示被动充能能量条。
 * - 仅在主手/副手持有 Wand 时运行。
 * - 按住右键充能时，HUD 由 on_use_tick 负责，这里不显示。
 */
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class WandAutoChargeHud {

    /**
     * 客户端每 tick 更新：显示被动充能提示条。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        Player player = mc.player;
        if (player == null) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack wand = ItemStack.EMPTY;
        if (main.getItem() instanceof Wand) wand = main; else if (off.getItem() instanceof Wand) wand = off; else return;

        java.util.List<ItemStack> plugins = wand.get(ModDataComponents.WAND_PLUGINS.get());
        boolean hasAuto = false;
        if (plugins != null) {
            for (ItemStack it : plugins) {
                if (it == null || it.isEmpty()) continue;
                var id = BuiltInRegistries.ITEM.getKey(it.getItem());
                if (id != null && "wand_plugin_auto_charge".equals(id.getPath())) { hasAuto = true; break; }
            }
        }
        if (!hasAuto) return;

        // 正在按住右键使用时交给 onUseTick 显示
        if (player.isUsingItem() && player.getUseItem() == wand) return;

        double rate = ModUtils.computeWandValues(plugins).chargeRateW;

        Long last_release = wand.get(ModDataComponents.WAND_LAST_RELEASE_TIME.get());
        long now = mc.level != null ? mc.level.getGameTime() : 0L;
        if (last_release == null) last_release = now;
        long dt = Math.max(0L, now - last_release);
        double mana = (dt / 20.0) * (rate / 1000.0);
        if (mana <= 0.0) return;
        String bar = "|>>> " + ModUtils.FormattedManaString(mana) + " <<<|";
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(bar), true);
    }
}
