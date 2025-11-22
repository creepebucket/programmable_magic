package org.creepebucket.programmable_magic.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;
import org.creepebucket.programmable_magic.items.wand.BaseWand;

import java.util.Set;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class CommonItemTooltipHandler {

    private static final Set<String> TIPPED_ITEMS = Set.of(
            "pure_redstone_dust",
            "redstone_gold_alloy",
            "rg_alloy_wire",
            "rg_alloy_rod",
            "debris_clay",
            "covered_rg_alloy_wire",
            "rg_alloy_wand"
    );

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        var item = event.getItemStack().getItem();
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        if (key == null || !MODID.equals(key.getNamespace())) return;
        if (!TIPPED_ITEMS.contains(key.getPath())) return;

        long hwnd = Minecraft.getInstance().getWindow().getWindow();
        boolean lalt = InputConstants.isKeyDown(hwnd, GLFW.GLFW_KEY_LEFT_ALT);
        if (!lalt) {
            event.getToolTip().add(Component.translatable("tooltip." + MODID + ".hold_lalt").withStyle(ChatFormatting.GRAY));
        } else {
            event.getToolTip().add(Component.translatable("tooltip." + MODID + "." + key.getPath()).withStyle(ChatFormatting.GRAY));
        }

        // 对于所有魔杖（BaseWand），永远在底部追加绿色属性说明
        if (event.getItemStack().getItem() instanceof BaseWand wand) {
            String mult = String.format("%.2f", wand.getManaMult());
            String slots = String.valueOf(wand.getSlots());
            event.getToolTip().add(Component.literal("魔力修正 x" + mult).withStyle(ChatFormatting.BLUE));
            event.getToolTip().add(Component.literal("槽位数 " + slots).withStyle(ChatFormatting.YELLOW));
        }
    }
}
