package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.SlotWidget;

import java.util.List;
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

    public static class SpellStorageWidget extends SlotWidget {
        public SyncedValue<Integer> deltaX;
        public Coordinate delta;
        public Coordinate original;
        public SyncedValue<Integer> deltaI;
        public List<Slot> slots;
        public int i;

        public SpellStorageWidget(List<Slot> slots, Coordinate pos, SyncedValue<Integer> deltaX, int i, SyncedValue<Integer> deltaI) {
            super(slots.get(i + deltaI.get()), pos);

            this.deltaX = deltaX;
            this.deltaI = deltaI;
            this.slots = slots;
            this.i = i;
            this.delta = new Coordinate((sw, sh) -> deltaX.get() % 16, (sw, sh) -> 0);
            this.original = pos;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            pos = original.add(delta);

            if ((0 > i + deltaI.get()) || (i + deltaI.get() >= 1024)) return;

            super.render(graphics, mouseX, mouseY, partialTick);
            this.slot = slots.get(i + deltaI.get());

            graphics.fill(pos.toScreenX() + 1, pos.toScreenY() + 1, pos.toScreenX() + 15, pos.toScreenY() + 15, -2147483648);
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

    public static class WandSubcategoryJumpButton extends ImageButtonWidget {
        public SyncedValue<Integer> deltaY;
        public int target, color;

        public WandSubcategoryJumpButton(Coordinate pos, Coordinate size, SyncedValue<Integer> deltaY, int target, Component tooltip, int color) {
            super(pos, size, null, null, () -> {}, tooltip);
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
