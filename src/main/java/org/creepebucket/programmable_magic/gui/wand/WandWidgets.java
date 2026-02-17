package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.*;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.SlideBarWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.SlotWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.TextWidget;
import org.creepebucket.programmable_magic.gui.wand.wand_plugins.BasePlugin;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.spells.SpellCompiler;
import org.creepebucket.programmable_magic.spells.api.SpellExceptions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.minecraft.util.Mth.hsvToRgb;

public class WandWidgets {
    public static class SpellStorageWidget extends SlotWidget {
        public List<Slot> slots;
        public int i;
        public Hook editHook, deleteHook;
        public List<SpellStorageWidget> storageSlots;
        public SmoothedValue delta2X = new SmoothedValue(0);

        public SpellStorageWidget(List<Slot> slots, Coordinate pos, int i, Hook editHook, Hook deleteHook, List<SpellStorageWidget> storageSlots) {
            super(slots.get(i), pos);
            this.slots = slots;
            this.i = i;
            this.editHook = editHook;
            this.deleteHook = deleteHook;
            this.storageSlots = storageSlots;
            this.originalSize = Coordinate.fromTopLeft(16, 16);

            addChild(new BlankInsertionWidget(Coordinate.fromTopLeft(-8, 16)).mainColor(originalMainColor));
        }

        public void renderNumber(GuiGraphics graphics, int n, int x, int y, int mouseX, int mouseY) {
            // 根据距离计算透明度并显示数字
            double distance = (mouseX - x + 1) * (mouseX - x + 1) + (mouseY - y + 2) * (mouseY - y + 2);
            int renderColor = new Color(mainColor()).toArgbWithAlphaMult(Math.clamp(1000 / distance, 0.1, 1.1));
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
        public int x() {
            double x = (int) (originalPos.toScreenX() + parent.x() + dx.get() % 16 + delta2X.get());

            var allAnimations = new ArrayList<Animation>();
            Widget parent = this;

            while (!(parent instanceof Widget.Root)) {
                allAnimations.addAll(parent.animations);
                parent = parent.parent;
            }

            for (Animation animation : allAnimations) {
                if (animation.isActive()) x += animation.dx;
            }
            return (int) x;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int i = this.i - (int) dx.get() / 16;
            if (0 > i || i >= 1000) return;
            delta2X.doStep(screen.dt);

            // 更新自身Slot
            slot = slots.get(i);

            // 渲染编号
            int count = 0;
            while (i > 0 || count < 3) {
                renderNumber(graphics, i % 10, x() - count * 5 + 11, y() - 5, mouseX, mouseY);
                count++;
                i /= 10;
            }

            graphics.fill(left() + 1, top() + 1, right() - 1, bottom() - 1, bgColor());

            ClientSlotManager.setClientPosition(slot, (int) (x() + dx.get() % 16 + delta2X.get()), y());

            if (this.i == 0 && delta2X.get() + 50 > Minecraft.getInstance().getWindow().getGuiScaledWidth()) {
                deleteHook.trigger(Minecraft.getInstance().keyboardHandler.getClipboard());
                for (SpellStorageWidget widget : storageSlots) widget.delta2X.set(0);
            }
        }

        public static class BlankInsertionWidget extends Widget implements Clickable, Renderable {
            public BlankInsertionWidget(Coordinate pos) {
                super(pos, Coordinate.fromTopLeft(16, 2));

                tooltip(Component.translatable("gui.programmable_magic.wand.spells.insertion"));
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                if (!isInBounds(mouseX, mouseY)) return;
                graphics.fill(left(), top(), right(), bottom(), mainColor());
            }

            @Override
            public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
                // FUCK MOJANG

                var parent = (SpellStorageWidget) this.parent;
                int index = parent.i - (int) parent.dx.current / 16;

                parent.editHook.trigger(index, event.hasShiftDown());

                for (SpellStorageWidget widget : ((WandScreen) screen).storageSlots)
                    if (widget.i >= parent.i)
                        widget.addAnimation(event.hasShiftDown() ? new MoveLeft() : new MoveRight(), 0);
                return true;
            }
        }

