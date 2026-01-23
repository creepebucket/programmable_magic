package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.ui.UiMenuBase;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

/**
 * 最小菜单：不含复杂数据同步，仅承载 Screen 与槽位布局。
 * - 负责响应 Screen 上报的屏幕坐标，按当前屏幕尺寸构建物品栏/法术栏/侧栏/卷轴制作槽位。
 * - 负责在服务端保存魔杖中的法术物品堆栈，以及卷轴生成逻辑。
 */
public class WandMenu extends UiMenuBase {
    /**
     * 由网络附加数据构造（包含手持是哪只手）。
     */
    public WandMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        super(ModMenuTypes.WAND_MENU.get(), containerId, playerInv, ui -> {});
        int ord = extra.readVarInt();
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
    }
}
