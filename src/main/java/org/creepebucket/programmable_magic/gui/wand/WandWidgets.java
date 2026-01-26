package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Lifecycle;
import org.creepebucket.programmable_magic.gui.lib.api.widgets.Tickable;
import org.creepebucket.programmable_magic.gui.lib.widgets.InfiniteSupplySlotWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollRegionWidget;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class WandWidgets {
    public static class SpellSupplyWidget extends InfiniteSupplySlotWidget implements Tickable {
        public SyncedValue<Integer> deltaX;
        public SyncedValue<Integer> deltaY;
        public Coordinate delta;
        public Coordinate original;

        public SpellSupplyWidget(ItemStack supplyStack, Coordinate pos, SyncedValue<Integer> deltaX, SyncedValue<Integer> deltaY) {
            super(supplyStack, pos);

            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.delta = new Coordinate((sw, sh) -> deltaX.get(), (sw, sh) -> deltaY.get());
            this.original = pos;
        }

        @Override
        public void tick() {
            pos = original.add(delta);

            super.tick();
        }
    }

    public static class WandSupplyScrollWidget extends ScrollRegionWidget implements Tickable {
        public SyncedValue<Integer> deltaY;
        public boolean enabled = true;

        public WandSupplyScrollWidget(Coordinate pos, int width, int height, int currentValue, int maxValue, int valueMultiplier, SyncedValue<Integer> deltaY) {
            super(pos, width, height, currentValue, maxValue, valueMultiplier);
            this.deltaY = deltaY;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (enabled) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            return false;
        }

        @Override
        public void tick() {
            deltaY.set(-currentValue);
        }
    }
}
