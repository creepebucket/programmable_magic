package org.creepebucket.programmable_magic.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.registries.ModDataComponents;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class PlaceholderTooltipHandler {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof WandItemPlaceholder)) return;

        String key = event.getItemStack().get(ModDataComponents.WAND_PLACEHOLDER_ITEM_ID.get());
        if (key == null || key.isEmpty()) key = "minecraft:air";
        ResourceLocation rl = ResourceLocation.tryParse(key);
        String display;
        if (rl == null) {
            display = "AIR";
        } else if (rl.equals(ResourceLocation.withDefaultNamespace("air"))) {
            display = "AIR";
        } else {
            var h = BuiltInRegistries.ITEM.get(rl);
            display = h.isPresent() ? new net.minecraft.world.item.ItemStack(h.get()).getHoverName().getString() : "AIR";
        }

        event.getToolTip().add(Component.literal("绑定: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(display).withStyle(ChatFormatting.YELLOW)));
        event.getToolTip().add(Component.literal("与任意物品无序合成进行绑定").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
