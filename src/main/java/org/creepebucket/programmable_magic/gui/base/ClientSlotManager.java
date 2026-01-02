package org.creepebucket.programmable_magic.gui.base;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.Slot;

import java.util.Map;
import java.util.WeakHashMap;

public class ClientSlotManager {
    private static final Map<Slot, Pair<Integer, Integer>> slotClientPositions = new WeakHashMap<>();

    public static int getClientX(Slot slot) {
        Pair<Integer, Integer> pos = slotClientPositions.get(slot);
        return pos != null ? pos.getFirst() : slot.x;
    }

    public static void setClientX(Slot slot, int value) {
        Pair<Integer, Integer> current = slotClientPositions.get(slot);
        int y = current != null ? current.getSecond() : slot.y;
        slotClientPositions.put(slot, new Pair<>(value, y));
    }

    public static int getClientY(Slot slot) {
        Pair<Integer, Integer> pos = slotClientPositions.get(slot);
        return pos != null ? pos.getSecond() : slot.y;
    }

    public static void setClientY(Slot slot, int value) {
        Pair<Integer, Integer> current = slotClientPositions.get(slot);
        int x = current != null ? current.getFirst() : slot.x;
        slotClientPositions.put(slot, new Pair<>(x, value));
    }

    public static void setClientPosition(Slot slot, int x, int y) {
        slotClientPositions.put(slot, new Pair<>(x, y));
    }

    public static Pair<Integer, Integer> getClientPosition(Slot slot) {
        return slotClientPositions.getOrDefault(slot, new Pair<>(slot.x, slot.y));
    }

    public static void removeClientPosition(Slot slot) {
        slotClientPositions.remove(slot);
    }

    public static void clearAll() {
        slotClientPositions.clear();
    }

}
