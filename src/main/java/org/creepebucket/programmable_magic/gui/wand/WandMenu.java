package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.DataInstance;
import org.creepebucket.programmable_magic.gui.lib.api.DataType;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.UiMenuBase;
import org.creepebucket.programmable_magic.gui.lib.widgets.SlotWidget;
import org.creepebucket.programmable_magic.gui.wand.WandSlots.OffsetSlot;
import org.creepebucket.programmable_magic.gui.wand.WandSlots.PluginSlot;
import org.creepebucket.programmable_magic.gui.wand.WandSlots.SupplySlot;
import org.creepebucket.programmable_magic.items.Wand;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.spells.old.SpellUtils;
import org.creepebucket.programmable_magic.wand_plugins.BasePlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 最小菜单：不含复杂数据同步，仅承载 Screen 与槽位布局。
 * - 负责响应 Screen 上报的屏幕坐标，按当前屏幕尺寸构建物品栏/法术栏/侧栏/卷轴制作槽位。
 * - 负责在服务端保存魔杖中的法术物品堆栈，以及卷轴生成逻辑。
 */
public class WandMenu extends UiMenuBase {
    public static final String KEY_GUI_LEFT = "gui_left";
    public static final String KEY_GUI_TOP = "gui_top";
    public static final String KEY_SCREEN_WIDTH = "screen_width";
    public static final String KEY_SCREEN_HEIGHT = "screen_height";
    public static final String KEY_SPELL_OFFSET = "spell_offset";
    public static final String KEY_CLEAN = "clean";
    public static final String KEY_SPELL_SIDEBAR = "spell_sidebar";
    public static final String KEY_SAVE = "save";
    public static final String KEY_SUPPLY_SCROLL = "spell_sidebar_scroll";
    private final Map<String, Object> clientData = new HashMap<>();
    private boolean slotsBuilt = false;

    private int screenWidth = 0;
    private int screenHeight = 0;
    public int spellIndexOffset = 0;
    public List<Slot> spellSlots;
    public int spellStartIndex = -1;
    public int spellEndIndex = -1;
    private String selectedSidebar = "compute";

    public boolean isCharging = false;
    public int chargeTicks = 0;

    public final ResizableContainer wandInv;
    private final InteractionHand wandHand;
    private final SimpleContainer supplyInv = new SimpleContainer(0);
    private final List<SupplySlot> supplySlots = new ArrayList<>();
    private List<ItemStack> supplyItems = new ArrayList<>();
    private List<SupplyGroupMeta> supplyGroupMetas = new ArrayList<>();
    private int supplyScrollRow = 0;

    // 插件：玩家自装配的插件存储与槽位
    private final SimpleContainer pluginInv;
    private final List<Slot> pluginSlots = new ArrayList<>();

    // 卷轴制作功能已移除

    private DataInstance ui_gui_left;
    private DataInstance ui_gui_top;
    private DataInstance ui_screen_width;
    private DataInstance ui_screen_height;
    private DataInstance ui_spell_offset;
    private DataInstance ui_clean;
    private DataInstance ui_spell_sidebar;
    private DataInstance ui_save;
    private DataInstance ui_supply_scroll;
    

