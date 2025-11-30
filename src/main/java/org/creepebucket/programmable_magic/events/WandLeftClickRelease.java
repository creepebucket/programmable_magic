package org.creepebucket.programmable_magic.events;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
 
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.creepebucket.programmable_magic.items.wand.BaseWand;
import org.creepebucket.programmable_magic.network.wand.SpellReleasePacket;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import java.util.ArrayList;
import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class WandLeftClickRelease {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!event.getLevel().isClientSide()) return;
        tryRelease(event);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide()) return;
        tryRelease(event);
        // 不取消，避免影响正常破坏方块；若需独占，可改为 event.setCanceled(true)
    }

    private static void tryRelease(PlayerInteractEvent event) {
        var player = event.getEntity();
        if (player == null) return;
        // 潜行用于打开小界面，不在潜行时释放
        if (player.isShiftKeyDown()) return;

        ItemStack wand = getHeldWand(player);
        if (wand.isEmpty() || !(wand.getItem() instanceof BaseWand)) return;
        List<String> saved = wand.get(ModDataComponents.WAND_SPELLS_SMALL.get());
        if (saved == null || saved.isEmpty()) return;

        List<ItemStack> stacks = new ArrayList<>();
        for (String key : saved) {
            var rl = net.minecraft.resources.ResourceLocation.tryParse(key);
            var h = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
            h.ifPresent(holder -> stacks.add(new ItemStack(holder)));
        }

        Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new SpellReleasePacket(stacks)));
    }

    private static ItemStack getHeldWand(net.minecraft.world.entity.player.Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof BaseWand) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof BaseWand) return off;
        return ItemStack.EMPTY;
    }
}