        public static class MoveLeft extends Animation {
            public SmoothedValue deltaX;

            public MoveLeft() {
                duration = 2;
                deltaX = new SmoothedValue(16).set(0);
            }

            @Override
            public void step(double dt) {
                deltaX.doStep(dt);
                dx = deltaX.get();
            }
        }

        public static class MoveRight extends Animation {
            public SmoothedValue deltaX;

            public MoveRight() {
                duration = 2;
                deltaX = new SmoothedValue(-16).set(0);
            }

            @Override
            public void step(double dt) {
                deltaX.doStep(dt);
                dx = deltaX.get();
            }
        }
    }

    public static class WandSubCategoryWidget extends Widget implements Renderable {
        public String key;

        public WandSubCategoryWidget(Coordinate pos, String subCategoryKey) {
            super(pos, Coordinate.ZERO);
            this.key = subCategoryKey;


        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            Map<String, Integer> COLOR_MAP = ModUtils.SPELL_COLORS();
            var color = COLOR_MAP.getOrDefault(key, 0xFFFFFFFF);
            color = (color & 16777215) | ((int) (((color >>> 24) * 0.6)) << 24);

            graphics.fill(x(), y() + 4, x() + 79, y() + 6, color);
            graphics.fill(x(), y() + 7, x() + 79, y() + 25, color);
            graphics.fill(x(), y() + 26, x() + 79, y() + 28, color);

            graphics.drawString(ClientUiContext.getFont(), Component.translatable(key), x() + 3, y() + 12, textColor());
        }
    }

    public static class WandSubcategoryJumpButton extends Widget implements Renderable, Clickable {
        public SmoothedValue deltaY;
        public int target, color;

        public WandSubcategoryJumpButton(Coordinate pos, Coordinate size, SmoothedValue deltaY, int target) {
            super(pos, size);
            this.deltaY = deltaY;
            this.target = target;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isInBounds(mouseX, mouseY)) {
                graphics.fill(x(), y(), x() + w(), y() + h(), mainColor());
            } else {
                graphics.fill(x(), y(), x() + w(), y() + h(), originalMainColor.toArgbWithAlphaMult(0.6));
            }
        }

