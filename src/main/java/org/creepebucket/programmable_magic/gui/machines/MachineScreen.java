package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;

public class MachineScreen extends Screen<MachineMenu> {
    public MachineScreen(MachineMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        //addWidget(new TextButtonWidget)
    }
}
