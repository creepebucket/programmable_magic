package org.creepebucket.programmable_magic.items.wand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;

/**
 * 最小魔杖基类：
 * - 右键（use）在服务端打开菜单，客户端由已注册的 Screen 渲染。
 * - 暴露法术倍率、槽位数、充能功率（W）三个属性供工具提示与法术逻辑使用。
 */
public class BaseWand extends Item {

    private final double manaMult;
    private final int slots;
    private final double chargeRate;

    public BaseWand(Properties properties, double manaMult, int slots, double chargeRate) {
        super(properties);
        this.manaMult = manaMult;
        this.slots = slots;
        this.chargeRate = chargeRate;
    }

    public double getManaMult() { return manaMult; }
    public int getSlots() { return slots; }
    public double getChargeRate() { return chargeRate; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, p) -> new WandMenu(containerId, inventory),
                            Component.literal("")
                    ),
                    buf -> {}
            );
        }
        return InteractionResult.SUCCESS;
    }
}
