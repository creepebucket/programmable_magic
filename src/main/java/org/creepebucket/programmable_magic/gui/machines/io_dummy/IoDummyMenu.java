package org.creepebucket.programmable_magic.gui.machines.io_dummy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class IoDummyMenu extends MachineMenu {

	public IoDummyMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public IoDummyMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
	}

	public IoDummyMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public IoDummyMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.IO_DUMMY.get(), containerId, playerInv, hand, Menu::init);
	}

	@Override
	public void init() {
		initNetworkData();
	}

	@Override
	protected void onNetworkSynced() {
	}
}
