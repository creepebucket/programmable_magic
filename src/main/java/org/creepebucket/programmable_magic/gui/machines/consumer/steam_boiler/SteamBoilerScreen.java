package org.creepebucket.programmable_magic.gui.machines.consumer.steam_boiler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.machines.api.MachineScreen;

public class SteamBoilerScreen extends MachineScreen<SteamBoilerMenu> {

	public SteamBoilerScreen(SteamBoilerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}

	@Override
	public void buildWidget() {
		super.buildWidget();
	}
}
