package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * 最小菜单：不含槽位与数据同步，仅用于承载 Screen。
 */
public class WandMenu extends AbstractContainerMenu {
    public static final String KEY_GUI_LEFT = "gui_left";
    public static final String KEY_GUI_TOP = "gui_top";
    public static final String KEY_SPELL_OFFSET = "spell_offset";
    private final java.util.Map<String, Object> clientData = new java.util.HashMap<>();
    private final Inventory playerInv;
    private boolean slotsBuilt = false;

    private int guiLeft = 0;
    private int guiTop = 0;
    private int spellIndexOffset = 0;
    private List<Slot> spellSlots;

    public WandMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        this(containerId, playerInv);
    }

    public WandMenu(int containerId, Inventory playerInv) {
        super(ModMenuTypes.WAND_MENU.get(), containerId);
        this.playerInv = playerInv;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }


    public void setClientData(String key, Object value) {
        clientData.put(key, value);

        if (KEY_GUI_LEFT.equals(key)) { this.guiLeft = (Integer) value; }
        else if (KEY_GUI_TOP.equals(key)) { this.guiTop = (Integer) value; }
        else if (KEY_SPELL_OFFSET.equals(key)) { this.spellIndexOffset = (Integer) value; }

        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();
        int scale = win.getGuiScale();

        int centerX = sw / 2;

        // 当两者都有时，添加槽位（避免 Slot.x/y final 的问题）
        if (!slotsBuilt && clientData.containsKey(KEY_GUI_LEFT) && clientData.containsKey(KEY_GUI_TOP)) {
            // 物品栏
            for (int i = 0; i < 36; i++) this.addSlotConverted(playerInv, i, centerX - 162 + (i % 18) * 18, sh + MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 18) * 18);
            // 法术栏
            spellSlots = new ArrayList<>();
            for (int i = 0; i < 25; i++) spellSlots.add(this.addSlotConverted(playerInv, i, centerX - 25 * 8 + i * 16 - 1, sh + MathUtils.SPELL_SLOT_OFFSET));

            slotsBuilt = true;
        }

        // 更新法术槽索引
        if (slotsBuilt && clientData.containsKey(KEY_SPELL_OFFSET)) {
            for (int i = 0; i < 25; i++) { spellSlots.get(i).index = i + spellIndexOffset; spellSlots.get(i).setChanged(); }
            this.slotsChanged(this.playerInv); // 通知容器重算可见内容
            this.broadcastChanges();
        }
    }

    public Slot addSlotConverted(Inventory inv, int index, int screenX, int screenY) {
        int cx = screenX - this.guiLeft;
        int cy = screenY - this.guiTop;
        return this.addSlot(new Slot(inv, index, cx, cy));
    }
}
