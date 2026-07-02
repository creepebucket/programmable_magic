package org.creepebucket.programmable_magic.gui.machines.io_dummy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;

public class IoDummyScreen extends MachineScreen<IoDummyMenu> {

	public IoDummyScreen(IoDummyMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();
	}
}
