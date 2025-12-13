package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.creepebucket.programmable_magic.items.WandItemPlaceholder;
import org.creepebucket.programmable_magic.items.SpellScrollItem;
import org.creepebucket.programmable_magic.items.mana_cell.BaseWand;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModItems;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.SpellRegistry;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 最小菜单：不含复杂数据同步，仅承载 Screen 与槽位布局。
 * - 负责响应 Screen 上报的屏幕坐标，按当前屏幕尺寸构建物品栏/法术栏/侧栏/卷轴制作槽位。
 * - 负责在服务端保存魔杖中的法术物品堆栈，以及卷轴生成逻辑。
 */
public class WandMenu extends AbstractContainerMenu {
    public static final String KEY_GUI_LEFT = "gui_left";
    public static final String KEY_GUI_TOP = "gui_top";
    public static final String KEY_SPELL_OFFSET = "spell_offset";
    public static final String KEY_CLEAN = "clean";
    public static final String KEY_SPELL_SIDEBAR = "spell_sidebar";
    public static final String KEY_SAVE = "save";
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

    // 卷轴制作：左纸右出卷轴
    private final SimpleContainer scrollInv = new SimpleContainer(2);
    private Slot scrollInputSlot;
    private Slot scrollOutputSlot;
    

    /**
     * 由网络附加数据构造（包含手持是哪只手）。
     */
    public WandMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        super(ModMenuTypes.WAND_MENU.get(), containerId);
        this.playerInv = playerInv;
        int ord = 0;
        try { ord = extra.readVarInt(); } catch (Exception ignored) {}
        this.wandHand = (ord >= 0 && ord < InteractionHand.values().length) ? InteractionHand.values()[ord] : InteractionHand.MAIN_HAND;
        this.wandInv = new SimpleContainer(resolveWandSlots());
        loadWandInvFromStack();
    }

    /**
     * 默认主手的便捷构造。
     */
    public WandMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, InteractionHand.MAIN_HAND);
    }

    /**
     * 指定手的便捷构造。
     */
    public WandMenu(int containerId, Inventory playerInv, InteractionHand hand) {
        super(ModMenuTypes.WAND_MENU.get(), containerId);
        this.playerInv = playerInv;
        this.wandHand = hand;
        this.wandInv = new SimpleContainer(resolveWandSlots());
        loadWandInvFromStack();
    }

    @Override
    /**
     * 简化：任意时刻均可交互。
     */
    public boolean stillValid(Player player) { return true; }

    @Override
    /**
     * 当法术存储容器变化时，同步卷轴输出与服务端存档。
     */
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.wandInv) {
            updateScrollOutput();
            saveWandInvToStack(this.playerInv.player);
        }
    }

    @Override
    /**
     * Shift 快速移动：
     * - 法术栏来源：清空该偏移槽。
     * - 供应栏来源：向法术存储最左侧空位放入 1 个。
     * - 其它：尝试移动到法术栏可见范围。
     */
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack in = slot.getItem();
            ret = in.copy();

            if (slot instanceof OffsetSlot) {
                // Shift 删除：来自法术栏的条目直接清空该法术槽
                slot.set(ItemStack.EMPTY);
            } else if (slot instanceof SupplySlot) {
                // 供应槽：只向法术存储中“最左侧”的空位放入 1 个并立即返回
                ItemStack one = in.copy();
                one.setCount(1);
                int n = this.wandInv.getContainerSize();
                for (int i = 0; i < n; i++) {
                    if (this.wandInv.getItem(i).isEmpty()) {
                        this.wandInv.setItem(i, one);
                        this.slotsChanged(this.wandInv);
                        return ItemStack.EMPTY;
                    }
                }
                return ItemStack.EMPTY;
            } else {
                int start = this.spellStartIndex >= 0 ? this.spellStartIndex : 36;
                int end = this.spellEndIndex >= 0 ? this.spellEndIndex : this.slots.size();
                if (!this.moveItemStackTo(in, start, end, false)) return ItemStack.EMPTY;
            }

            if (in.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return ret;
    }


    /**
     * 由 Screen 上报的客户端数据回填到 Menu：
     * - gui_left/gui_top：用于从屏幕坐标换算容器坐标并构建槽位。
     * - spell_offset：法术偏移视窗。
     * - clean：清空当前法术存储。
     * - spell_sidebar：更换法术供应类别并重置滚动。
     * - spell_sidebar_scroll：按行滚动供应侧栏。
     * - save：将当前法术存储备份到“保存用”数据组件。
     */
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
        } else if (KEY_SAVE.equals(key)) {
            saveWandInvToSavedStacks(this.playerInv.player);
        }

        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

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

            // 在法术槽右侧添加两个槽位：左输入纸，右输出卷轴
            int spellY = sh + MathUtils.SPELL_SLOT_OFFSET - (compactMode ? 18 : 0);
            int rightMostX = centerX - spellSlotCount * 8 + (spellSlotCount - 1) * 16 - 1;
            int inputX = sw - 80;
            int outputX = sw - 48;
            this.scrollInputSlot = this.addSlot(new ScrollInputSlot(this.scrollInv, 0, inputX - this.guiLeft, spellY - this.guiTop));
            this.scrollOutputSlot = this.addSlot(new ScrollOutputSlot(this.scrollInv, 1, outputX - this.guiLeft, spellY - this.guiTop));
            updateScrollOutput();

            slotsBuilt = true;
        }

        // 偏移变化：OffsetSlot 动态映射，无需改 Slot.index
        if (slotsBuilt && clientData.containsKey(KEY_SPELL_OFFSET)) {
            this.slotsChanged(this.wandInv);
            this.broadcastChanges();
        }
    }

    /**
     * 辅助：以屏幕坐标添加玩家物品栏槽位。
     */
    public Slot addSlotConverted(Inventory inv, int index, int screenX, int screenY) {
        int cx = screenX - this.guiLeft;
        int cy = screenY - this.guiTop;
        return this.addSlot(new Slot(inv, index, cx, cy));
    }

    /**
     * 辅助：以屏幕坐标添加偏移映射槽位（法术栏视窗）。
     */
    public Slot addOffsetSlotConverted(Container inv, int baseIndex, int screenX, int screenY) {
        int cx = screenX - this.guiLeft;
        int cy = screenY - this.guiTop;
        return this.addSlot(new OffsetSlot(inv, baseIndex, cx, cy));
    }

    /**
     * 法术栏槽位：通过全局偏移映射至 wandInv 的实际索引。
     */
    private class OffsetSlot extends Slot {
        private final Container inv;
        private final int baseIndex;

        public OffsetSlot(Container inv, int baseIndex, int x, int y) {
            super(inv, 0, x, y);
            this.inv = inv;
            this.baseIndex = baseIndex;
        }

        /** 目标索引 = 视窗基准 + 全局偏移 */
        private int targetIndex() { return baseIndex + spellIndexOffset; }

        /** 检查索引是否在容器有效范围内 */
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
            boolean isScroll = item instanceof SpellScrollItem;
            return (isSpell || isPlaceholder || isScroll) && this.container.canPlaceItem(idx, stack);
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

    /**
     * 返回法术栏实际容量（由魔杖定义）。
     */
    public int getSpellSlotCapacity() { return this.wandInv.getContainerSize(); }

    /**
     * 返回魔杖充能功率（W）。
     */
    public double getChargeRate() {
        ItemStack st = getWandStack();
        if (st != null && st.getItem() instanceof BaseWand bw) return bw.getChargeRate();
        return 0.0;
    }

    /**
     * 解析当前手持魔杖的法术槽位数（默认 25）。
     */
    private int resolveWandSlots() {
        ItemStack st = getWandStack();
        if (st != null && st.getItem() instanceof BaseWand bw) return bw.getSlots();
        return 25;
    }

    /**
     * 获取当前该菜单绑定手上的魔杖堆栈。
     */
    private ItemStack getWandStack() {
        var p = this.playerInv.player;
        return this.wandHand == InteractionHand.OFF_HAND ? p.getOffhandItem() : p.getMainHandItem();
    }

    /**
     * 从魔杖数据组件加载 wandInv。
     */
    private void loadWandInvFromStack() {
        ItemStack st = getWandStack();
        List<ItemStack> saved = st != null ? st.get(ModDataComponents.WAND_STACKS_SMALL.get()) : null;
        int n = this.wandInv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack it = (saved != null && i < saved.size()) ? saved.get(i) : ItemStack.EMPTY;
            this.wandInv.setItem(i, it);
        }
    }

    /**
     * 保存 wandInv 至魔杖数据组件（仅服务端）。
     */
    private void saveWandInvToStack(Player player) {
        if (player.level().isClientSide) return;
        ItemStack st = getWandStack();
        if (st == null || st.isEmpty()) return;
        int n = this.wandInv.getContainerSize();
        var list = new ArrayList<ItemStack>(n);
        for (int i = 0; i < n; i++) if (!this.wandInv.getItem(i).isEmpty()) list.add(this.wandInv.getItem(i));
        st.set(ModDataComponents.WAND_STACKS_SMALL.get(), list);
    }

    /**
     * 另存一份“保存用”法术清单（仅非空项，复制）。
     */
    private void saveWandInvToSavedStacks(Player player) {
        if (player.level().isClientSide) return;
        ItemStack st = getWandStack();
        if (st == null || st.isEmpty()) return;
        int n = this.wandInv.getContainerSize();
        var list = new ArrayList<ItemStack>(n);
        for (int i = 0; i < n; i++) if (!this.wandInv.getItem(i).isEmpty()) list.add(this.wandInv.getItem(i).copy());
        st.set(ModDataComponents.WAND_SAVED_STACKS.get(), list);
    }

    @Override
    /**
     * 菜单关闭时保存一次。
     */
    public void removed(Player player) {
        super.removed(player);
        saveWandInvToStack(player);
    }

    @Override
    /**
     * 供应槽的点击行为特殊处理：若手上有物品则清空（模拟创造删除）。
     */
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot s = this.slots.get(slotId);
            if (s instanceof SupplySlot) {
                // 创造栏逻辑：如果手上有物品，点击供应槽会清空手上物品（删除）
                if (!this.getCarried().isEmpty()) { this.setCarried(ItemStack.EMPTY); return; }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    // 计算当前侧栏应展示的法术清单（按子类别分组后扁平化，保持视觉顺序）并生成初始供给槽位
    /**
     * 根据当前侧栏类型构建一组可见的供应槽位（含滚动偏移）。
     */
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

    /**
     * 计算当前侧栏类别下的扁平化法术物品列表（保持分组遍历顺序）。
     */
    private java.util.List<ItemStack> computeSupplyItemsForCurrentSidebar() {
        var type = SpellUtils.stringSpellTypeMap.getOrDefault(this.selectedSidebar,
                SpellItemLogic.SpellType.COMPUTE_MOD);
        var map = SpellUtils.getSpellsGroupedBySubCategory(type);
        var flat = new java.util.ArrayList<ItemStack>();
        for (var e : map.entrySet()) flat.addAll(e.getValue());
        return flat;
    }

    /**
     * 刷新供应槽：禁用旧槽并重建可见区域。
     */
    private void refreshSupplySlots(boolean rebuildLayout) {
        // 重建分组布局（Slot 坐标不可变更，滚动时需要再生成一批新的槽位并禁用旧槽位）
        supplyItems = computeSupplyItemsForCurrentSidebar();
        for (var s : supplySlots) s.setActive(false);
        supplySlots.clear();
        buildInitialSupplySlots();
        this.slotsChanged(this.supplyInv);
        this.broadcastChanges();
    }

    /**
     * 辅助：以屏幕坐标添加供应槽位。
     */
    private SupplySlot addSupplySlotConverted(int supplyIndex, int screenX, int screenY) {
        int cx = screenX - this.guiLeft;
        int cy = screenY - this.guiTop;
        var s = new SupplySlot(this.supplyInv, 0, cx, cy, supplyIndex);
        return (SupplySlot) this.addSlot(s);
    }

    /**
     * 供应槽：不存储物品，展示并按需“复制”取出。
     */
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

    /**
     * 生成卷轴输出：当左侧为纸张时，将 wandInv 非空项复制到右侧卷轴。
     */
    private void updateScrollOutput() {
        ItemStack in = this.scrollInv.getItem(0);
        if (in != null && !in.isEmpty() && in.is(Items.PAPER)) {
            // 构建一个卷轴，内含当前 wandInv 非空条目
            java.util.ArrayList<ItemStack> list = new java.util.ArrayList<>();
            int n = this.wandInv.getContainerSize();
            for (int i = 0; i < n; i++) {
                ItemStack it = this.wandInv.getItem(i);
                if (!it.isEmpty()) list.add(it.copy());
            }
            ItemStack scroll = ModItems.SPELL_SCROLL.get().getDefaultInstance();
            scroll.set(ModDataComponents.SPELL_SCROLL_STACKS.get(), list);
            this.scrollInv.setItem(1, scroll);
        } else {
            this.scrollInv.setItem(1, ItemStack.EMPTY);
        }
        this.slotsChanged(this.scrollInv);
        this.broadcastChanges();
    }

    /**
     * 卷轴制作左槽：仅允许纸张。
     */
    private class ScrollInputSlot extends Slot {
        public ScrollInputSlot(Container inv, int index, int x, int y) { super(inv, index, x, y); }
        @Override
        public boolean mayPlace(ItemStack stack) { return stack.is(Items.PAPER); }
        @Override
        public void setChanged() { updateScrollOutput(); }
    }

    /**
     * 卷轴制作右槽：只读输出。
     */
    private class ScrollOutputSlot extends Slot {
        public ScrollOutputSlot(Container inv, int index, int x, int y) { super(inv, index, x, y); }
        @Override
        public boolean mayPlace(ItemStack stack) { return false; }
        @Override
        public boolean hasItem() { return !this.container.getItem(this.getSlotIndex()).isEmpty(); }
        @Override
        public ItemStack getItem() { return this.container.getItem(this.getSlotIndex()); }
        @Override
        public void set(ItemStack stack) { /* 禁止外部写入 */ }
        @Override
        public ItemStack remove(int amount) { return super.remove(amount); }
        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            // 消耗左侧一张纸
            ItemStack in = scrollInv.getItem(0);
            if (!in.isEmpty()) {
                in.shrink(1);
                if (in.isEmpty()) scrollInv.setItem(0, ItemStack.EMPTY);
            }
            updateScrollOutput();
        }
    }
}
