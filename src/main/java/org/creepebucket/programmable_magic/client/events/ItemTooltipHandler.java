package org.creepebucket.programmable_magic.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.creepebucket.programmable_magic.items.api.ModItemExtensions;
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

        if (event.getItemStack().getItem() instanceof ModItemExtensions ext) {
            ext.appendTooltip(event.getItemStack(), event.getToolTip(), ctrl, shift, alt);
        }
    }
}