        @Override
        public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
            this.deltaY.set(target);
            return true;
        }
    }

    public static class SpellReleaseWidget extends Widget implements Renderable, Tooltipable, Clickable, Tickable {
        public int chargedTick = 0;
        public boolean isCharging = false;

        public SpellReleaseWidget(Coordinate pos, Coordinate size) {
            super(pos, size);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isCharging)
                graphics.fill(x(), y(), x() + w(), y() + h(), hsvToRgb(chargedTick * 0.01f % 1, 1, .5f) << 8 >>> 8 | 0x80000000);
            else if (isInBounds(mouseX, mouseY))
                graphics.fill(x(), y(), x() + w(), y() + h(), bgColor());
            else
                graphics.fill(x(), y(), x() + w(), y() + h(), mainColor());
        }

        @Override
        public boolean renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
            if (!isInBounds(mouseX, mouseY) && !isCharging) return false;

            Component tooltip;

            if (!isCharging) tooltip = Component.translatable("gui.programmable_magic.wand.release");
            else
                tooltip = Component.literal(ModUtils.FormattedManaString(chargedTick)).withColor(hsvToRgb(chargedTick * 0.01f % 1, 1, 1));

            graphics.renderTooltip(
                    ClientUiContext.getFont(),
                    List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText())),
                    mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null
            );
            return true;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            chargedTick = 0;
            isCharging = false;
            return false;
        }

        @Override
        public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
            isCharging = true;
            return true;
        }

        @Override
        public void tick() {
            if (isCharging) chargedTick++;
        }
    }

    public static class CompileErrorWidget extends Widget implements Renderable, Tickable {
        public List<SpellExceptions> errors = List.of();

        public CompileErrorWidget(Coordinate pos) {
            super(pos, Coordinate.fromTopLeft(0, 0));
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

            var count = 0;
            for (SpellExceptions error : errors) {
                graphics.drawString(ClientUiContext.getFont(), error.message(), x(), y() + count * 16, textColor());
                count++;
            }
        }

        @Override
        public void tick() {
            var compiler = new SpellCompiler();

            compiler.compile(((WandMenu) screen.getMenu()).storedSpells);
            errors = compiler.errors;
        }
    }

    public static class RectangleButtonWidget extends Widget implements Renderable, Clickable {
        public Runnable onPress;

        public RectangleButtonWidget(Coordinate pos, Coordinate size, Runnable onPress) {
            super(pos, size);
            this.onPress = onPress;
        }

        @Override
        public boolean mouseClickedChecked(MouseButtonEvent event, boolean fromMouse) {
            onPress.run();
            return true;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(left(), top(), right(), bottom(), isInBounds(mouseX, mouseY) ? mainColor() : bgColor());
            graphics.fill(x() + w() / 3, y() + h() / 2, x() + w() * 2 / 3, y() + h() / 2 + 1, isInBounds(mouseX, mouseY) ? bgColor() : mainColor());
        }
    }

    public static class WandNotificationWidget extends Widget implements Renderable, Clickable {
        public List<Notification> notifications = new LinkedList<>();

        public WandNotificationWidget(Coordinate pos, Coordinate size) {
            super(pos, size);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean fromMouse) {
            // 通知的点击取消逻辑
            var deleted = false;

            for (Notification notification : notifications) {
                if (isIn(event.x(), event.y(), x(), (int) (y() + notification.dy), w(), 16)) {
                    // 设置其期限为现在 (需要播放删除动画)
                    double now = System.nanoTime() / 1e9;
                    notification.created = now - 1;
                    notification.duration = 1; // 除零
                    deleted = true;
                }
            }

            return deleted;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // 对于每个通知
            var deletedCount = 0;

            for (Notification notification : List.copyOf(notifications).reversed()) {
                // 平滑dy
                notification.speed += (notification.targetDy - notification.dy) * 200 * screen.dt - notification.speed * 30 * screen.dt;
                notification.dy += notification.speed * screen.dt;

                // 检查是否超时
                double now = System.nanoTime() / 1e9;
                if (notification.created + notification.duration < now - .3) {
                    // 在超时0.3秒之后移除
                    notifications.remove(notification);
                    deletedCount++;
                    continue;
                }

                // 计算超时动画的dx
                var dx = now > notification.created + notification.duration ? (now - notification.created - notification.duration) * (now - notification.created - notification.duration) * 1000 : 0;
                // 计算透明度
                var alphaMult = 1 - dx / 90;

                // 渲染通知背景和持续时间提示条
                var x = x() + (int) dx;
                var y = y() + (int) notification.dy;
                var w = ClientUiContext.getFont().width(notification.content) + 8;

                graphics.fill(x, y, x + w, y + 15, (notification.color & 16777215) | ((int) (((notification.color >>> 24) * alphaMult)) << 24));
                graphics.fill(x, y + 14, (int) (x + w * Math.min(1, (now - notification.created) / notification.duration)), y + 15, (16777215) | ((int) (((-1 >>> 24) * alphaMult)) << 24));

                // 渲染文本
                graphics.drawString(ClientUiContext.getFont(), notification.content, x + 2, y + 3, (notification.textColor & 16777215) | ((int) (((notification.textColor >>> 24) * alphaMult)) << 24));

                // 如果有删除, 修改通知targetDy
                notification.targetDy -= deletedCount * 16;
            }
        }

        public void addNotification(int color, Component content, double duration) {
            var n = new Notification();
            n.duration = duration;
            n.created = System.nanoTime() / 1e9;
            n.color = color;
            n.textColor = -1;
            n.content = content;

            // 对于所有通知, 增加其dy
            for (Notification notification : notifications) notification.targetDy += 16;

            notifications.add(n);
        }

        public void addDebug(Component content) {
            addNotification(-2147483647, content, 1);
        }

        public void addInfo(Component content) {
            addNotification(-2147483647, content, 3);
        }

        public void addError(Component content) {
            addNotification(0x80FF0000, content, 5);
        }

        public void addWarning(Component content) {
            addNotification(0x80FFFF00, content, 3);
        }

        public static class Notification {
            public int color, textColor; // duration 秒
            public Component content;
            public double duration, dy = -50, targetDy = 7, created, speed = 0;
        }
    }

    public static class PluginWidget extends SlotWidget {
        public double lastChange = 0;
        public Component name = Component.empty(), function = Component.empty(), lastName = Component.empty(), lastFunction = Component.empty();
        public ItemStack lastStack;
        public BasePlugin lastPlugin;
        public TextWidget nameWidget, functionWidget;

        /**
         * 创建一个槽位控件。
         *
         * @param slot
         * @param pos
         */
        public PluginWidget(Slot slot, Coordinate pos) {
            super(slot, pos);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

            ClientSlotManager.setClientPosition(slot, x(), y() + 1);

            // 检测Slot是否变化
            var now = System.nanoTime() / 1e9;
            if (!slot.getItem().equals(lastStack)) {
                lastChange = now;

                lastName = name;
                lastFunction = function;

                lastStack = slot.getItem();

                if (lastStack.isEmpty()) {
                    name = Component.translatable("gui.programmable_magic.wand.plugin.no_plugin");
                    function = Component.translatable("gui.programmable_magic.wand.plugin.no_plugin_desc");

                } else {
                    name = lastStack.getHoverName();
                }

                if (lastPlugin != null) lastPlugin.onRemove((WandScreen) screen);
                lastPlugin = WandPluginRegistry.Client.getClientLogic(slot.getItem().getItem());
                if (lastPlugin != null) lastPlugin.onAdd((WandScreen) screen);

                if (nameWidget != null) nameWidget.addAnimation(new Animation.FadeOut.ToRight(.3), 0);
                if (nameWidget != null) functionWidget.addAnimation(new Animation.FadeOut.ToRight(.3), .05);

                nameWidget = (TextWidget) addChild(new TextWidget(Coordinate.fromTopLeft(-104, 0), name)
                        .color(originalTextColor).addAnimation(new Animation.FadeIn.FromLeft(.3), .1));
                functionWidget = (TextWidget) addChild(new TextWidget(Coordinate.fromTopLeft(-104, 9), function)
                        .color(originalTextColor).addAnimation(new Animation.FadeIn.FromLeft(.3), .15));
            }

            if (!lastStack.isEmpty())
                functionWidget.text = WandPluginRegistry.Client.getClientLogic(lastStack.getItem()).function();

            graphics.fill(x(), y() + 1, x() + 16, y() + 17, bgColor());
        }
    }

    public static class ColorSelectionWidget extends Widget implements Renderable, Lifecycle {
        public SlideBarWidget r, g, b;
        public RectangleWidget preview;

        public ColorSelectionWidget(Coordinate pos) {
            super(pos, Coordinate.ZERO);
        }

        public Color color() {
            return new Color((int) r.value, (int) g.value, (int) b.value);
        }

        @Override
        public void onInitialize() {
            r = (SlideBarWidget) addChild(new SlideBarWidget(Coordinate.ZERO, Coordinate.fromTopLeft(80, 5), Coordinate.fromTopLeft(0, 255)));
            g = (SlideBarWidget) addChild(new SlideBarWidget(Coordinate.fromTopLeft(0, 6), Coordinate.fromTopLeft(80, 5), Coordinate.fromTopLeft(0, 255)));
            b = (SlideBarWidget) addChild(new SlideBarWidget(Coordinate.fromTopLeft(0, 12), Coordinate.fromTopLeft(80, 5), Coordinate.fromTopLeft(0, 255)));

            preview = (RectangleWidget) addChild(new RectangleWidget(Coordinate.fromTopLeft(83, 0), Coordinate.fromTopLeft(17, 17)));
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            r.background.color(new Color(0, (int) g.value, (int) b.value), new Color(255, (int) g.value, (int) b.value));
            g.background.color(new Color((int) r.value, 0, (int) b.value), new Color((int) r.value, 255, (int) b.value));
            b.background.color(new Color((int) r.value, (int) g.value, 0), new Color((int) r.value, (int) g.value, 255));

            preview.color(color());
        }
    }
}
