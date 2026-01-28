package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.client.ClientUiContext;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.lib.widgets.RectangleWidget;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollbarWidget;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

/**
 * 最小菜单：不含复杂数据同步，仅承载 Screen 与槽位布局。
 * - 负责响应 Screen 上报的屏幕坐标，按当前屏幕尺寸构建物品栏/法术栏/侧栏/卷轴制作槽位。
 * - 负责在服务端保存魔杖中的法术物品堆栈，以及卷轴生成逻辑。
 */
public class WandMenu extends Menu {
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

        /* ===========法术供应段=========== */
        SyncedValue<Integer> supplySlotDeltaY = dataManager.register("supply_slot_delta_y", SyncMode.BOTH, 0);

        // 添加法术供应槽位
        var spells = SpellRegistry.SPELLS_BY_SUBCATEGORY;

        var dx = 0;
        var dy = 0;
        var categoriesCount = 0;

        // 背景
        addClientWidget(new RectangleWidget(Coordinate.fromTopLeft(8, 0), Coordinate.fromBottomLeft(80, 0), 0x80000000));

        // 可以滚动的部分
        for (String key : spells.keySet()) {
            var subCategorySpells = spells.get(key);
            dx = 0;

            // 用于快速跳转到该类别的按钮
            int finalCategoriesCount = categoriesCount;
            addClientWidget(new wandWidgetClient.WandSubcategoryJumpButton(
                    new Coordinate((w, h) -> 0, (w, h) -> (finalCategoriesCount * h / spells.size())),
                    new Coordinate((w, h) -> 8, (w, h) -> (((finalCategoriesCount+1) * h / spells.size()) - (finalCategoriesCount * h / spells.size()))),
                    supplySlotDeltaY, -dy + 20, Component.translatable(key), ModUtils.SPELL_COLORS().getOrDefault(key, 0xFFFFFFFF)));

            // 子类别标题
            addClientWidget(new WandWidgetServer.WandSubCategoryWidget(Coordinate.fromTopLeft(dx + 8, dy), key, supplySlotDeltaY));

            // 法术
            for (int i = 0; i < subCategorySpells.size(); i++) {
                addWidget(new WandWidgetServer.SpellSupplyWidget(
                        new ItemStack(subCategorySpells.get(i).get()), Coordinate.fromTopLeft(dx % 80 + 8, dy + Math.floorDiv(dx, 80) * 16 + 32), supplySlotDeltaY
                ));

                dx += 16;
            }
            dy = dy + 64 + Math.floorDiv(dx - 16, 80) * 16; // 奇技淫巧和魔法数字的集大成者

            categoriesCount++;
        }

        int finalDy = dy;

        // 滚动交互
        addClientWidget(new WandWidgetServer.WandSupplyScrollWidget(Coordinate.fromTopLeft(8, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), 16, supplySlotDeltaY));
        // 滚动条
        addClientWidget(new ScrollbarWidget.DynamicScrollbar(Coordinate.fromTopLeft(88, 0), Coordinate.fromBottomLeft(4, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), supplySlotDeltaY, 0xFFFFFFFF, "y", true));

        /* ===========法术储存段=========== */

        // 计算目前能塞下多少法术
        SyncedValue<Integer> spellCountOnScreen = dataManager.register("spell_count_on_screen", SyncMode.BOTH, 0);
        ModUtils.ClientUtils.computeToSyncedValueIfClient(() -> () -> Math.floorDiv(ClientUiContext.mc.getWindow().getGuiScaledWidth() - 192, 16), spellCountOnScreen);
    }
}
