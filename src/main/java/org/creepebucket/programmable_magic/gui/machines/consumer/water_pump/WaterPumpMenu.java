package org.creepebucket.programmable_magic.gui.machines.consumer.water_pump;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.mananet.machines.consumer.water_pump.WaterPumpBlockEntity;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class WaterPumpMenu extends MachineMenu {
	public DynamicValue<Double> power;
	public DynamicValue<Double> powerFact;
	public boolean enabled_synced;

	public WaterPumpMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public WaterPumpMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
	}

	public WaterPumpMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public WaterPumpMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.WATER_PUMP_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected WaterPumpMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();
		power = registerData("power", SyncMode.S2C, 0d);
		powerFact = registerData("power_fact", SyncMode.BOTH, 1d);
	}

	@Override
	protected void onNetworkSynced() {
		var blockEntity = (WaterPumpBlockEntity) playerInv.player.level().getBlockEntity(pos);
		power.set(blockEntity.power);
		if (!enabled_synced) {
			powerFact.set(blockEntity.powerFact);
			enabled_synced = true;
			enabled.set(blockEntity.enabled);
		}
		blockEntity.powerFact = powerFact.get();
		blockEntity.setChanged();
	}
}
