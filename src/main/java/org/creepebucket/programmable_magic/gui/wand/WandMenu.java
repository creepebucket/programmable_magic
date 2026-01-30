package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.slots.InfiniteSupplySlot;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

/**
 * 最小菜单：不含复杂数据同步，仅承载 Screen 与槽位布局。
 * - 负责响应 Screen 上报的屏幕坐标，按当前屏幕尺寸构建物品栏/法术栏/侧栏/卷轴制作槽位。
 * - 负责在服务端保存魔杖中的法术物品堆栈，以及卷轴生成逻辑。
 */
public class WandMenu extends Menu {
    public SyncedValue<Integer> supplySlotDeltaY;
    public SyncedValue<Integer> supplySlotTargetDeltaY;
    public int supplySlotsStartIndex;
    public int supplySlotsCount;

    public WandMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        super(ModMenuTypes.WAND_MENU.get(), containerId, playerInv, Menu::init);
    }

    public WandMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, InteractionHand.MAIN_HAND);
    }

    public WandMenu(int containerId, Inventory playerInv, InteractionHand hand) {
        super(ModMenuTypes.WAND_MENU.get(), containerId, playerInv, Menu::init);
    }

    @Override
    public void init() {
        this.supplySlotDeltaY = dataManager.register("supply_slot_delta_y", SyncMode.BOTH, 0);
        this.supplySlotTargetDeltaY = dataManager.register("supply_slot_target_delta_y", SyncMode.BOTH, 0);

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
    }
}
