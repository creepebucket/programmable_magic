package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.SlotWidget;

import java.util.Map;

public class WandWidgets {
    public static class SpellSupplyWidget extends SlotWidget {
        public SyncedValue<Integer> deltaY;
        public Coordinate delta;
        public Coordinate original;

        public SpellSupplyWidget(Slot slot, Coordinate pos, SyncedValue<Integer> deltaY) {
            super(slot, pos);

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
            if (!isInBounds(mouseX, mouseY, pos.toScreenX(), pos.toScreenY(), 80, 999)) return false;
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

            graphics.fill(x, y + 4, x + 79, y + 6, color);
            graphics.fill(x, y + 7, x + 79, y + 25, color);
            graphics.fill(x, y + 26, x + 79, y + 28, color);

            graphics.drawString(ClientUiContext.getFont(), Component.translatable(key), x + 3, y + 12, 0xFFFFFFFF);
        }
    }

    public static class WandSubcategoryJumpButton extends ImageButtonWidget {
        public SyncedValue<Integer> deltaY;
        public int target, color;

        public WandSubcategoryJumpButton(Coordinate pos, Coordinate size, SyncedValue<Integer> deltaY, int target, Component tooltip, int color) {
            super(pos, size, null, null, () -> {
            }, tooltip);
            this.deltaY = deltaY;
            this.target = target;
            this.color = color;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (contains(mouseX, mouseY)) {
                graphics.fill(pos.toScreenX(), pos.toScreenY(), pos.toScreenX() + size.toScreenX(), pos.toScreenY() + size.toScreenY(), color);
            } else {
                graphics.fill(pos.toScreenX(), pos.toScreenY(), pos.toScreenX() + size.toScreenX(), pos.toScreenY() + size.toScreenY(),
                        (color & 16777215) | ((int) (((color >>> 24) * 0.6)) << 24));
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            // 检测点击是否在按钮范围内
            if (!contains(event.x(), event.y())) return false;
            this.deltaY.set(target);
            return true;
        }
    }
}
