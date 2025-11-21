package org.creepebucket.programmable_magic.items.mana_cell;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;
import java.util.Objects;
import org.creepebucket.programmable_magic.ModUtils;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ManaCellTooltip {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {

        //检查手持物品
        if (event.getItemStack().getItem() instanceof BaseManaCell) {
            ItemStack cellStack = event.getItemStack();
            BaseManaCell cell = (BaseManaCell) cellStack.getItem();

            event.getToolTip().add(Component.translatable("tooltip.programmable_magic.mana_stored"));

            // 显示魔力
            Map<String, ChatFormatting> manaTypes = Map.of(
                    "radiation", ChatFormatting.YELLOW,
                    "temperature", ChatFormatting.RED,
                    "momentum", ChatFormatting.BLUE,
                    "pressure", ChatFormatting.GREEN
            );

            for (Map.Entry<String, ChatFormatting> entry : manaTypes.entrySet()) {
                String manaType = entry.getKey();
                ChatFormatting color = entry.getValue();
                String shortName = manaType.substring(0, 4);

                event.getToolTip().add(Component.translatable("tooltip.programmable_magic.mana_" + shortName).withStyle(color)
                        .append(Component.literal(" " + ModUtils.FormattedManaString(cell.getMana(cellStack, manaType)) + " "))
                        .append(progressBar(0.0, cell.getMaxMana(), cell.getMana(cellStack, manaType), 30, color))
                        .append(Component.literal(" " + ModUtils.FormattedManaString(cell.getMaxMana()))));
            }
        }
    }

    public static MutableComponent progressBar(double min, double max, double current, int length, ChatFormatting style) {
        double fill_rate = (max - min) == 0 ? 0.0 : current / (max - min);
        int filled = (int) Math.round(length * fill_rate);
        if (filled < 0) filled = 0;
        if (filled > length) filled = length;
        int unfilled = length - filled;

        return Component.literal("|".repeat(filled)).withStyle(style).append(Component.literal("|".repeat(unfilled)).withStyle(ChatFormatting.GRAY));
    }
}
