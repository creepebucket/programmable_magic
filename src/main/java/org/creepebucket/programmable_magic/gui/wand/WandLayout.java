package org.creepebucket.programmable_magic.gui.wand;

public class WandLayout {
    public static int SPELL_SLOT_OFFSET = -80; // 法术槽相对屏幕底部的偏移
    public static int INVENTORY_OFFSET = -60;  // 物品栏相对屏幕底部的偏移

    public static int visible_spell_slots(int screen_width) {
        return Math.max(1, Math.floorDiv(screen_width - 200, 16) - 4);
    }

    public static boolean compact_mode(int screen_width) {
        return visible_spell_slots(screen_width) <= 16;
    }

    public static int compact_mode_y_offset(int screen_width) {
        return compact_mode(screen_width) ? 18 : 0;
    }

    public static int inventory_slot_x(int screen_width, int slot_index) {
        int center_x = screen_width / 2;
        boolean compact_mode = compact_mode(screen_width);
        if (!compact_mode) return center_x - 162 + (slot_index % 18) * 18;
        if (slot_index >= 9) return center_x - 82 + (slot_index % 9) * 18;
        return center_x - 88 + (slot_index % 9) * 20;
    }

    public static int inventory_slot_y(int screen_width, int screen_height, int slot_index) {
        boolean compact_mode = compact_mode(screen_width);
        if (!compact_mode) return screen_height + INVENTORY_OFFSET + Math.floorDiv(slot_index, 18) * 18;
        if (slot_index >= 9) return screen_height + INVENTORY_OFFSET + Math.floorDiv(slot_index, 9) * 18 - 36;
        return screen_height - 19;
    }

    public static int inventory_bg_x(int screen_width, int slot_index) {
        return inventory_slot_x(screen_width, slot_index);
    }

    public static int inventory_bg_y(int screen_width, int screen_height, int slot_index) {
        int y = inventory_slot_y(screen_width, screen_height, slot_index);
        if (compact_mode(screen_width) && slot_index >= 9) return y + compact_mode_y_offset(screen_width) - 18;
        return y;
    }

    public static int plugin_slot_x(int screen_width) {
        return screen_width - 24;
    }

    public static int plugin_slot_y(int slot_index) {
        return 10 + slot_index * 18;
    }

    public static int spell_slot_x(int screen_width, int visible_index) {
        int center_x = screen_width / 2;
        int visible = visible_spell_slots(screen_width);
        return center_x - visible * 8 + visible_index * 16 - 1;
    }

    public static int spell_slot_y(int screen_width, int screen_height) {
        return screen_height + SPELL_SLOT_OFFSET - compact_mode_y_offset(screen_width);
    }

    public static int supply_slot_x(int col) {
        return 19 + col * 16 - 1;
    }

    public static int supply_slot_y(int row) {
        return 4 + row * 16;
    }
}
