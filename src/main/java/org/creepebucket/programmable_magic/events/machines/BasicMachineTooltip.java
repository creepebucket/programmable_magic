package org.creepebucket.programmable_magic.events.machines;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.mananet.machines.BasicMachine;
import org.creepebucket.programmable_magic.utils.RelativeBlockPos;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class BasicMachineTooltip {

    public static void append(ItemStack stack, List<Component> tooltip, BasicMachine machine, boolean ctrl, boolean shift, boolean alt, boolean tabKey) {
        tooltip.add(Component.translatable("tooltip." + MODID + ".machine.key_shift", "Shift").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip." + MODID + ".machine.key_alt", "Alt").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip." + MODID + ".machine.key_tab", "Tab").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip." + MODID + ".machine.key_ctrl", "Ctrl").withStyle(ChatFormatting.DARK_GRAY));

        String blockId = BuiltInRegistries.BLOCK.getKey(machine).getPath();
        if (shift) {
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine.section.intro").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine." + blockId + ".intro").withStyle(ChatFormatting.GRAY));
        }
        if (alt) {
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine.section.structure").withStyle(ChatFormatting.AQUA));
            for (var entry : machine.IO_DEFINITION.entrySet()) {
                tooltip.add(Component.translatable("gui.programmable_magic.machine.io_dummy." + entry.getValue()).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(": " + formatPos(entry.getKey())).withStyle(ChatFormatting.GREEN)));
            }
            for (var pos : machine.MANA_LINK_POSITIONS) {
                tooltip.add(Component.translatable("tooltip." + MODID + ".machine.mana_link_prefix").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(formatPos(pos)).withStyle(ChatFormatting.GOLD)));
            }
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine.read_how").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        if (tabKey) {
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine.section.usage").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine." + blockId + ".usage").withStyle(ChatFormatting.GRAY));
        }
        if (ctrl) {
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine.section.mode").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip." + MODID + ".machine." + blockId + ".mode").withStyle(ChatFormatting.GRAY));
        }
    }

    public static String formatPos(RelativeBlockPos pos) {
        return "(" + pos.facing_off + ", " + pos.y_off + ", " + pos.cw90_off + ")";
    }
}
