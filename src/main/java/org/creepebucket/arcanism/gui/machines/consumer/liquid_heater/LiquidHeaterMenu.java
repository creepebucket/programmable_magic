package org.creepebucket.arcanism.gui.machines.consumer.liquid_heater;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.arcanism.gui.lib.api.DynamicValue;
import org.creepebucket.arcanism.gui.lib.api.SyncMode;
import org.creepebucket.arcanism.gui.lib.ui.Menu;
import org.creepebucket.arcanism.gui.machines.api.MachineMenu;
import org.creepebucket.arcanism.mananet.machines.consumer.liquid_heater.LiquidHeaterBlockEntity;
import org.creepebucket.arcanism.registries.ModMenuTypes;

public class LiquidHeaterMenu extends MachineMenu {
	public DynamicValue<Double> conversionCost, inputSpeed, outputSpeed, powerFact, fuelTotalValue, fuelCurrentValue;
	public DynamicValue<String> inputId, outputId, currentFuelId;
	public DynamicValue<Boolean> inputMode;
	public boolean enabled_synced;

	public LiquidHeaterMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public LiquidHeaterMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
	}

	public LiquidHeaterMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public LiquidHeaterMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.LIQUID_HEATER_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected LiquidHeaterMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();

		conversionCost = registerData("conversion_cost", SyncMode.S2C, 12345d);
		inputSpeed = registerData("input_speed", SyncMode.S2C, 30d);
		outputSpeed = registerData("output_speed", SyncMode.S2C, 120d);
		powerFact = registerData("power_fact", SyncMode.BOTH, 1d);
		fuelTotalValue = registerData("fuel_total_value", SyncMode.S2C, 1234567d);
		fuelCurrentValue = registerData("fuel_current_value", SyncMode.S2C, 901234d);

		inputId = registerData("input_id", SyncMode.S2C, "minecraft:water");
		outputId = registerData("output_id", SyncMode.S2C, "minecraft:lava");
		currentFuelId = registerData("current_fuel_id", SyncMode.S2C, "minecraft:coal");

		inputMode = registerData("input_mode", SyncMode.BOTH, true);
	}

	@Override
	protected void onNetworkSynced() {
		var blockEntity = (LiquidHeaterBlockEntity) playerInv.player.level().getBlockEntity(pos);
		conversionCost.set(blockEntity.conversionCost);
		inputSpeed.set(blockEntity.inputSpeed);
		outputSpeed.set(blockEntity.outputSpeed);
		fuelTotalValue.set(blockEntity.fuelTotalValue);
		fuelCurrentValue.set(blockEntity.fuelCurrentValue);
		inputId.set(blockEntity.inputId);
		outputId.set(blockEntity.outputId);
		currentFuelId.set(blockEntity.currentFuelId);
		if (!enabled_synced) {
			enabled.set(blockEntity.enabled);
			powerFact.set(blockEntity.powerFact);
			inputMode.set(blockEntity.inputMode);
			enabled_synced = true;
		}
		blockEntity.powerFact = powerFact.get();
		blockEntity.inputMode = inputMode.get();
		blockEntity.setChanged();
	}
}
