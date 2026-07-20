package org.creepebucket.programmable_magic.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.creepebucket.programmable_magic.ModConfig;
import org.creepebucket.programmable_magic.items.api.ModItemExtensions;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.utils.ModUtils;
import org.lwjgl.glfw.GLFW;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ItemTooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        var window = Minecraft.getInstance().getWindow();
        // 对于所有魔杖（Wand），永远在底部追加绿色属性说明
        boolean ctrl = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean shift = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        boolean alt = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);

        var level = Minecraft.getInstance().level;
        if (level != null) {
            int burnTime = event.getItemStack().getBurnTime(RecipeType.SMELTING, level.fuelValues());
            if (burnTime > 0)
                event.getToolTip().add(Component.translatable("tooltip.programmable_magic.heat_value",
                    ModUtils.formattedNumber(burnTime * ModConfig.CONFIG.fuelValueMultiplier.get())));
        }

        if (event.getItemStack().getItem() instanceof ModItemExtensions ext) {
            ext.appendTooltip(event.getItemStack(), event.getToolTip(), ctrl, shift, alt);
            return;
        }

        var item = event.getItemStack().getItem();
        if (WandPluginRegistry.isPlugin(item)) {
            WandPluginRegistry.getPlugin(item).appendTooltip(event.getItemStack(), event.getToolTip(), ctrl, shift, alt);
        }
    }
}
