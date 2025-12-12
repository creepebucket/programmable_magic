package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.items.wand.BaseWand;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 最小菜单：不含槽位与数据同步，仅用于承载 Screen。
 */
public class WandMenu extends AbstractContainerMenu {
    public static final String KEY_GUI_LEFT = "gui_left";
    public static final String KEY_GUI_TOP = "gui_top";
    public static final String KEY_SPELL_OFFSET = "spell_offset";
    public static final String KEY_CLEAN = "clean";
    public static final String KEY_SPELL_SIDEBAR = "spell_sidebar";
    public static final String KEY_SUPPLY_SCROLL = "spell_sidebar_scroll";
    private final java.util.Map<String, Object> clientData = new java.util.HashMap<>();
    private final Inventory playerInv;
    private boolean slotsBuilt = false;

    private int guiLeft = 0;
    private int guiTop = 0;
    private int spellIndexOffset = 0;
    private List<Slot> spellSlots;
    private int spellStartIndex = -1;
    private int spellEndIndex = -1;
    private String selectedSidebar = "compute";

    private final SimpleContainer wandInv;
    private final InteractionHand wandHand;
    private final SimpleContainer supplyInv = new SimpleContainer(0);
    private final java.util.List<SupplySlot> supplySlots = new java.util.ArrayList<>();
    private java.util.List<ItemStack> supplyItems = new java.util.ArrayList<>();
    private int supplyScrollRow = 0;
    

