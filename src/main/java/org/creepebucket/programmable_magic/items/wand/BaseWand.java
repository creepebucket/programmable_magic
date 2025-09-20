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

    public BaseWand(Properties properties, double manaMult, int slots) {
        super(properties);

        MANA_MULT = manaMult;
        SLOTS = slots;
    }

    //常量getter
    public double getManaMult() {
        return MANA_MULT;
    }

    public int getSlots() {
        return SLOTS;
    }

    //右键打开gui
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider menuProvider = new SimpleMenuProvider(
                    (containerId, inventory, p) -> new WandMenu(containerId, inventory, ContainerLevelAccess.create(level, player.blockPosition()), SLOTS, MANA_MULT),
                    Component.translatable("gui.programmable_magic.wand_title")
            );
            serverPlayer.openMenu(menuProvider, buf -> {
                buf.writeInt(SLOTS);
                buf.writeDouble(MANA_MULT);
            });
        }

        return InteractionResult.SUCCESS;
    }

    //TODO: 做个小的预定义法术槽位瞬发
}
