package org.creepebucket.arcanism.gui.machines.generator.steam_turbine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.arcanism.gui.lib.api.DynamicValue;
import org.creepebucket.arcanism.gui.lib.api.SyncMode;
import org.creepebucket.arcanism.gui.lib.ui.Menu;
import org.creepebucket.arcanism.gui.machines.api.MachineMenu;
import org.creepebucket.arcanism.mananet.machines.generator.steam_turbine.SteamTurbineBlockEntity;
import org.creepebucket.arcanism.registries.ModMenuTypes;

public class SteamTurbineMenu extends MachineMenu {
	public DynamicValue<Double> powerFact;
	public DynamicValue<Double> manaPowerW;
	public DynamicValue<Boolean> voidOverflow;
	public boolean enabled_synced;

	public SteamTurbineMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public SteamTurbineMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
	}

	public SteamTurbineMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public SteamTurbineMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.STEAM_TURBINE_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected SteamTurbineMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();

		powerFact = registerData("power_fact", SyncMode.BOTH, 1d);
		manaPowerW = registerData("mana_power_w", SyncMode.S2C, 0d);
		voidOverflow = registerData("void_overflow", SyncMode.BOTH, false);
	}

	@Override
	protected void onNetworkSynced() {
		var blockEntity = (SteamTurbineBlockEntity) playerInv.player.level().getBlockEntity(pos);
		if (!enabled_synced) {
			powerFact.set(blockEntity.powerFact);
			voidOverflow.set(blockEntity.voidOverflow);
			enabled_synced = true;
			enabled.set(blockEntity.enabled);
		}
		blockEntity.powerFact = powerFact.get();
		blockEntity.voidOverflow = voidOverflow.get();
		manaPowerW.set(blockEntity.manaPowerW);
		blockEntity.setChanged();
	}
}