    public WandMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        super(ModMenuTypes.WAND_MENU.get(), containerId);
        this.playerInv = playerInv;
        int ord = 0;
        try { ord = extra.readVarInt(); } catch (Exception ignored) {}
        this.wandHand = (ord >= 0 && ord < InteractionHand.values().length) ? InteractionHand.values()[ord] : InteractionHand.MAIN_HAND;
        this.wandInv = new SimpleContainer(resolveWandSlots());
        loadWandInvFromStack();
    }

    public WandMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, InteractionHand.MAIN_HAND);
    }

    public WandMenu(int containerId, Inventory playerInv, InteractionHand hand) {
        super(ModMenuTypes.WAND_MENU.get(), containerId);
        this.playerInv = playerInv;
        this.wandHand = hand;
        this.wandInv = new SimpleContainer(resolveWandSlots());
        loadWandInvFromStack();
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack in = slot.getItem();
            ret = in.copy();

            if (slot instanceof OffsetSlot) {
                // Shift 删除：来自法术栏的条目直接清空该法术槽
                slot.set(ItemStack.EMPTY);
            } else {
                int start = this.spellStartIndex >= 0 ? this.spellStartIndex : 36;
                int end = this.spellEndIndex >= 0 ? this.spellEndIndex : this.slots.size();
                if (!this.moveItemStackTo(in, start, end, false)) return ItemStack.EMPTY;
            }

            if (in.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return ret;
    }


    public void setClientData(String key, Object value) {
        clientData.put(key, value);

        if (KEY_GUI_LEFT.equals(key)) { this.guiLeft = (Integer) value; if (slotsBuilt) refreshSupplySlots(true); }
        else if (KEY_GUI_TOP.equals(key)) { this.guiTop = (Integer) value; if (slotsBuilt) refreshSupplySlots(true); }
        else if (KEY_SPELL_OFFSET.equals(key)) {
            int visible = (this.spellSlots != null) ? this.spellSlots.size() : 25;
            int maxOffset = Math.max(0, this.wandInv.getContainerSize() - visible);
            this.spellIndexOffset = Mth.clamp((Integer) value, 0, maxOffset);
        } else if (KEY_CLEAN.equals(key)) {
            int n = this.wandInv.getContainerSize();
            for (int i = 0; i < n; i++) this.wandInv.setItem(i, ItemStack.EMPTY);
            this.slotsChanged(this.wandInv);
            this.broadcastChanges();
            saveWandInvToStack(this.playerInv.player);
        } else if (KEY_SPELL_SIDEBAR.equals(key)) {
            this.selectedSidebar = (String) value;
            this.supplyScrollRow = 0;
            if (slotsBuilt) refreshSupplySlots(true);
        } else if (KEY_SUPPLY_SCROLL.equals(key)) {
            try { this.supplyScrollRow = (Integer) value; } catch (Exception ignored) {}
            if (slotsBuilt) refreshSupplySlots(true);
        }

        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();
        int scale = win.getGuiScale();

        int centerX = sw / 2;

        // 当两者都有时，添加槽位（避免 Slot.x/y final 的问题）
        if (!slotsBuilt && clientData.containsKey(KEY_GUI_LEFT) && clientData.containsKey(KEY_GUI_TOP)) {
            int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
            boolean compactMode = spellSlotCount <= 16;

            // 物品栏
            for (int i = 0; i < 36; i++) {
                if (!compactMode) {
                    this.addSlotConverted(playerInv, i, centerX - 162 + (i % 18) * 18, sh + MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 18) * 18);
                } else {
                    if (i >= 9) {
                        this.addSlotConverted(playerInv, i, centerX - 82 + (i % 9) * 18, sh + MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 9) * 18 - 36);
                    } else {
                        this.addSlotConverted(playerInv, i, centerX - 88 + (i % 9) * 20, sh - 19);
                    }
                }
            }

            // 法术栏（独立容器 + 偏移映射）
            spellSlots = new ArrayList<>();
            this.spellStartIndex = this.slots.size();
            for (int i = 0; i < spellSlotCount; i++) spellSlots.add(this.addOffsetSlotConverted(wandInv, i, centerX - spellSlotCount * 8 + i * 16 - 1, sh + MathUtils.SPELL_SLOT_OFFSET - (compactMode ? 18 : 0)));
            this.spellEndIndex = this.slots.size();

            // 左侧法术供应栏（无限供应）：按当前侧栏分类与子类别布局生成
            buildInitialSupplySlots();

            slotsBuilt = true;
        }

        // 偏移变化：OffsetSlot 动态映射，无需改 Slot.index
        if (slotsBuilt && clientData.containsKey(KEY_SPELL_OFFSET)) {
            this.slotsChanged(this.wandInv);
            this.broadcastChanges();
        }
    }

    public Slot addSlotConverted(Inventory inv, int index, int screenX, int screenY) {
        int cx = screenX - this.guiLeft;
        int cy = screenY - this.guiTop;
        return this.addSlot(new Slot(inv, index, cx, cy));
    }

    public Slot addOffsetSlotConverted(Container inv, int baseIndex, int screenX, int screenY) {
        int cx = screenX - this.guiLeft;
        int cy = screenY - this.guiTop;
        return this.addSlot(new OffsetSlot(inv, baseIndex, cx, cy));
    }

    private class OffsetSlot extends Slot {
        private final Container inv;
        private final int baseIndex;

        public OffsetSlot(Container inv, int baseIndex, int x, int y) {
            super(inv, 0, x, y);
            this.inv = inv;
            this.baseIndex = baseIndex;
        }

        private int targetIndex() { return baseIndex + spellIndexOffset; }

        private boolean inRange(int idx) { return idx >= 0 && idx < inv.getContainerSize(); }

        @Override
        public boolean hasItem() {
            int idx = targetIndex();
            return inRange(idx) && !inv.getItem(idx).isEmpty();
        }

        @Override
        public ItemStack getItem() {
            int idx = targetIndex();
            return inRange(idx) ? inv.getItem(idx) : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            int idx = targetIndex();
            if (inRange(idx)) inv.setItem(idx, stack);
            setChanged();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            int idx = targetIndex();
            if (!inRange(idx)) return false;
            var item = stack.getItem();
            boolean isSpell = SpellRegistry.isSpell(item);
            boolean isPlaceholder = item instanceof WandItemPlaceholder;
            return (isSpell || isPlaceholder) && this.container.canPlaceItem(idx, stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            int idx = targetIndex();
            return inRange(idx) && super.mayPickup(player);
        }

        @Override
        public ItemStack remove(int amount) {
            int idx = targetIndex();
            return inRange(idx) ? inv.removeItem(idx, amount) : ItemStack.EMPTY;
        }

        @Override
        public void setChanged() { this.inv.setChanged(); }
    }

    public int getSpellSlotCapacity() { return this.wandInv.getContainerSize(); }

    private int resolveWandSlots() {
        ItemStack st = getWandStack();
        if (st != null && st.getItem() instanceof BaseWand bw) return bw.getSlots();
        return 25;
    }

    private ItemStack getWandStack() {
        var p = this.playerInv.player;
        return this.wandHand == InteractionHand.OFF_HAND ? p.getOffhandItem() : p.getMainHandItem();
    }

    private void loadWandInvFromStack() {
        ItemStack st = getWandStack();
        List<ItemStack> saved = st != null ? st.get(ModDataComponents.WAND_STACKS_SMALL.get()) : null;
        int n = this.wandInv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack it = (saved != null && i < saved.size()) ? saved.get(i) : ItemStack.EMPTY;
            this.wandInv.setItem(i, it);
        }
    }

    private void saveWandInvToStack(Player player) {
        if (player.level().isClientSide) return;
        ItemStack st = getWandStack();
        if (st == null || st.isEmpty()) return;
        int n = this.wandInv.getContainerSize();
        var list = new ArrayList<ItemStack>(n);
        for (int i = 0; i < n; i++) if (!this.wandInv.getItem(i).isEmpty()) list.add(this.wandInv.getItem(i));
        st.set(ModDataComponents.WAND_STACKS_SMALL.get(), list);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        saveWandInvToStack(player);
    }

    // 计算当前侧栏应展示的法术清单（按子类别分组后扁平化，保持视觉顺序）并生成初始供给槽位
    private void buildInitialSupplySlots() {
        supplyItems = computeSupplyItemsForCurrentSidebar();

        var win = Minecraft.getInstance().getWindow();
        int sh = win.getGuiScaledHeight();

        int startX = 19; // 屏幕坐标（随后减去 gui_left）
        int startY = 10;

        int x = startX;
        int y = startY;
        int idx = 0;
        for (var entry : SpellUtils.getSpellsGroupedBySubCategory(
                SpellUtils.stringSpellTypeMap.getOrDefault(this.selectedSidebar,
                        SpellItemLogic.SpellType.COMPUTE_MOD)
        ).entrySet()) {
            java.util.List<ItemStack> list = entry.getValue();
            int n = list.size();
            for (int i = 0; i < n; i++) {
                int screenX = x + (i % 5) * 16 - 1;
                int screenY = y + 10 + Math.floorDiv(i, 5) * 16 - (this.supplyScrollRow * 16);
                var slot = addSupplySlotConverted(idx, screenX, screenY);
                slot.setActive(true);
                supplySlots.add(slot);
                idx++;
            }
            y += Math.floorDiv(n - 1, 5) * 16 + 32;
        }
    }

    private java.util.List<ItemStack> computeSupplyItemsForCurrentSidebar() {
        var type = SpellUtils.stringSpellTypeMap.getOrDefault(this.selectedSidebar,
                SpellItemLogic.SpellType.COMPUTE_MOD);
        var map = SpellUtils.getSpellsGroupedBySubCategory(type);
        var flat = new java.util.ArrayList<ItemStack>();
        for (var e : map.entrySet()) flat.addAll(e.getValue());
        return flat;
    }

    private void refreshSupplySlots(boolean rebuildLayout) {
        // 重建分组布局（Slot 坐标不可变更，滚动时需要再生成一批新的槽位并禁用旧槽位）
        supplyItems = computeSupplyItemsForCurrentSidebar();
        for (var s : supplySlots) s.setActive(false);
        supplySlots.clear();
        buildInitialSupplySlots();
        this.slotsChanged(this.supplyInv);
        this.broadcastChanges();
    }

    private SupplySlot addSupplySlotConverted(int supplyIndex, int screenX, int screenY) {
        int cx = screenX - this.guiLeft;
        int cy = screenY - this.guiTop;
        var s = new SupplySlot(this.supplyInv, 0, cx, cy, supplyIndex);
        return (SupplySlot) this.addSlot(s);
    }

    private class SupplySlot extends Slot {
        private int supplyIndex;
        private boolean active = false;

        public SupplySlot(Container inv, int index, int x, int y, int supplyIndex) {
            super(inv, index, x, y);
            this.supplyIndex = supplyIndex;
        }

        public void setActive(boolean v) { this.active = v; }
        public void setSupplyIndex(int idx) { this.supplyIndex = idx; }

        @Override
        public boolean isActive() { return active; }

        private ItemStack supplyItem() {
            if (!active) return ItemStack.EMPTY;
            if (supplyIndex < 0 || supplyIndex >= supplyItems.size()) return ItemStack.EMPTY;
            ItemStack src = supplyItems.get(supplyIndex);
            if (src == null || src.isEmpty()) return ItemStack.EMPTY;
            ItemStack copy = src.copy();
            copy.setCount(1);
            return copy;
        }

        @Override
        public boolean hasItem() { return !supplyItem().isEmpty(); }

        @Override
        public ItemStack getItem() { return supplyItem(); }

        @Override
        public void set(ItemStack stack) { /* 供应槽不接收设置 */ }

        @Override
        public boolean mayPlace(ItemStack stack) { return false; }

        @Override
        public boolean mayPickup(Player player) { return hasItem(); }

        @Override
        public ItemStack remove(int amount) {
            ItemStack it = supplyItem();
            if (it.isEmpty()) return ItemStack.EMPTY;
            int cnt = Math.max(1, Math.min(amount, it.getMaxStackSize()));
            ItemStack out = it.copy();
            out.setCount(cnt);
            return out;
        }

        @Override
        public void setChanged() { /* no-op */ }
    }
}