    /**
     * 由网络附加数据构造（包含手持是哪只手）。
     */
    public WandMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        super(ModMenuTypes.WAND_MENU.get(), containerId, playerInv, ui -> {});
        int ord = extra.readVarInt();
        this.wandHand = (ord >= 0 && ord < InteractionHand.values().length) ? InteractionHand.values()[ord] : InteractionHand.MAIN_HAND;
        this.wandInv = new ResizableContainer(resolveWandSlots());
        this.pluginInv = new SimpleContainer(resolvePluginSlots());
        loadWandInvFromStack();
        loadPluginsFromStack();
        updateWandCapacityFromPlugins();
        initUiData();
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
        super(ModMenuTypes.WAND_MENU.get(), containerId, playerInv, ui -> {});
        this.wandHand = hand;
        this.wandInv = new ResizableContainer(resolveWandSlots());
        this.pluginInv = new SimpleContainer(resolvePluginSlots());
        loadWandInvFromStack();
        loadPluginsFromStack();
        updateWandCapacityFromPlugins();
        initUiData();
    }

    private void initUiData() {
        this.ui_gui_left = ui().data(KEY_GUI_LEFT, DataType.INT, SyncMode.C2S, 0);
        this.ui_gui_top = ui().data(KEY_GUI_TOP, DataType.INT, SyncMode.C2S, 0);
        this.ui_screen_width = ui().data(KEY_SCREEN_WIDTH, DataType.INT, SyncMode.C2S, 0);
        this.ui_screen_height = ui().data(KEY_SCREEN_HEIGHT, DataType.INT, SyncMode.C2S, 0);
        this.ui_spell_offset = ui().data(KEY_SPELL_OFFSET, DataType.INT, SyncMode.C2S, 0);
        this.ui_clean = ui().data(KEY_CLEAN, DataType.BOOLEAN, SyncMode.C2S, false);
        this.ui_spell_sidebar = ui().data(KEY_SPELL_SIDEBAR, DataType.STRING, SyncMode.C2S, "compute");
        this.ui_save = ui().data(KEY_SAVE, DataType.BOOLEAN, SyncMode.C2S, false);
        this.ui_supply_scroll = ui().data(KEY_SUPPLY_SCROLL, DataType.INT, SyncMode.C2S, 0);
    }

    public void sendUiData(String key, Object value) {
        if (KEY_GUI_LEFT.equals(key)) this.ui_gui_left.setInt((Integer) value);
        else if (KEY_GUI_TOP.equals(key)) this.ui_gui_top.setInt((Integer) value);
        else if (KEY_SCREEN_WIDTH.equals(key)) this.ui_screen_width.setInt((Integer) value);
        else if (KEY_SCREEN_HEIGHT.equals(key)) this.ui_screen_height.setInt((Integer) value);
        else if (KEY_SPELL_OFFSET.equals(key)) this.ui_spell_offset.setInt((Integer) value);
        else if (KEY_CLEAN.equals(key)) this.ui_clean.setBoolean((Boolean) value);
        else if (KEY_SPELL_SIDEBAR.equals(key)) this.ui_spell_sidebar.setString((String) value);
        else if (KEY_SUPPLY_SCROLL.equals(key)) this.ui_supply_scroll.setInt((Integer) value);
        else if (KEY_SAVE.equals(key)) this.ui_save.setBoolean((Boolean) value);
    }

    public void sendMenuData(String key, Object value) {
        setClientData(key, value);
        sendUiData(key, value);
    }

    @Override
    public void reportScreenSize(int screenWidth, int screenHeight) {
        sendMenuData(KEY_SCREEN_WIDTH, screenWidth);
        sendMenuData(KEY_SCREEN_HEIGHT, screenHeight);
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
            saveWandInvToStack(this.playerInv.player);
        } else if (container == this.pluginInv) {
            savePluginsToStack(this.playerInv.player);
            updateWandCapacityFromPlugins();
            int cap = this.wandInv.getContainerSize();
            this.spellIndexOffset = Mth.clamp(this.spellIndexOffset, 0, cap);
            if (this.screenWidth > 0 && this.screenHeight > 0) ensureDynamicSlots();
            if (hasPluginPrefix("spell_supply_t")) refreshSupplySlots();
            else disableSupplySlots();
            this.slotsChanged(this.wandInv);
            this.broadcastChanges();
            rebuildUi();
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
        // 禁用主手槽位的 shift 快速移动（通过对象引用比对主手物品）
        if (slot != null && slot.container == this.playerInv) {
            ItemStack main = this.playerInv.player.getMainHandItem();
            if (!main.isEmpty() && slot.getItem() == main) return ItemStack.EMPTY;
        }
        if (slot != null && slot.hasItem()) {
            ItemStack in = slot.getItem();
            ret = in.copy();

            if (slot instanceof OffsetSlot) {
                // Shift 删除：来自法术栏的条目直接清空该法术槽
                slot.set(ItemStack.EMPTY);
            } else if (slot instanceof SupplySlot) {
                // 供应槽：只向法术存储中“最左侧”的空位放入 1 个并立即返回
                for (int i = 0; i < this.wandInv.getContainerSize(); i++) {
                    if (this.wandInv.getItem(i).isEmpty()) {
                        ItemStack one = in.copy();
                        one.setCount(1);
                        this.wandInv.setItem(i, one);
                        this.slotsChanged(this.wandInv);
                        return ItemStack.EMPTY;
                    }
                }
                return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(in,
                        this.spellStartIndex >= 0 ? this.spellStartIndex : 36,
                        this.spellEndIndex >= 0 ? this.spellEndIndex : this.slots.size(),
                        false)) return ItemStack.EMPTY;
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

        if (KEY_SCREEN_WIDTH.equals(key)) {
            this.screenWidth = (Integer) value;
            if (this.screenWidth > 0 && this.screenHeight > 0) onScreenSizeReady();
        } else if (KEY_SCREEN_HEIGHT.equals(key)) {
            this.screenHeight = (Integer) value;
            if (this.screenWidth > 0 && this.screenHeight > 0) onScreenSizeReady();
        } else if (KEY_SPELL_OFFSET.equals(key)) {
            int cap = this.wandInv.getContainerSize();
            this.spellIndexOffset = Mth.clamp((Integer) value, 0, cap);
            if (slotsBuilt) {
                this.slotsChanged(this.wandInv);
                this.broadcastChanges();
            }
        } else if (KEY_CLEAN.equals(key)) {
            int n = this.wandInv.getContainerSize();
            for (int i = 0; i < n; i++) this.wandInv.setItem(i, ItemStack.EMPTY);
            this.slotsChanged(this.wandInv);
            this.broadcastChanges();
            saveWandInvToStack(this.playerInv.player);
        } else if (KEY_SPELL_SIDEBAR.equals(key)) {
            this.selectedSidebar = (String) value;
            this.supplyScrollRow = 0;
            if (slotsBuilt && hasPluginPrefix("spell_supply_t")) refreshSupplySlots();
        } else if (KEY_SUPPLY_SCROLL.equals(key)) {
            this.supplyScrollRow = (Integer) value;
            if (slotsBuilt && hasPluginPrefix("spell_supply_t")) updateSupplySlotMapping();
        } else if (KEY_SAVE.equals(key)) {
            saveWandInvToSavedStacks(this.playerInv.player);
        }
    }

    private void onScreenSizeReady() {
        ensureSlotsBuilt();
        ensureDynamicSlots();
        if (hasPluginPrefix("spell_supply_t")) refreshSupplySlots();
        else disableSupplySlots();
        if (slotsBuilt) rebuildUi();
    }

    private void disableSupplySlots() {
        for (var slot : this.supplySlots) {
            slot.setSupplyIndex(-1);
            slot.setActive(false);
        }
    }

    private void ensureSlotsBuilt() {
        if (this.slotsBuilt) return;

        for (int i = 0; i < 36; i++) this.addSlot(new Slot(this.playerInv, i, 0, 0));
        this.spellSlots = new ArrayList<>();

        int count = this.pluginInv.getContainerSize();
        for (int i = 0; i < count; i++) {
            Slot slot = this.addSlot(new PluginSlot(this.pluginInv, i, 0, 0));
            this.pluginSlots.add(slot);
        }

        this.slotsBuilt = true;
    }

    private void ensureDynamicSlots() {
        int visibleSpells = Math.max(1, WandLayout.visible_spell_slots(this.screenWidth));
        for (int i = this.spellSlots.size(); i < visibleSpells; i++) {
            if (this.spellStartIndex < 0) this.spellStartIndex = this.slots.size();
            Slot slot = this.addSlot(new OffsetSlot(this, this.wandInv, i, 0, 0));
            this.spellSlots.add(slot);
            this.spellEndIndex = this.slots.size();
        }

        int rows = Math.max(1, Math.floorDiv((this.screenHeight - 16) - 20, 16));
        int needed = rows * 5;
        for (int i = this.supplySlots.size(); i < needed; i++) {
            SupplySlot slot = (SupplySlot) this.addSlot(new SupplySlot(this.supplyInv, 0, 0, 0, -1, () -> this.supplyItems));
            slot.setActive(false);
            this.supplySlots.add(slot);
        }
    }

    public String selectedSidebar() { return this.selectedSidebar; }
    public int supplyScrollRow() { return this.supplyScrollRow; }

    public void rebuildUi() {
        if (!this.playerInv.player.level().isClientSide()) return;
        this.ui().clearWidgets();

        if (!this.slotsBuilt) return;

        this.ui().addWidget(new WandUiWidgets.BaseBackgroundWidget(this));
        this.ui().addWidget(new WandUiWidgets.ChargeTickWidget(this));
        buildBaseSlotsUi();
        buildInstalledPluginsUi();
    }

    private void buildBaseSlotsUi() {
        boolean spellSlotsEnabled = hasPluginPrefix("spell_slots_t");
        boolean supplyEnabled = hasPluginPrefix("spell_supply_t");

        for (int i = 0; i < 36; i++) {
            Slot slot = this.slots.get(i);
            int idx = i;
            this.ui().addWidget(new SlotWidget(slot, new Coordinate(
                    (sw, sh) -> WandLayout.inventory_slot_x(sw, idx),
                    (sw, sh) -> WandLayout.inventory_slot_y(sw, sh, idx)
            )));
        }

        for (int i = 0; i < this.pluginSlots.size(); i++) {
            Slot slot = this.pluginSlots.get(i);
            int idx = i;
            this.ui().addWidget(new SlotWidget(slot, new Coordinate(
                    (sw, sh) -> WandLayout.plugin_slot_x(sw) - 1,
                    (sw, sh) -> WandLayout.plugin_slot_y(idx)
            )));
        }

        for (int i = 0; i < this.spellSlots.size(); i++) {
            Slot slot = this.spellSlots.get(i);
            int idx = i;
            this.ui().addWidget(new SlotWidget(slot, new Coordinate(
                    (sw, sh) -> (spellSlotsEnabled && idx < WandLayout.visible_spell_slots(sw)) ? WandLayout.spell_slot_x(sw, idx) : -9999,
                    (sw, sh) -> (spellSlotsEnabled && idx < WandLayout.visible_spell_slots(sw)) ? WandLayout.spell_slot_y(sw, sh) : -9999
            )));
        }

        for (int i = 0; i < this.supplySlots.size(); i++) {
            Slot slot = this.supplySlots.get(i);
            int idx = i;
            this.ui().addWidget(new SlotWidget(slot, new Coordinate(
                    (sw, sh) -> {
                        int needed = Math.max(1, Math.floorDiv((sh - 16) - 20, 16)) * 5;
                        return supplyEnabled ? WandLayout.supply_slot_x(idx % 5) : -9999;
                    },
                    (sw, sh) -> {
                        int needed = Math.max(1, Math.floorDiv((sh - 16) - 20, 16)) * 5;
                        return supplyEnabled ? WandLayout.supply_slot_y(Math.floorDiv(idx, 5) + (slot.isActive() ? 0 : 4)) : -9999;
                    }
            )));
        }
    }

    private void buildInstalledPluginsUi() {
        int n = this.pluginInv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack st = this.pluginInv.getItem(i);
            if (st == null || st.isEmpty()) continue;
            BasePlugin plugin = WandPluginRegistry.createPlugin(st.getItem());
            if (plugin == null) continue;
            plugin.buildUi(this);
        }
    }

    private boolean hasPluginPrefix(String prefix) {
        int n = this.pluginInv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack st = this.pluginInv.getItem(i);
            if (st == null || st.isEmpty()) continue;
            BasePlugin plugin = WandPluginRegistry.createPlugin(st.getItem());
            if (plugin == null) continue;
            if (plugin.pluginName.startsWith(prefix)) return true;
        }
        return false;
    }

    /**
     * 返回法术栏实际容量（由魔杖定义）。
     */
    public int getSpellSlotCapacity() { return this.wandInv.getContainerSize(); }

    /**
     * 返回插件槽位容量。
     */
    public int getPluginSlotCapacity() { return this.pluginInv.getContainerSize(); }

    /**
     * 返回魔杖充能功率（W）。
     */
    public double getChargeRate() {
        var values = ModUtils.computeWandValues(collectPlugins());
        return values.chargeRateW;
    }

    /**
     * 解析当前手持魔杖的法术槽位数（默认 25）。
     */
    private int resolveWandSlots() {
        ItemStack st = getWandStack();
        if (st.getItem() instanceof Wand bw) return bw.getSlots();
        return 1000;
    }

    /**
     * 解析插件槽位数量（默认 0）。
     */
    private int resolvePluginSlots() {
        ItemStack st = getWandStack();
        if (st.getItem() instanceof Wand bw) return bw.getPluginSlots();
        return 0;
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
        List<ItemStack> saved = st.get(ModDataComponents.WAND_STACKS_SMALL.get());
        int n = this.wandInv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack it = (saved != null && i < saved.size()) ? saved.get(i) : ItemStack.EMPTY;
            this.wandInv.setItem(i, it);
        }
    }

    /**
     * 从魔杖数据组件加载插件栏。
     */
    private void loadPluginsFromStack() {
        ItemStack st = getWandStack();
        List<ItemStack> saved = st.get(ModDataComponents.WAND_PLUGINS.get());
        int n = this.pluginInv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack it = (saved != null && i < saved.size()) ? saved.get(i) : ItemStack.EMPTY;
            this.pluginInv.setItem(i, it);
        }
    }

    /**
     * 根据插件聚合结果更新法术容器有效容量。
     */
    private void updateWandCapacityFromPlugins() {
        var values = ModUtils.computeWandValues(collectPlugins());
        int cap = Math.max(0, (int) Math.floor(values.spellSlots));
        this.wandInv.setLimit(cap);
    }

    private List<ItemStack> collectPlugins() {
        ArrayList<ItemStack> list = new ArrayList<>();
        int n = this.pluginInv.getContainerSize();
        for (int i = 0; i < n; i++) {
            ItemStack it = this.pluginInv.getItem(i);
            if (it != null && !it.isEmpty()) list.add(it);
        }
        return list;
    }

    /**
     * 保存 wandInv 至魔杖数据组件（仅服务端）。
     */
    private void saveWandInvToStack(Player player) {
        if (player.level().isClientSide()) return;
        ItemStack st = getWandStack();
        if (st.isEmpty()) return;
        int n = this.wandInv.getContainerSize();
        var list = new ArrayList<ItemStack>(n);
        for (int i = 0; i < n; i++) list.add(this.wandInv.getItem(i).copy());
        st.set(ModDataComponents.WAND_STACKS_SMALL.get(), list);
    }

    /**
     * 保存插件栏至魔杖组件（仅服务端）。
     */
    private void savePluginsToStack(Player player) {
        if (player.level().isClientSide()) return;
        ItemStack st = getWandStack();
        if (st.isEmpty()) return;
        int n = this.pluginInv.getContainerSize();
        var list = new ArrayList<ItemStack>(n);
        for (int i = 0; i < n; i++) if (!this.pluginInv.getItem(i).isEmpty()) list.add(this.pluginInv.getItem(i));
        st.set(ModDataComponents.WAND_PLUGINS.get(), list);
    }

    /**
     * 另存一份“保存用”法术清单（仅非空项，复制）。
     */
    private void saveWandInvToSavedStacks(Player player) {
        if (player.level().isClientSide()) return;
        ItemStack st = getWandStack();
        if (st.isEmpty()) return;
        int n = this.wandInv.getContainerSize();
        var list = new ArrayList<ItemStack>(n);
        for (int i = 0; i < n; i++) list.add(this.wandInv.getItem(i).copy());
        st.set(ModDataComponents.WAND_SAVED_STACKS.get(), list);
    }

    @Override
    /**
     * 菜单关闭时保存一次。
     */
    public void removed(Player player) {
        super.removed(player);
        saveWandInvToStack(player);
        savePluginsToStack(player);
        if (!player.level().isClientSide()) getWandStack().set(ModDataComponents.WAND_LAST_RELEASE_TIME.get(), player.level().getGameTime());
    }

    @Override
    /**
     * 供应槽的点击行为特殊处理：若手上有物品则清空（模拟创造删除）。
     */
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP) return;
        // 仅禁用“主手槽位”的点击交互（通过对象引用比对主手物品），其它不限制
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot s = this.slots.get(slotId);
            if (s != null && s.container == this.playerInv) {
                ItemStack main = this.playerInv.player.getMainHandItem();
                if (!main.isEmpty() && s.getItem() == main) return;
            }
        }
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot s = this.slots.get(slotId);
            if (s instanceof SupplySlot) {
                // 创造栏逻辑：如果手上有物品，点击供应槽会清空手上物品（删除）
                if (!this.getCarried().isEmpty()) { this.setCarried(ItemStack.EMPTY); return; }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    /**
     * 计算当前侧栏类别下的扁平化法术物品列表（保持分组遍历顺序）。
     */
    public List<ItemStack> computeSupplyItemsForCurrentSidebar() {
        var type = SpellUtils.stringSpellTypeMap.getOrDefault(this.selectedSidebar,
                SpellItemLogic.SpellType.COMPUTE_MOD);
        var map = SpellUtils.getSpellsGroupedBySubCategory(type);
        var flat = new ArrayList<ItemStack>();
        var metas = new ArrayList<SupplyGroupMeta>();
        int base = 0;
        int rowStart = 1;
        for (var e : map.entrySet()) {
            List<ItemStack> list = e.getValue();
            int size = list.size();
            int rows = (int) Math.ceil(size / 5.0);
            metas.add(new SupplyGroupMeta(base, size, rows, rowStart));
            flat.addAll(list);
            base += size;
            rowStart += rows + 1;
        }
        this.supplyGroupMetas = metas;
        return flat;
    }

    private static final class SupplyGroupMeta {
        final int baseIndex;
        final int size;
        final int rows;
        final int rowStart; // 该组第一行的全局行号
        SupplyGroupMeta(int baseIndex, int size, int rows, int rowStart) {
            this.baseIndex = baseIndex;
            this.size = size;
            this.rows = rows;
            this.rowStart = rowStart;
        }
    }

    /**
     * 刷新供应槽：禁用旧槽并重建可见区域。
     */
    /**
     * 刷新供应槽：重算当前侧栏物品并更新映射。
     */
    private void refreshSupplySlots() {
        supplyItems = computeSupplyItemsForCurrentSidebar();
        updateSupplySlotMapping();
    }

    /**
     * 将可见网格行列映射到实际数据索引，并设置槽位激活状态。
     */
    public void updateSupplySlotMapping() {
        int visibleRows;
        {
            int sh = this.screenHeight;
            int visibleHeightPx = (sh - 16) - 20;
            visibleRows = Math.max(1, Math.floorDiv(visibleHeightPx, 16));
        }

        int totalSlots = supplySlots.size();
        int totalInView = visibleRows * 5;

        for (int r = 0; r < visibleRows; r++) {
            int cRow = this.supplyScrollRow + r; // 全局内容行
            // 查找该行属于哪个组，或是否为组间空白行
            SupplyGroupMeta metaForRow = null;
            boolean gapRow = false;
            for (var m : supplyGroupMetas) {
                if (cRow >= m.rowStart && cRow < m.rowStart + m.rows) { metaForRow = m; break; }
                if (cRow == m.rowStart + m.rows) { gapRow = true; break; }
            }

            for (int col = 0; col < 5; col++) {
                int slotIdx = r * 5 + col;
                if (slotIdx >= totalSlots) break;
                var s = supplySlots.get(slotIdx);
                if (gapRow || metaForRow == null) {
                    s.setSupplyIndex(-1);
                    s.setActive(false);
                    continue;
                }
                int localRow = cRow - metaForRow.rowStart;
                int idxInGroup = localRow * 5 + col;
                if (idxInGroup >= metaForRow.size) {
                    s.setSupplyIndex(-1);
                    s.setActive(false);
                } else {
                    int globalIndex = metaForRow.baseIndex + idxInGroup;
                    s.setSupplyIndex(globalIndex);
                    s.setActive(true);
                }
            }
        }

        for (int i = totalInView; i < totalSlots; i++) {
            var s = supplySlots.get(i);
            s.setSupplyIndex(-1);
            s.setActive(false);
        }
        this.slotsChanged(this.supplyInv);
        this.broadcastChanges();
    }

    // 卷轴制作逻辑已移除

    /**
     * 供 Screen 调用：返回指定插件槽位的当前物品。
     */
    /**
     * 供 Screen 调用：返回指定插件槽位的当前物品。
     */
    public ItemStack getPluginItem(int index) {
        if (index < 0 || index >= this.pluginInv.getContainerSize()) return ItemStack.EMPTY;
        return this.pluginInv.getItem(index);
    }


    @Override
    public void handleSimpleKvC2S(String key, Object value) {
        super.handleSimpleKvC2S(key, value);
        setClientData(key, value);
    }

    @Override
    public void handleSimpleKvS2C(String key, Object value) {
        super.handleSimpleKvS2C(key, value);
        setClientData(key, value);
    }

    /**
     * 可调容量容器：
     * - 以最大容量初始化，运行时按插件数值设置“有效容量”。
     * - getContainerSize 返回有效容量，内部存储保留最大容量。
     */
    public static class ResizableContainer extends SimpleContainer {
        private final int max;
        private int limit;

        public ResizableContainer(int max) {
            super(max);
            this.max = Math.max(0, max);
            this.limit = this.max;
        }

        public void setLimit(int limit) {
            if (limit < 0) limit = 0;
            if (limit > max) limit = max;
            this.limit = limit;
        }

        @Override
        public int getContainerSize() {
            return this.limit;
        }
    }

}
