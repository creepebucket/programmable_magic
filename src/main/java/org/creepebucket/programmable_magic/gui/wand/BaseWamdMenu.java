package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.creepebucket.programmable_magic.items.wand.BaseWand;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModTagKeys;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static org.creepebucket.programmable_magic.ModUtils.getItemsFromTag;
import static org.creepebucket.programmable_magic.registries.ModTagKeys.SPELL;

public class BaseWamdMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProgrammableMagic:WandMenu");

    private final ContainerLevelAccess levelAccess;
    private final ItemStackHandler baseSpellItemInventory;
    private final ItemStackHandler spellModAdjustItemInventory;
    private final ItemStackHandler spellModControlItemInventory;
    private final ItemStackHandler spellModTargetItemInventory;
    private final ItemStackHandler spellStorageInventory;

    private final DataComponentType<List<ItemStack>> storageComponent;

    List<SpellDisplaySlot> listBaseSpellSlot = new ArrayList<>();
    List<SpellDisplaySlot> listSpellModAdjustSlot = new ArrayList<>();
    List<SpellDisplaySlot> listSpellModControlSlot = new ArrayList<>();
    List<SpellDisplaySlot> listSpellModTargetSlot = new ArrayList<>();
    List<SpellStorageSlot> listSpellStorageSlot = new ArrayList<>();

    public static final int CenterX = 89;

    public static final int playerInvLeftX = CenterX - 18 * 18 / 2;
    public static final int hotbarY = 152;
    public static final int playerInvY = hotbarY + 18;
    public static final int hotbarLeftX = CenterX - 18 * 18 / 2 + 1;
    public static final int playerInvWidth = 18 * 18 - 2;
    public static final int playerInvHeight = 34;

    public static final int SpellDisplaySlotWidth = 18 * 10 - 2; // fuck, 怎么首字母大写了
    public static final int SpellDisplaySlotHeight = 18 * 3 - 2;
    public static final int SpellDisplayLeftX = -120;
    public static final int SpellDisplayRightX = CenterX + abs(CenterX - SpellDisplayLeftX) - SpellDisplaySlotWidth;
    public static final int SpellDisplayTopY = -30;
    public static final int SpellDisplayBottomY = SpellDisplayTopY + SpellDisplaySlotHeight + 15;

    public int spellStorageLeftX;
    public int spellstorageY;
    public int spellStorageWidth;

    private final int SLOTS;
    private double MANA_MULT;

    protected BaseWamdMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory, ContainerLevelAccess levelAccess, int spellSlots, double manaMult, DataComponentType<List<ItemStack>> storageComponent) {
        super(menuType, containerId);

        this.storageComponent = storageComponent;
        this.SLOTS = spellSlots;
        this.MANA_MULT = manaMult;

        List<ItemStack> baseSpells = getItemsFromTag(ModTagKeys.SPELL_BASE_EFFECT);
        List<ItemStack> spellModAdjust = getItemsFromTag(ModTagKeys.SPELL_ADJUST_MOD);
        List<ItemStack> spellModControl = getItemsFromTag(ModTagKeys.SPELL_CONTROL_MOD);
        List<ItemStack> spellModTarget = getItemsFromTag(ModTagKeys.SPELL_TARGET_MOD);

        this.levelAccess = levelAccess;

        //法术展示
        this.baseSpellItemInventory = new ItemStackHandler(baseSpells.size());
        this.spellModAdjustItemInventory = new ItemStackHandler(spellModAdjust.size());
        this.spellModControlItemInventory = new ItemStackHandler(spellModControl.size());
        this.spellModTargetItemInventory = new ItemStackHandler(spellModTarget.size());
        this.spellStorageInventory = new ItemStackHandler(this.SLOTS);

        //添加法术槽位
        addSlots(SpellDisplayLeftX, SpellDisplayTopY, 10, baseSpells.size(), baseSpellItemInventory, this.listBaseSpellSlot,
                (handler, slotIndex, x, y, menu) ->
                        new SpellDisplaySlot(handler, slotIndex, x, y, menu, baseSpells.get(slotIndex)));
        addSlots(SpellDisplayRightX, SpellDisplayTopY, 10, spellModAdjust.size(), spellModAdjustItemInventory, this.listSpellModAdjustSlot,
                (handler, slotIndex, x, y, menu) ->
                        new SpellDisplaySlot(handler, slotIndex, x, y, menu, spellModAdjust.get(slotIndex)));
        addSlots(SpellDisplayLeftX, SpellDisplayBottomY, 10, spellModControl.size(), spellModControlItemInventory, this.listSpellModControlSlot,
                (handler, slotIndex, x, y, menu) ->
                        new SpellDisplaySlot(handler, slotIndex, x, y, menu, spellModControl.get(slotIndex)));
        addSlots(SpellDisplayRightX, SpellDisplayBottomY, 10, spellModTarget.size(), spellModTargetItemInventory, this.listSpellModTargetSlot,
                (handler, slotIndex, x, y, menu) ->
                        new SpellDisplaySlot(handler, slotIndex, x, y, menu, spellModTarget.get(slotIndex)));

        //法术存储
        spellStorageWidth = SLOTS * 18 - 2;
        spellStorageLeftX = CenterX - spellStorageWidth / 2;
        spellstorageY = 120;
        addSlots(spellStorageLeftX, spellstorageY, 999, this.SLOTS, this.spellStorageInventory, this.listSpellStorageSlot,
                SpellStorageSlot::new);

        // 所有槽位与库存初始化完成后，再从玩家持有魔杖中加载
        loadFromPlayer(playerInventory.player);
    }

    public void addInventorySlot(Inventory playerInventory) {
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, hotbarLeftX + col * 18, hotbarY));
        }
        // 背包
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col + 9, CenterX + col * 18 + 1, hotbarY));
        }
        for (int col = 0; col < 18; col++) {
            this.addSlot(new Slot(playerInventory, col + 18, playerInvLeftX + col * 18 + 1, playerInvY));
        }
    }

    private static ItemStack sanitize(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getCount() <= 0) return ItemStack.EMPTY;
        ItemStack c = stack.copy();
        int clamped = Math.min(Math.max(c.getCount(), 1), 99);
        c.setCount(clamped);
        return c;
    }

    public List<ItemStack> getStorageStacks() {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < this.spellStorageInventory.getSlots(); i++) {
            ItemStack stack = this.spellStorageInventory.getStackInSlot(i);
            ItemStack s = sanitize(stack);
            if (!s.isEmpty()) result.add(s);
        }
        return result;
    }

    public void setStorageStacks(List<ItemStack> stacks) {
        int max = Math.min(stacks.size(), this.spellStorageInventory.getSlots());
        int idx = 0;
        for (int i = 0; i < max; i++) {
            ItemStack s = sanitize(stacks.get(i));
            this.spellStorageInventory.setStackInSlot(i, s);
            idx++;
        }
        for (int i = idx; i < this.spellStorageInventory.getSlots(); i++) {
            this.spellStorageInventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private static ItemStack findWand(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof BaseWand) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof BaseWand) return off;
        return ItemStack.EMPTY;
    }

    public void loadFromPlayer(Player player) {
        ItemStack wand = findWand(player);
        if (!wand.isEmpty()) {
            List<ItemStack> saved = wand.getOrDefault(storageComponent, List.of());
            if (!saved.isEmpty()) {
                setStorageStacks(saved);
            }
        }
    }

    public void saveToPlayer(Player player) {
        ItemStack wand = findWand(player);
        if (!wand.isEmpty()) {
            wand.set(storageComponent, getStorageStacks());
        }
    }

    /**
     * 将点击的法术展示物品发送到法术存储区。
     */
    public void sendSpellToStorage(ItemStack spellStack) {
        for (SpellStorageSlot storageSlot : this.listSpellStorageSlot) {
            if (!storageSlot.hasItem()) {
                this.spellStorageInventory.setStackInSlot(storageSlot.getSlotIndex(), spellStack.copy());
                break;
            }
        }
    }

    public <SlotType extends Slot> void addSlots(
            int x, int y,
            int columns, int slotCount,
            ItemStackHandler inventory,
            List<SlotType> slotList,
            WandSpellStorageMenu.SlotFactory<SlotType> slotFactory
    ) {
        final int SLOT_SIZE = 18;
        for (int i = 0; i < slotCount; i++) {
            int row = (int) floor((double) i / columns);
            int col = i % columns;
            int posX = x + col * SLOT_SIZE;
            int posY = y + row * SLOT_SIZE;
            SlotType slot = slotFactory.create(inventory, i, posX, posY, this);
            slotList.add(slot);
            this.addSlot(slot);
        }
    }

    @FunctionalInterface
    public interface SlotFactory<SlotType extends Slot> {
        SlotType create(ItemStackHandler inventory, int slotIndex, int x, int y, BaseWamdMenu menu);
    }

    /*
    限定法术存储/显示槽位的交互功能, 防止玩家把法术展示的物品拿出来了
    使用下面重写的3个方法实现
     */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (slot instanceof SpellStorageSlot
                && slot.getItem().is(SPELL)) {

            ((SpellStorageSlot) slot).handleClick();
            return ItemStack.EMPTY;
        }

        if (slot instanceof SpellDisplaySlot) {
            return ItemStack.EMPTY;
        }

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack originalStack = stack.copy();

        int totalDisplaySlots = this.listBaseSpellSlot.size() + this.listSpellModAdjustSlot.size() + this.listSpellModControlSlot.size() + this.listSpellModTargetSlot.size();
        int storageSlotsStart = totalDisplaySlots;
        int storageSlotsEnd = storageSlotsStart + this.SLOTS;
        int playerInvStart = storageSlotsEnd;
        int playerInvEnd = playerInvStart + 36;

        if (slotIndex >= storageSlotsStart && slotIndex < storageSlotsEnd) {
            // 从存储区移动到背包
            // 最后一个参数为 true 表示反向填充，会优先填满主背包，最后填充快捷栏，通常是更好的体验
            if (!this.moveItemStackTo(stack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= playerInvStart && slotIndex < playerInvEnd) {
            // 从背包移动到存储区
            // 检查物品是否是法术
            if (stack.is(SPELL)) {
                if (!this.moveItemStackTo(stack, storageSlotsStart, storageSlotsEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return originalStack;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId < 0) {
            super.clicked(slotId, button, clickType, player);
            return;
        }
        Slot slot = this.slots.get(slotId);

        if (clickType == ClickType.SWAP
                && slot instanceof SpellStorageSlot
                && slot.getItem().is(SPELL)) {

            ((SpellStorageSlot) slot).handleClick();
            return;
        }

        if (slot instanceof SpellDisplaySlot spellDisplaySlot
                && clickType == ClickType.PICKUP
                && button == 0
                && this.getCarried().isEmpty()) {

            spellDisplaySlot.handleClick();
        }

        if (slot instanceof SpellStorageSlot storageSlot
                && clickType == ClickType.PICKUP
                && button == 0
                && !storageSlot.getItem().isEmpty()
                && this.getCarried().isEmpty()
                && storageSlot.getItem().is(SPELL)) {

            storageSlot.handleClick();
            return;
        }

        if (slot instanceof SpellDisplaySlot) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        int displaySlotsEnd = this.listBaseSpellSlot.size() + this.listSpellModAdjustSlot.size() + this.listSpellModControlSlot.size() + this.listSpellModTargetSlot.size();

        // 如果移动的目标范围开始于展示槽位区域内，则阻止本次移动，防止物品被放入展示槽。
        if (startIndex < displaySlotsEnd) {
            return false;
        }

        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // 每次数据同步时，重置所有展示槽位，确保它们不被意外修改
        this.listBaseSpellSlot.forEach(SpellDisplaySlot::resetToDefault);
        this.listSpellModAdjustSlot.forEach(SpellDisplaySlot::resetToDefault);
        this.listSpellModControlSlot.forEach(SpellDisplaySlot::resetToDefault);
        this.listSpellModTargetSlot.forEach(SpellDisplaySlot::resetToDefault);
    }

    @Override
    public void removed(Player player) {
        // 关闭时保存（过滤过的栈，不包含空/0）
        saveToPlayer(player);
        super.removed(player);
    }

    public List<ItemStack> getSpellsInStorage() {
        LOGGER.debug("获取法术存储中的法术");
        List<ItemStack> spells = new ArrayList<>();
        for (int i = 0; i < this.spellStorageInventory.getSlots(); i++) {
            ItemStack stack = this.spellStorageInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                spells.add(stack);
                LOGGER.debug("槽位 {}: {} x{}", i, stack.getDisplayName().getString(), stack.getCount());
            }
        }
        LOGGER.info("从法术存储中获取到 {} 个法术", spells.size());
        return spells;
    }

    public void clearSpellStorage() {
        LOGGER.info("开始清空法术存储");
        int clearedCount = 0;
        for (int i = 0; i < this.spellStorageInventory.getSlots(); i++) {
            ItemStack stack = this.spellStorageInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                LOGGER.debug("清空槽位 {}: {} x{}", i, stack.getDisplayName().getString(), stack.getCount());
                clearedCount++;
            }
            this.spellStorageInventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        LOGGER.info("法术存储清空完成，共清空 {} 个槽位", clearedCount);
    }

    public int getSlots() {
        return SLOTS;
    }
}
