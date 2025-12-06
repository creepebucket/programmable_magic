package org.creepebucket.programmable_magic.items.wand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;

public class BaseWand extends Item {

    private final double MANA_MULT;
    private final int SLOTS;
    private final double CHARGE_RATE; // 充能速率（W：mana/s）
    public BaseWand(Properties properties, double manaMult, int slots, double chargeRate) {
        super(properties);

        MANA_MULT = manaMult;
        SLOTS = slots;
        CHARGE_RATE = chargeRate;
    }

    //常量getter
    public double getManaMult() {
        return MANA_MULT;
    }

    public int getSlots() {
        return SLOTS;
    }

    // 充能速率（mana 每秒）
    public double getChargeRate() { return CHARGE_RATE; }

    // 正常右键：打开完整界面；潜行右键：打开小槽位界面（SLOTS-5）
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean isSmall = player.isShiftKeyDown();
            int openSlots = isSmall ? Math.max(0, SLOTS - 5) : SLOTS;
            MenuProvider menuProvider = new SimpleMenuProvider(
                    (containerId, inventory, p) -> new WandMenu(containerId, inventory, ContainerLevelAccess.create(level, player.blockPosition()), openSlots, MANA_MULT, isSmall),
                    Component.translatable("gui.programmable_magic.wand_title")
            );
            serverPlayer.openMenu(menuProvider, buf -> {
                buf.writeInt(openSlots);
                buf.writeDouble(MANA_MULT);
                buf.writeBoolean(isSmall);
            });
        }

        return InteractionResult.SUCCESS;
    }

    

    //TODO: 做个小的预定义法术槽位瞬发
}
