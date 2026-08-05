package org.creepebucket.programmable_magic.gui.machines.generator.pressure_relief_valve;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.mananet.machines.generator.pressure_relief_valve.PressureReliefValveBlockEntity;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class PressureReliefValveMenu extends MachineMenu {
	public DynamicValue<Double> powerFact;
	public DynamicValue<Double> manaPowerW;
	public boolean enabled_synced;

	public PressureReliefValveMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public PressureReliefValveMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
	}

	public PressureReliefValveMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public PressureReliefValveMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.PRESSURE_RELIEF_VALVE_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected PressureReliefValveMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();

		powerFact = registerData("power_fact", SyncMode.BOTH, 1d);
		manaPowerW = registerData("mana_power_w", SyncMode.S2C, 0d);
	}

	@Override
	protected void onNetworkSynced() {
		var blockEntity = (PressureReliefValveBlockEntity) playerInv.player.level().getBlockEntity(pos);
		if (!enabled_synced) {
			powerFact.set(blockEntity.powerFact);
			enabled_synced = true;
			enabled.set(blockEntity.enabled);
		}
		blockEntity.powerFact = powerFact.get();
		manaPowerW.set(blockEntity.manaPowerW);
		blockEntity.setChanged();
	}
}
