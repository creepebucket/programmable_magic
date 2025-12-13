package org.creepebucket.programmable_magic.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.creepebucket.programmable_magic.items.SpellScrollItem;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ScrollTooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof SpellScrollItem)) return;

        List<net.minecraft.world.item.ItemStack> list = event.getItemStack().get(ModDataComponents.SPELL_SCROLL_STACKS.get());
        if (list == null) return;

        List<Component> tooltip = event.getToolTip();
        if (list.isEmpty()) {
            tooltip.add(Component.translatable("tooltip." + MODID + ".spell_scroll_empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip." + MODID + ".spell_scroll_header").withStyle(ChatFormatting.AQUA));
        int shown = 0;
        for (var st : list) {
            if (st == null || st.isEmpty()) continue;
            if (!SpellRegistry.isSpell(st.getItem())) continue;
            tooltip.add(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(st.getHoverName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
            shown++;
            if (shown >= 12) break;
        }
        int remain = 0;
        if (list != null) {
            for (var st : list) if (st != null && !st.isEmpty() && SpellRegistry.isSpell(st.getItem())) remain++;
            remain = Math.max(0, remain - shown);
        }
        if (remain > 0) {
            tooltip.add(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("tooltip." + MODID + ".and_more", remain).withStyle(ChatFormatting.GRAY)));
        }
    }
}
