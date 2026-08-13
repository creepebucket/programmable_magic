package org.creepebucket.arcanism.gui.command;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.arcanism.gui.lib.api.*;
import org.creepebucket.arcanism.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.arcanism.gui.lib.ui.Screen;
import org.creepebucket.arcanism.gui.lib.widgets.*;
import org.creepebucket.arcanism.utils.Mana;

import java.util.HashMap;
import java.util.Map;


import static org.creepebucket.arcanism.gui.lib.api.Coordinate.*;
import static net.minecraft.network.chat.Component.literal;

public class NetworkInfoScreen extends Screen<NetworkInfoMenu> {
    public InputBoxWidget box;
    public SearchResultWidget result;

    public NetworkInfoScreen(NetworkInfoMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    public void buildWidget() {
        addWidget(new NumberInputWidget(fromTopRight(-50, 40), fromTopLeft(80, 16), menu.updateInterval, 1, 100).setDepth(1).disableMinMaxButton().rightAlign().tooltip(Component.translatable("gui.arcanism.network_info.update_interval_tip")));

        // 搜索框
        box = (InputBoxWidget) addWidget(new InputBoxWidget(fromTopLeft(52, 40), fromTopRight(-224, 16), "", 9999).mainColor(new Color(0)));
        addWidget(new RectangleWidget(fromTopLeft(50, 40), fromTopLeft(2, 16)).color(new Color(0x80FFFFFF)));
        box.box.setHint(Component.translatable("gui.arcanism.network_info.search_hint"));
        addWidget(new TextButtonWidget(fromTopRight(-132, 40), fromTopLeft(38, 16), Component.translatable("gui.arcanism.network_info.search"), () -> result.setKeyword(box.box.getValue())).rightAlign());

        // 表格
        result = (SearchResultWidget) addWidget(new SearchResultWidget(fromTopLeft(50, 58), fromBottomRight(-100, -98), menu.datas));
        addWidget(new ScrollRegionWidget(fromTopLeft(50, 58), fromBottomRight(-100, -98), fromTopLeft(-10000, 0), 30, result.childDy));

        // 底部装饰
        addWidget(new RectangleWidget(fromBottomLeft(50, -40), fromTopRight(-100, 1)).bottomAlignY());
    }

    public static class SearchResultWidget extends Widget implements Lifecycle {
        public String keyword = "";
        public DynamicValue<Map<Long, Map<String, Mana>>> datas;
        public SmoothedValue childDy = new SmoothedValue(0);

        public SearchResultWidget(Coordinate pos, Coordinate size, DynamicValue<Map<Long, Map<String, Mana>>> datas) {
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
                var child = addChild(new TableElementWidget(fromTopLeft(0, c * 33), fromTopRight(0, 32), key, datas));
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
        public DynamicValue<Map<Long, Map<String, Mana>>> datas;
        public long id;

        public TableElementWidget(Coordinate pos, Coordinate size, long id, DynamicValue<Map<Long, Map<String, Mana>>> datas) {
            super(pos, size);
            this.datas = datas;
            this.id = id;
        }

        @Override
        public void onInitialize() {
            addChild(new RectangleWidget(fromTopLeft(0, 0), fromBottomRight(0, 0)).color(new Color(0x80000000)));

            // id
            addChild(new TextWidget(fromTopLeft(5, 5), Component.translatable("gui.arcanism.network_info.id")).noShadow());
            addChild(new RectangleWidget(fromTopLeft(48, 2), fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));
            addChild(new TextWidget(fromTopLeft(53, 5), literal(String.valueOf(id))).noShadow());

            // 存储
            addChild(new TextWidget(fromCenterTop(5, 5), Component.translatable("gui.arcanism.network_info.storage")).noShadow());
            addChild(new RectangleWidget(fromCenterTop(48, 2), fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));

            addChild(new NumberDisplayWidget(fromCenterTop(52         , 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new Mana()).getRadiation()), 7, 1, true).color(new Color(255, 255, 0)));
            addChild(new NumberDisplayWidget(fromCenterTop(52 + 44    , 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new Mana()).getTemperature()), 7, 1, true).color(new Color(255, 0  , 0)));
            addChild(new NumberDisplayWidget(fromCenterTop(52 + 44 * 2, 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new Mana()).getMomentum()), 7, 1, true).color(new Color(0, 255, 255)));
            addChild(new NumberDisplayWidget(fromCenterTop(52 + 44 * 3, 4), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("current", new Mana()).getPressure()), 7, 1, true).color(new Color(0  , 255, 0)));

            // 缓存
            addChild(new TextWidget(fromTopLeft(5, 19), Component.translatable("gui.arcanism.network_info.cache")).noShadow());
            addChild(new RectangleWidget(fromTopLeft(48, 17), fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));

            addChild(new NumberDisplayWidget(fromTopLeft(52         , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new Mana()).getRadiation()), 7, 1, true).color(new Color(255, 255, 0)));
            addChild(new NumberDisplayWidget(fromTopLeft(52 + 44    , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new Mana()).getTemperature()), 7, 1, true).color(new Color(255, 0  , 0)));
            addChild(new NumberDisplayWidget(fromTopLeft(52 + 44 * 2, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new Mana()).getMomentum()), 7, 1, true).color(new Color(0, 255, 255)));
            addChild(new NumberDisplayWidget(fromTopLeft(52 + 44 * 3, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("cache", new Mana()).getPressure()), 7, 1, true).color(new Color(0  , 255, 0)));

            // 功率
            addChild(new TextWidget(fromCenterTop(5, 19), Component.translatable("gui.arcanism.network_info.power")).noShadow());
            addChild(new RectangleWidget(fromCenterTop(48, 17), fromTopLeft(2, 13)).color(new Color(0x80FFFFFF)));

            addChild(new NumberDisplayWidget(fromCenterTop(52         , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new Mana()).getRadiation()   * -20), 7, 1, true).color(new Color(255, 255, 0)));
            addChild(new NumberDisplayWidget(fromCenterTop(52 + 44    , 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new Mana()).getTemperature() * -20), 7, 1, true).color(new Color(255, 0  , 0)));
            addChild(new NumberDisplayWidget(fromCenterTop(52 + 44 * 2, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new Mana()).getMomentum()    * -20), 7, 1, true).color(new Color(0, 255, 255)));
            addChild(new NumberDisplayWidget(fromCenterTop(52 + 44 * 3, 19), DynamicValue.fromSupplier(() -> datas.get().getOrDefault(id, new HashMap<>()).getOrDefault("load", new Mana()).getPressure()    * -20), 7, 1, true).color(new Color(0  , 255, 0)));
        }
    }
}
