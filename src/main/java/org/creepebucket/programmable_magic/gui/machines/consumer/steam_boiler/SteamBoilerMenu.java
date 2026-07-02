package org.creepebucket.programmable_magic.gui.machines.consumer.steam_boiler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class SteamBoilerMenu extends MachineMenu {

	public SteamBoilerMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public SteamBoilerMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
	}

	public SteamBoilerMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public SteamBoilerMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.STEAM_BOILER_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected SteamBoilerMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();
	}

	@Override
	protected void onNetworkSynced() {
	}
}
