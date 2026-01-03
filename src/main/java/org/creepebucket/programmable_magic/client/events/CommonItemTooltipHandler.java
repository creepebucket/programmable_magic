package org.creepebucket.programmable_magic.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.creepebucket.programmable_magic.items.Wand;
import org.lwjgl.glfw.GLFW;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.ModUtils;

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
        Identifier key = BuiltInRegistries.ITEM.getKey(item);
        if (key == null || !MODID.equals(key.getNamespace())) return;
        if (!TIPPED_ITEMS.contains(key.getPath())) return;

        var window = Minecraft.getInstance().getWindow();
        boolean lalt = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT);
        if (!lalt) {
            event.getToolTip().add(Component.translatable("tooltip." + MODID + ".hold_lalt").withStyle(ChatFormatting.GRAY));
        } else {
            event.getToolTip().add(Component.translatable("tooltip." + MODID + "." + key.getPath()).withStyle(ChatFormatting.GRAY));
        }

        // 对于所有魔杖（Wand），永远在底部追加绿色属性说明
        if (event.getItemStack().getItem() instanceof Wand wand) {
            java.util.List<net.minecraft.world.item.ItemStack> plugins = event.getItemStack().get(ModDataComponents.WAND_PLUGINS.get());
            var values = ModUtils.computeWandValues(plugins);
            int slotsCap = (int) Math.floor(values.spellSlots);
            String slots = String.valueOf(slotsCap);
            String pluginSlots = String.valueOf(wand.getPluginSlots());

            String mult = String.format("%.2f", values.manaMult);
            String energyPerSec = ModUtils.formattedNumber(values.chargeRateW);

            event.getToolTip().add(Component.literal("魔力修正 x" + mult).withStyle(ChatFormatting.BLUE));
            event.getToolTip().add(Component.literal("槽位数 " + slots).withStyle(ChatFormatting.YELLOW));
            event.getToolTip().add(Component.literal("充能速率 " + energyPerSec + "W").withStyle(ChatFormatting.GREEN));
            event.getToolTip().add(Component.literal("插件槽位 " + pluginSlots).withStyle(ChatFormatting.AQUA));
        }
    }
}
