package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.creepebucket.programmable_magic.gui.lib.api.Coordinate;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.lib.widgets.ScrollbarWidget;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.util.Map;

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

        // 法术供应段
        SyncedValue<Integer> supplySlotDeltaY = dataManager.register("supply_slot_delta_y", SyncMode.BOTH, 0);

        // 添加法术供应槽位
        var spells = SpellRegistry.SPELLS_BY_SUBCATEGORY;
        var dx = 0;
        var dy = 0;

        // 可以滚动的部分
        for (String key : spells.keySet()) {
            dx = 0;

            addClientWidget(new WandWidgets.WandSubCategoryWidget(Coordinate.fromTopLeft(dx, dy), key, supplySlotDeltaY));

            for (int i = 0; i < spells.get(key).size(); i++) {
                addWidget(new WandWidgets.SpellSupplyWidget(
                        new ItemStack(spells.get(key).get(i).get()), Coordinate.fromTopLeft(dx % 80, dy + Math.floorDiv(dx, 80) * 16 + 32), supplySlotDeltaY
                ));

                dx = dx + 16;
            }
            dy = dy + 64 + Math.floorDiv(dx - 16, 80) * 16; // 奇技淫巧和魔法数字的集大成者
        }

        int finalDy = dy;

        addClientWidget(new WandWidgets.WandSupplyScrollWidget(Coordinate.fromTopLeft(0, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), 16, supplySlotDeltaY));
        addClientWidget(new ScrollbarWidget.DynamicScrollbar(Coordinate.fromTopLeft(80, 0), Coordinate.fromBottomLeft(4, 0),
                new Coordinate((w, h) -> (-finalDy + h), (w, h) -> 0), supplySlotDeltaY, 0xFFFFFFFF, "y", true));

    }
}
