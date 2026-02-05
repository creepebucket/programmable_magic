package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Clickable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tooltipable;
import org.creepebucket.programmable_magic.gui.lib.widgets.ImageButtonWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollRegionWidget;
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

    public static class SpellStorageWidget extends SlotWidget implements Tooltipable, Clickable {
        public Coordinate original;
        public List<Slot> slots;
        public int i;
        public double deltaX = -400;
        public double delta2X = 0;
        public SyncedValue<Integer> target;
        public Hook editHook, deleteHook;
        public List<SpellStorageWidget> storageSlots;
        public double speed = 1000;
        public double speed2 = 0;
        public double acc2 = 0;

        public SpellStorageWidget(List<Slot> slots, Coordinate pos, int i, Hook editHook, Hook deleteHook, List<SpellStorageWidget> storageSlots, SyncedValue<Integer> target) {
            super(slots.get(i), pos);
            this.slots = slots;
            this.i = i;
            this.original = pos;
            this.editHook = editHook;
            this.deleteHook = deleteHook;
            this.storageSlots = storageSlots;
            this.target = target;
            this.size = Coordinate.fromTopLeft(16, 16);
        }

        public void renderNumber(GuiGraphics graphics, int n, int x, int y, int mouseX, int mouseY) {
            // 根据距离计算透明度并显示数字
            double distance = (mouseX - x + 1) * (mouseX - x + 1) + (mouseY - y + 2) * (mouseY - y + 2);
            int renderColor = 16777215 | ((int) ((255 * (Math.clamp(1000 / distance, 0.1, 1.1) - 0.1))) << 24);
            if (renderColor >>> 24 == 0) return;

            switch (n) {
                case 0 -> {
                    graphics.renderOutline(x, y, 4, 5, renderColor);
                }
                case 1 -> {
                    graphics.fill(x, y + 1, x + 2, y + 2, renderColor);
                    graphics.fill(x + 2, y, x + 3, y + 5, renderColor);
                    graphics.fill(x, y + 4, x + 4, y + 5, renderColor);
                }
                case 2 -> {
                    graphics.fill(x, y, x + 4, y + 1, renderColor);
                    graphics.fill(x + 3, y + 1, x + 4, y + 2, renderColor);
                    graphics.fill(x + 2, y + 2, x + 3, y + 3, renderColor);
                    graphics.fill(x + 1, y + 3, x + 2, y + 4, renderColor);
                    graphics.fill(x, y + 4, x + 4, y + 5, renderColor);
                }
                case 3 -> {
                    graphics.fill(x, y, x + 4, y + 1, renderColor);
                    graphics.fill(x + 3, y + 1, x + 4, y + 2, renderColor);
                    graphics.fill(x + 2, y + 2, x + 3, y + 3, renderColor);
                    graphics.fill(x + 3, y + 3, x + 4, y + 4, renderColor);
                    graphics.fill(x, y + 4, x + 4, y + 5, renderColor);
                }
                case 4 -> {
                    graphics.fill(x + 2, y, x + 4, y + 1, renderColor);
                    graphics.fill(x + 1, y + 1, x + 2, y + 2, renderColor);
                    graphics.fill(x, y + 2, x + 1, y + 4, renderColor);
                    graphics.fill(x + 3, y + 2, x + 4, y + 5, renderColor);
                    graphics.fill(x + 1, y + 3, x + 3, y + 4, renderColor);
                }
                case 5 -> {
                    graphics.fill(x, y, x + 4, y + 1, renderColor);
                    graphics.fill(x, y + 1, x + 1, y + 2, renderColor);
                    graphics.fill(x + 1, y + 2, x + 3, y + 3, renderColor);
                    graphics.fill(x + 3, y + 3, x + 4, y + 4, renderColor);
                    graphics.fill(x, y + 4, x + 3, y + 5, renderColor);
                }
                case 6 -> {
                    graphics.fill(x + 1, y, x + 4, y + 1, renderColor);
                    graphics.fill(x, y + 1, x + 1, y + 4, renderColor);
                    graphics.fill(x + 1, y + 4, x + 4, y + 5, renderColor);
                    graphics.fill(x + 1, y + 2, x + 4, y + 3, renderColor);
                    graphics.fill(x + 3, y + 3, x + 4, y + 4, renderColor);
                }
                case 7 -> {
                    graphics.fill(x, y, x + 4, y + 1, renderColor);
                    graphics.fill(x + 3, y + 1, x + 4, y + 2, renderColor);
                    graphics.fill(x + 2, y + 2, x + 3, y + 3, renderColor);
                    graphics.fill(x + 1, y + 3, x + 2, y + 5, renderColor);
                }
                case 8 -> {
                    graphics.fill(x, y, x + 4, y + 1, renderColor);
                    graphics.fill(x, y + 1, x + 1, y + 2, renderColor);
                    graphics.fill(x + 3, y + 1, x + 4, y + 2, renderColor);
                    graphics.fill(x + 1, y + 2, x + 3, y + 3, renderColor);
                    graphics.fill(x, y + 3, x + 1, y + 4, renderColor);
                    graphics.fill(x + 3, y + 3, x + 4, y + 4, renderColor);
                    graphics.fill(x, y + 4, x + 4, y + 5, renderColor);
                }
                case 9 -> {
                    graphics.fill(x, y, x + 3, y + 1, renderColor);
                    graphics.fill(x, y + 1, x + 1, y + 2, renderColor);
                    graphics.fill(x + 1, y + 2, x + 3, y + 3, renderColor);
                    graphics.fill(x, y + 4, x + 3, y + 5, renderColor);
                    graphics.fill(x + 3, y, x + 4, y + 4, renderColor);
                }
            }
        }

        @Override
        public boolean renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
            if (!isInBounds(mouseX, mouseY, pos.toScreenX() - 8, pos.toScreenY() + 16, 16, 2)) return false;

            graphics.renderTooltip(ClientUiContext.getFont(),
                    List.of(ClientTooltipComponent.create(Component.translatable("gui.programmable_magic.wand.spells.insertion").getVisualOrderText())),
                    mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
            return true;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            if (!isInBounds(event.x(), event.y(), pos.toScreenX() - 8, pos.toScreenY() + 16, 16, 2)) return false;

            // FUCK MOJANG

            int index = this.i - (int) deltaX / 16;
            if (0 > index || index >= 1024) return false;

            editHook.trigger(index, event.hasShiftDown());

            // 处理动画
            for (int j = i; j < storageSlots.size(); j++) storageSlots.get(j).delta2X += event.hasShiftDown()? 16 : -16; // 不用dx dx会被取模

            return true;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

            int i = this.i - (int) deltaX / 16;
            if (0 > i || i >= 1000) return;

            pos = original.add(Coordinate.fromTopLeft((int) (deltaX % 16) + this.i * 16 + (int) delta2X, 0));

            // 平滑dx
            double dt = WandScreen.dt;

            speed += ((double) target.get() - deltaX) * 200 * dt - speed * 30 * dt;
            deltaX += speed * dt;

            // d2x用于插入/删除动画, target始终为0
            speed2 += -delta2X * 200 * dt - speed2 * 30 * dt + acc2;
            delta2X += speed2 * dt;
            acc2 = Math.max(acc2 - 100 * dt, 0); // acc2 用于删除动画

            // 删除整个法术序列的条件 只能被acc2触发
            if (this.i == 0 && original.toScreenX() + delta2X > Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                deleteHook.trigger();
                for (SpellStorageWidget widget: storageSlots) widget.acc2 = 0;
            }

            // 渲染格子

            graphics.fill(pos.toScreenX() + 1, pos.toScreenY() + 1, pos.toScreenX() + 15, pos.toScreenY() + 15, -2147483648);

            // 更新自身Slot
            slot = slots.get(i);

            // 渲染编号
            int count = 0;
            while (i > 0 || count < 3) {
                renderNumber(graphics, i % 10, pos.toScreenX() - count * 5 + 11, pos.toScreenY() - 5, mouseX, mouseY);
                count++;
                i /= 10;
            }

            // 渲染空格插入按钮
            if(isInBounds(mouseX, mouseY, pos.toScreenX() - 8, pos.toScreenY() + 16, 16, 2))
                graphics.fill(pos.toScreenX() - 8, pos.toScreenY() + 16, pos.toScreenX() + 8, pos.toScreenY() + 18, -1);

            super.render(graphics, mouseX, mouseY, partialTick);

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
