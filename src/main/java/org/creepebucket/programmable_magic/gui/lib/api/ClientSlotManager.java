package org.creepebucket.programmable_magic.gui.lib.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class ClientSlotManager {
    private static final Map<Slot, Pair<Integer, Integer>> slotClientPositions = new WeakHashMap<>();

    public static @Nullable Pair<Integer, Integer> getClientPosition(Slot slot) {
        return slotClientPositions.get(slot);
    }

    public static void setClientPosition(Slot slot, int x, int y) {
        slotClientPositions.put(slot, new Pair<>(x, y));
    }

    public static void removeClientPosition(Slot slot) {
        slotClientPositions.remove(slot);
    }

    public static void clearAll() {
        slotClientPositions.clear();
    }

}
