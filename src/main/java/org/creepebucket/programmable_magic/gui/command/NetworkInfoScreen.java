package org.creepebucket.programmable_magic.gui.command;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.lib.api.*;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.lib.widgets.*;

import java.util.HashMap;
import java.util.Map;

public class NetworkInfoScreen extends Screen<NetworkInfoMenu> {
    public InputBoxWidget box;
    public SearchResultWidget result;

    public NetworkInfoScreen(NetworkInfoMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    public void buildWidget() {
        addWidget(new NumberInputWidget(Coordinate.fromTopRight(-50, 40), Coordinate.fromTopLeft(80, 16), menu.updateInterval, 1, 100).setDepth(1).disableMinMaxButton().rightAlign().tooltip(Component.translatable("gui.programmable_magic.network_info.update_interval_tip")));

        // 搜索框
        box = (InputBoxWidget) addWidget(new InputBoxWidget(Coordinate.fromTopLeft(52, 40), Coordinate.fromTopRight(-224, 16), "", 9999).mainColor(new Color(0)));
        addWidget(new RectangleWidget(Coordinate.fromTopLeft(50, 40), Coordinate.fromTopLeft(2, 16)).color(new Color(0x80FFFFFF)));
        box.box.setHint(Component.translatable("gui.programmable_magic.network_info.search_hint"));
        addWidget(new TextButtonWidget(Coordinate.fromTopRight(-132, 40), Coordinate.fromTopLeft(38, 16), Component.translatable("gui.programmable_magic.network_info.search"), () -> result.setKeyword(box.box.getValue())).rightAlign());

        // 表格
        result = (SearchResultWidget) addWidget(new SearchResultWidget(Coordinate.fromTopLeft(50, 58), Coordinate.fromBottomRight(-100, -98), menu.datas));
        addWidget(new ScrollRegionWidget(Coordinate.fromTopLeft(50, 58), Coordinate.fromBottomRight(-100, -98), Coordinate.fromTopLeft(-10000, 0), 30, result.childDy));

        // 底部装饰
        addWidget(new RectangleWidget(Coordinate.fromBottomLeft(50, -40), Coordinate.fromTopRight(-100, 1)).bottomAlignY());
    }

    public static class SearchResultWidget extends Widget implements Lifecycle {
        public String keyword = "";
        public DynamicValue<Map<Long, Map<String, ModUtils.Mana>>> datas;
        public SmoothedValue childDy = new SmoothedValue(0);

        public SearchResultWidget(Coordinate pos, Coordinate size, DynamicValue<Map<Long, Map<String, ModUtils.Mana>>> datas) {
            super(pos, size);
            this.datas = datas;
            datas.whenFirstDataArrivesDo(this::rebuild);
            smoothedValues.add(childDy);
        }

        public void rebuild() {
            children.clear();
            int c = 0;
            for (long key: datas.get().keySet()) {
                if (keyword != "" && !String.valueOf(key).startsWith(keyword)) continue;
                var child = addChild(new TableElementWidget(Coordinate.fromTopLeft(0, c * 33), Coordinate.fromTopRight(0, 32), key, datas));
                child.addAnimation(new Animation.FadeIn.FromBottom(0.5), c * 0.05);
                child.dy = childDy;
                c++;
            }
        }

        @Override
        public void renderWidget(GuiGraphicsExtractor graphics, int mx, int my, float partialTick, double dt, boolean isForeground) {
            graphics.enableScissor(left(), top(), right(), bottom());
            super.renderWidget(graphics, mx, my, partialTick, dt, isForeground);
            graphics.disableScissor();
        }

        public void setKeyword(String value) {
            keyword = value;
            rebuild();
        }
    }

    public static class TableElementWidget extends Widget implements Lifecycle {
        public DynamicValue<Map<Long, Map<String, ModUtils.Mana>>> datas;
        public long id;

        public TableElementWidget(Coordinate pos, Coordinate size, long id, DynamicValue<Map<Long, Map<String, ModUtils.Mana>>> datas) {
            super(pos, size);
            this.datas = datas;
            this.id = id;
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(Coordinate.fromTopLeft(0, 0), Coordinate.fromBottomRight(0, 0)).color(new Color(0x80000000)));

            // id
            addChild(new TextWidget(Coordinate.fromTopLeft(5, 5), Component.translatable("gui.programmable_magic.network_info.id")).noShadow());
            addChild(new RectangleWidget(Coordinate.fromTopLeft(48, 2), Coordinate.fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));
            addChild(new TextWidget(Coordinate.fromTopLeft(53, 5), Component.literal(String.valueOf(id))).noShadow());

            // 存储
            addChild(new TextWidget(Coordinate.fromCenterTop(5, 5), Component.translatable("gui.programmable_magic.network_info.storage")).noShadow());
            addChild(new RectangleWidget(Coordinate.fromCenterTop(48, 2), Coordinate.fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));

            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52         , 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new ModUtils.Mana()).getRadiation()   * 1000), 7, 1, true).color(new Color(255, 255, 0)));
            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52 + 44    , 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new ModUtils.Mana()).getTemperature() * 1000), 7, 1, true).color(new Color(255, 0  , 0)));
            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52 + 44 * 2, 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new ModUtils.Mana()).getMomentum()    * 1000), 7, 1, true).color(new Color(0, 255, 255)));
            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52 + 44 * 3, 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new ModUtils.Mana()).getPressure()    * 1000), 7, 1, true).color(new Color(0  , 255, 0)));

            // 缓存
            addChild(new TextWidget(Coordinate.fromTopLeft(5, 19), Component.translatable("gui.programmable_magic.network_info.cache")).noShadow());
            addChild(new RectangleWidget(Coordinate.fromTopLeft(48, 17), Coordinate.fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));

            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(52         , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new ModUtils.Mana()).getRadiation()   * 1000), 7, 1, true).color(new Color(255, 255, 0)));
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(52 + 44    , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new ModUtils.Mana()).getTemperature() * 1000), 7, 1, true).color(new Color(255, 0  , 0)));
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(52 + 44 * 2, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new ModUtils.Mana()).getMomentum()    * 1000), 7, 1, true).color(new Color(0, 255, 255)));
            addChild(new NumberDisplayWidget(Coordinate.fromTopLeft(52 + 44 * 3, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new ModUtils.Mana()).getPressure()    * 1000), 7, 1, true).color(new Color(0  , 255, 0)));

            // 功率
            addChild(new TextWidget(Coordinate.fromCenterTop(5, 19), Component.translatable("gui.programmable_magic.network_info.power")).noShadow());
            addChild(new RectangleWidget(Coordinate.fromCenterTop(48, 17), Coordinate.fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));

            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52         , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new ModUtils.Mana()).getRadiation()   * -20000), 7, 1, true).color(new Color(255, 255, 0)));
            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52 + 44    , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new ModUtils.Mana()).getTemperature() * -20000), 7, 1, true).color(new Color(255, 0  , 0)));
            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52 + 44 * 2, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new ModUtils.Mana()).getMomentum()    * -20000), 7, 1, true).color(new Color(0, 255, 255)));
            addChild(new NumberDisplayWidget(Coordinate.fromCenterTop(52 + 44 * 3, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new ModUtils.Mana()).getPressure()    * -20000), 7, 1, true).color(new Color(0  , 255, 0)));
        }
    }
}
