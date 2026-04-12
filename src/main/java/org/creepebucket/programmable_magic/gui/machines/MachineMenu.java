package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class MachineMenu extends Menu {
    public MachineMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        this(containerId, playerInv);
    }

    public MachineMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, InteractionHand.MAIN_HAND);
    }

    public MachineMenu(int containerId, Inventory playerInv, InteractionHand hand) {
        super(ModMenuTypes.MACHINE_MENU.get(), containerId, playerInv, hand, Menu::init);
    }

    protected MachineMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
        super(type, containerId, playerInv, hand, definition);
    }

    @Override
    public void init() {

    }
}
