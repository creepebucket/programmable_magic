package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.widgets.InfiniteSupplySlotWidget;

import java.util.Map;

public class WandWidgetServer {
    public static class SpellSupplyWidget extends InfiniteSupplySlotWidget {
        public SyncedValue<Integer> deltaY;
        public Coordinate delta;
        public Coordinate original;

        public SpellSupplyWidget(ItemStack supplyStack, Coordinate pos, SyncedValue<Integer> deltaY) {
            super(supplyStack, pos);

            this.deltaY = deltaY;
            this.delta = new Coordinate((sw, sh) -> 0, (sw, sh) -> deltaY.get());
            this.original = pos;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            pos = original.add(delta);
            super.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public static class WandSupplyScrollWidget extends Widget implements MouseScrollable {
        public Coordinate region;
        public int valueMultiplier;
        public SyncedValue<Integer> deltaY;

        public WandSupplyScrollWidget(Coordinate pos, Coordinate region, int valueMultiplier, SyncedValue<Integer> deltaY) {
            this.pos = pos;
            this.region = region;
            this.valueMultiplier = valueMultiplier;
            this.deltaY = deltaY;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if(!isInBounds(mouseX, mouseY, pos.toScreenX(), pos.toScreenY(), 80, 999)) return false;
            deltaY.set((int) Mth.clamp(deltaY.get() + Math.floor(scrollY * 16), region.toScreenX(), region.toScreenY()));
            return true;
        }
    }

    public static class WandSubCategoryWidget extends Widget implements Renderable {
        public SyncedValue<Integer> deltaY;
        public String key;

        public WandSubCategoryWidget(Coordinate pos, String subCategoryKey, SyncedValue<Integer> deltaY) {
            this.pos = pos;
            this.key = subCategoryKey;
            this.deltaY = deltaY;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = pos.toScreenX();
            int y = pos.toScreenY() + deltaY.get();
            Map<String, Integer> COLOR_MAP = ModUtils.SPELL_COLORS();
            var color = COLOR_MAP.getOrDefault(key, 0xFFFFFFFF);
            color = (color & 16777215) | ((int) (((color >>> 24) * 0.6)) << 24);

            graphics.fill(x, y+4, x+79, y+6, color);
            graphics.fill(x, y+7, x+79, y+25, color);
            graphics.fill(x, y+26, x+79, y+28, color);

            graphics.drawString(ClientUiContext.getFont(), Component.translatable(key), x+3, y+12, 0xFFFFFFFF);
        }
    }

}
