package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.slots.InfiniteSupplySlot;
import org.creepebucket.programmable_magic.gui.lib.slots.OneItemOnlySlot;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * 最小菜单：不含复杂数据同步，仅承载 Screen 与槽位布局。
 * - 负责响应 Screen 上报的屏幕坐标，按当前屏幕尺寸构建物品栏/法术栏/侧栏/卷轴制作槽位。
 * - 负责在服务端保存魔杖中的法术物品堆栈，以及卷轴生成逻辑。
 */
public class WandMenu extends Menu {
    public SyncedValue<Integer> supplySlotDeltaY, supplySlotTargetDeltaY, spellSlotTargetDeltaX, packedSpellDeltaY, packedSpellTargetDeltaY;
    public WandHooks.StoredSpellsEditHook storedSpellsEditHook;
    public WandHooks.ImportSpellsHook importSpellsHook;
    public WandHooks.ClearSpellsHook clearSpellsHook;
    public int supplySlotsStartIndex;
    public int supplySlotsCount;
    public Container storedSpells;
    public List<Slot> spellStoreSlots;
    public List<Slot> hotbarSlots;
    public List<Slot> backpackSlots;
    public List<Slot> packedSpellSlots;
    public InteractionHand hand;
    public boolean quickMoved = false;

    public WandMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        this(containerId, playerInv, InteractionHand.values()[extra.readVarInt()]);
    }

    public WandMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, InteractionHand.MAIN_HAND);
    }

    public WandMenu(int containerId, Inventory playerInv, InteractionHand hand) {
        super(ModMenuTypes.WAND_MENU.get(), containerId, playerInv, Menu::init);
        this.hand = hand;

        // chatgpt 给的持久化逻辑
        if (!(this.playerInv.player instanceof ServerPlayer serverPlayer)) return;
        ItemStack wand = serverPlayer.getItemInHand(this.hand);
        List<ItemStack> saved = wand.get(ModDataComponents.SPELLS.get());
        if (saved == null) return;

        for (int i = 0; i < saved.size() && i < this.storedSpells.getContainerSize(); i++) {
            ItemStack stack = saved.get(i);
            if (!stack.isEmpty() && SpellRegistry.isSpell(stack.getItem())) this.storedSpells.setItem(i, stack.copy());
        }
    }

    @Override
    public void init() {
        this.supplySlotDeltaY = dataManager.register("supply_slot_delta_y", SyncMode.LOCAL_ONLY, 0);
        this.supplySlotTargetDeltaY = dataManager.register("supply_slot_target_delta_y", SyncMode.LOCAL_ONLY, 0);
        this.spellSlotTargetDeltaX = dataManager.register("storage_slot_target_delta_x", SyncMode.LOCAL_ONLY, 0);
        this.packedSpellDeltaY = dataManager.register("packed_spell_delta_y", SyncMode.LOCAL_ONLY, 0);
        this.packedSpellTargetDeltaY = dataManager.register("packed_spell_target_delta_y", SyncMode.LOCAL_ONLY, 0);
        this.spellStoreSlots = new ArrayList<>(1024);
        this.hotbarSlots = new ArrayList<>(9);
        this.backpackSlots = new ArrayList<>(27);
        this.packedSpellSlots = new ArrayList<>(1);

        var spells = SpellRegistry.SPELLS_BY_SUBCATEGORY;
        this.supplySlotsStartIndex = this.slots.size();
        this.supplySlotsCount = 0;

        for (String key : spells.keySet()) {
            var subCategorySpells = spells.get(key);
            for (int i = 0; i < subCategorySpells.size(); i++) {
                this.addSlot(new InfiniteSupplySlot(new ItemStack(subCategorySpells.get(i).get())));
                this.supplySlotsCount++;
            }
        }

        // 法术槽位 固定1024个
        this.storedSpells = new SimpleContainer(1024);
        this.storedSpellsEditHook = hook(new WandHooks.StoredSpellsEditHook(storedSpells));
        this.importSpellsHook = hook(new WandHooks.ImportSpellsHook(storedSpells));
        this.clearSpellsHook = hook(new WandHooks.ClearSpellsHook(storedSpells));
        for (int i = 0; i < 1024; i++) spellStoreSlots.add(addSlot(new OneItemOnlySlot(storedSpells, i, -99, -99)));

        // 背包
        for (int i = 0; i < 9; i++) hotbarSlots.add(addSlot(new Slot(playerInv, i, -99, -99)));
        for (int i = 0; i < 27; i++) backpackSlots.add(addSlot(new Slot(playerInv, 9 + i, -99, -99)));

        packedSpellSlots.add(addSlot(new Slot(new SimpleContainer(1), 0, -99, -99)));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!(player instanceof ServerPlayer)) return;

        ItemStack wand = player.getItemInHand(this.hand);
        ArrayList<ItemStack> saved = new ArrayList<>(this.storedSpells.getContainerSize());

        for (int i = 0; i < this.storedSpells.getContainerSize(); i++) {
            ItemStack stack = this.storedSpells.getItem(i);
            if (stack.isEmpty()) {
                saved.add(ItemStack.EMPTY);
                continue;
            }
            if (SpellRegistry.isSpell(stack.getItem())) {
                saved.add(stack.copy());
                this.storedSpells.setItem(i, ItemStack.EMPTY);
                continue;
            }
            saved.add(ItemStack.EMPTY);
        }

        wand.set(ModDataComponents.SPELLS.get(), saved);
        this.clearContainer(player, this.storedSpells);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.PICKUP && slotId >= 0 && this.getSlot(slotId) instanceof InfiniteSupplySlot && !this.getCarried().isEmpty()) {
            this.setCarried(ItemStack.EMPTY);
            this.broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        if (quickMoved) return ItemStack.EMPTY;
        quickMoved = true;

        var slot = getSlot(index);
        if (slot instanceof InfiniteSupplySlot) { // 是法术供应槽

            // 放到存储
            if (!moveItemStackTo(slot.getItem().copy(), supplySlotsCount, supplySlotsCount + 1024, false))
                return ItemStack.EMPTY;
            slot.set(ItemStack.EMPTY);
        } else if (slot instanceof OneItemOnlySlot) { // 是法术存储槽

            if (SpellRegistry.isSpell(slot.getItem().getItem())) {

                // 是法术就删除
                slot.set(ItemStack.EMPTY);
            } else {

                // 不是放回背包
                if (!moveItemStackTo(slot.getItem().copy(), supplySlotsCount + 1024, supplySlotsCount + 1060, false))
                    return ItemStack.EMPTY;
                slot.set(ItemStack.EMPTY);
            }
        } else { // 是背包

            // 放到存储
            if (!moveItemStackTo(slot.getItem().copy(), supplySlotsCount, supplySlotsCount + 1024, false))
                return ItemStack.EMPTY;
            slot.set(slot.getItem().copy().split(slot.getItem().getCount() - 1));
        }

        return slot.getItem().copy();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // mojang wdnmd 这其实是tick逻辑
        quickMoved = false;
    }
}
