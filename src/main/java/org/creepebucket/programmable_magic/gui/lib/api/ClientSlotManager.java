package org.creepebucket.programmable_magic.gui.lib.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 客户端槽位位置管理：为 {@link Slot} 记录可变的客户端渲染坐标。
 */
public class ClientSlotManager {
    private static final Map<Slot, Pair<Integer, Integer>> slotClientPositions = new WeakHashMap<>();

    /**
     * 获取指定槽位的客户端坐标（若未设置则返回 {@code null}）。
     */
    public static @Nullable Pair<Integer, Integer> getClientPosition(Slot slot) {
        return slotClientPositions.get(slot);
    }

    /**
     * 设置指定槽位的客户端坐标。
     */
    public static void setClientPosition(Slot slot, int x, int y) {
        slotClientPositions.put(slot, new Pair<>(x, y));
    }

    /**
     * 移除指定槽位的客户端坐标记录。
     */
    public static void removeClientPosition(Slot slot) {
        slotClientPositions.remove(slot);
    }

    /**
     * 清空全部槽位的客户端坐标记录。
     */
    public static void clearAll() {
        slotClientPositions.clear();
    }
}
