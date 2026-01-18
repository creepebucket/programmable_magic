package org.creepebucket.programmable_magic.client.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.creepebucket.programmable_magic.items.BaseSpellItem;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class SpellTooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BaseSpellItem spellItem)) {
            return;
        }

        SpellItemLogic logic = spellItem.getLogic();
        List<Component> tooltip = event.getToolTip();

        appendOverloads(tooltip, logic.RightParamsOffset, logic.getNeededParamsType(), logic.getReturnParamsType());
        appendDescriptions(tooltip, logic.getTooltip());
    }

    private static void appendOverloads(List<Component> tooltip, int rightParamsOffset, List<List<SpellValueType>> inputs, List<List<SpellValueType>> outputs) {
        if (inputs.isEmpty() && outputs.isEmpty()) {
            return;
        }

        MutableComponent header = Component.literal("法术重载:").withStyle(ChatFormatting.GOLD);
        if (rightParamsOffset > 0) {
            header.append(Component.literal("  右置" + rightParamsOffset + "参").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(header);
        int overloads = Math.max(inputs.size(), outputs.size());
        for (int i = 0; i < overloads; i++) {
            List<SpellValueType> in = i < inputs.size() ? inputs.get(i) : List.of();
            List<SpellValueType> out = i < outputs.size() ? outputs.get(i) : List.of();
            tooltip.add(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY).append(buildSignatureLine(in, out)));
        }
    }

    private static void appendDescriptions(List<Component> tooltip, List<Component> descriptions) {
        if (descriptions.isEmpty()) {
            return;
        }

        tooltip.add(Component.literal("法术说明:").withStyle(ChatFormatting.AQUA));
        for (Component description : descriptions) {
            tooltip.add(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(description.copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }

    private static Component buildSignatureLine(List<SpellValueType> inputs, List<SpellValueType> outputs) {
        MutableComponent line = Component.empty();
        line.append(formatTuple(inputs));
        line.append(Component.literal(" -> ").withStyle(ChatFormatting.DARK_GRAY));
        line.append(formatTuple(outputs));
        return line;
    }

    private static Component formatTuple(List<SpellValueType> types) {
        MutableComponent tuple = Component.literal("(").withStyle(ChatFormatting.GRAY);
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) {
                tuple.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
            }
            tuple.append(types.get(i).typed());
        }
        tuple.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
        return tuple;
    }
}
