package org.creepebucket.programmable_magic.gui.machines.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.mananet.machines.buffer.ManaBufferBlockEntity;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class ManaBufferMenu extends MachineMenu {
	public DynamicValue<Double> baseStorage, baseExpansion, baseExpansionPower, maxChargePower;
	public DynamicValue<Integer> chargeSlotCount;
	public DynamicValue<Double> powerFact;
	public boolean enabled_synced;

	public ManaBufferMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public ManaBufferMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
	}

	public ManaBufferMenu(int containerId, Inventory playerInv, BlockPos pos, double baseStorage, double baseExpansion, double baseExpansionPower, double maxChargePower, int chargeSlotCount) {
		this(containerId, playerInv);
		setBlockPos(pos);
		this.baseStorage.set(baseStorage);
		this.baseExpansion.set(baseExpansion);
		this.baseExpansionPower.set(baseExpansionPower);
		this.maxChargePower.set(maxChargePower);
		this.chargeSlotCount.set(chargeSlotCount);
	}

	public ManaBufferMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public ManaBufferMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.MANA_BUFFER_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected ManaBufferMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();

		baseStorage = registerData("base_storage", SyncMode.S2C, 0d);
		baseExpansion = registerData("base_expansion", SyncMode.S2C, 0d);
		baseExpansionPower = registerData("base_expansion_power", SyncMode.S2C, 0d);
		maxChargePower = registerData("max_charge_power", SyncMode.S2C, 0d);
		chargeSlotCount = registerData("charge_slot_count", SyncMode.S2C, 0);
		powerFact = registerData("power_fact", SyncMode.BOTH, 1d);
	}

	@Override
	protected void onNetworkSynced() {
		var blockEntity = (ManaBufferBlockEntity) playerInv.player.level().getBlockEntity(pos);
		baseStorage.set(blockEntity.baseStorage);
		baseExpansion.set(blockEntity.baseExpansion);
		baseExpansionPower.set(blockEntity.baseExpansionPower);
		maxChargePower.set(blockEntity.maxChargePower);
		chargeSlotCount.set(blockEntity.chargeSlotCount);
		if (!enabled_synced) {
			powerFact.set(blockEntity.powerFact);
			enabled_synced = true;
			enabled.set(blockEntity.enabled);
		}
		blockEntity.powerFact = powerFact.get();
		blockEntity.setChanged();
	}
}
