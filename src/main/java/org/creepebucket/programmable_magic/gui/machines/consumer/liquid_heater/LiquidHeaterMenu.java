package org.creepebucket.programmable_magic.gui.machines.consumer.liquid_heater;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class LiquidHeaterMenu extends MachineMenu {
	public DynamicValue<Double> conversionCost, inputSpeed, outputSpeed;
	public DynamicValue<String> inputId, outputId;

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

		inputId = registerData("input_id", SyncMode.S2C, "minecraft:water");
		outputId = registerData("output_id", SyncMode.S2C, "minecraft:lava");
	}

	@Override
	protected void onNetworkSynced() {
	}
}
