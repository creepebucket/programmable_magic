package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.MouseScrollable;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Renderable;
import org.creepebucket.programmable_magic.gui.lib.widgets.InfiniteSupplySlotWidget;

import java.util.HashMap;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandWidgets {
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
        public Map<String, Integer> COLOR_MAP = new HashMap<>();

        public WandSubCategoryWidget(Coordinate pos, String subCategoryKey, SyncedValue<Integer> deltaY) {
            this.pos = pos;
            this.key = subCategoryKey;
            this.deltaY = deltaY;

            COLOR_MAP.put("spell." + MODID + ".subcategory.visual", 0x80C832A1);
            COLOR_MAP.put("spell." + MODID + ".subcategory.entity", 0x80C82C59);
            COLOR_MAP.put("spell." + MODID + ".subcategory.block", 0x80EB3838);
            COLOR_MAP.put("spell." + MODID + ".subcategory.trigger", 0x80C8702C);
            COLOR_MAP.put("spell." + MODID + ".subcategory.structure", 0x80C8902C);
            COLOR_MAP.put("spell." + MODID + ".subcategory.flow_control", 0x80C8B32C);
            COLOR_MAP.put("spell." + MODID + ".subcategory.dynamic_constant.number", 0x809FE333);
            COLOR_MAP.put("spell." + MODID + ".subcategory.constants.number", 0x805DEE22);
            COLOR_MAP.put("spell." + MODID + ".subcategory.operations.number", 0x8031FF7E);
            COLOR_MAP.put("spell." + MODID + ".subcategory.dynamic_constant.vector", 0x803AFFED);
            COLOR_MAP.put("spell." + MODID + ".subcategory.constants.vector", 0x802DCDFF);
            COLOR_MAP.put("spell." + MODID + ".subcategory.operations.vector", 0x803498FF);
            COLOR_MAP.put("spell." + MODID + ".subcategory.operations.boolean", 0x80424EF9);
            COLOR_MAP.put("spell." + MODID + ".subcategory.constants.boolean", 0x807747F0);
            COLOR_MAP.put("spell." + MODID + ".subcategory.dynamic_constant.entity", 0x808F21FF);
            COLOR_MAP.put("spell." + MODID + ".subcategory.operations.block", 0x80B53EDF);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = pos.toScreenX();
            int y = pos.toScreenY() + deltaY.get();

            graphics.fill(x, y+4, x+79, y+6, COLOR_MAP.getOrDefault(key, 0x80FFFFFF));
            graphics.fill(x, y+7, x+79, y+25, COLOR_MAP.getOrDefault(key, 0x80FFFFFF));
            graphics.fill(x, y+26, x+79, y+28, COLOR_MAP.getOrDefault(key, 0x80FFFFFF));

            graphics.drawString(ClientUiContext.getFont(), Component.translatable(key), x+3, y+12, 0x80FFFFFF);
        }
    }
}
