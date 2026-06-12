package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;

public abstract class MachineMenu extends Menu {
	public SyncedValue<Double> radiationPowerW, temperaturePowerW, momentumPowerW, pressurePowerW;
	public SyncedValue<Double> radiationStorageJ, temperatureStorageJ, momentumStorageJ, pressureStorageJ;
	public SyncedValue<Double> radiationCacheJ, temperatureCacheJ, momentumCacheJ, pressureCacheJ;
	public SyncedValue<Boolean> enabled;
	public BlockPos pos;
	public int count;
	protected MachineMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	protected void setBlockPos(BlockPos pos) {
		this.pos = pos;
		this.count = 15;
	}

	protected void initNetworkData() {
		radiationPowerW = registerData("radiation_power_w", SyncMode.S2C, 0d);
		temperaturePowerW = registerData("temperature_power_w", SyncMode.S2C, 0d);
		momentumPowerW = registerData("momentum_power_w", SyncMode.S2C, 0d);
		pressurePowerW = registerData("pressure_power_w", SyncMode.S2C, 0d);
		radiationStorageJ = registerData("radiation_storage_j", SyncMode.S2C, 0d);
		temperatureStorageJ = registerData("temperature_storage_j", SyncMode.S2C, 0d);
		momentumStorageJ = registerData("momentum_storage_j", SyncMode.S2C, 0d);
		pressureStorageJ = registerData("pressure_storage_j", SyncMode.S2C, 0d);
		radiationCacheJ = registerData("radiation_cache_j", SyncMode.S2C, 0d);
		temperatureCacheJ = registerData("temperature_cache_j", SyncMode.S2C, 0d);
		momentumCacheJ = registerData("momentum_cache_j", SyncMode.S2C, 0d);
		pressureCacheJ = registerData("pressure_cache_j", SyncMode.S2C, 0d);
		enabled = registerData("enabled", SyncMode.S2C, false);
	}

	protected void syncManaData(NetNodeBlockEntity blockEntity) {
		var manaData = blockEntity.getNetworkData();
		var load = manaData.getLoad();
		var current = manaData.getCurrent();
		var cache = manaData.getCache();
		double load_to_power_w = -20000d;
		double current_to_storage_j = 1000d;

		radiationPowerW.set(load.getRadiation() * load_to_power_w);
		temperaturePowerW.set(load.getTemperature() * load_to_power_w);
		momentumPowerW.set(load.getMomentum() * load_to_power_w);
		pressurePowerW.set(load.getPressure() * load_to_power_w);
		radiationStorageJ.set(current.getRadiation() * current_to_storage_j);
		temperatureStorageJ.set(current.getTemperature() * current_to_storage_j);
		momentumStorageJ.set(current.getMomentum() * current_to_storage_j);
		pressureStorageJ.set(current.getPressure() * current_to_storage_j);
		radiationCacheJ.set(cache.getRadiation() * current_to_storage_j);
		temperatureCacheJ.set(cache.getTemperature() * current_to_storage_j);
		momentumCacheJ.set(cache.getMomentum() * current_to_storage_j);
		pressureCacheJ.set(cache.getPressure() * current_to_storage_j);
	}

	@Override
	public void tick() {
		if (playerInv.player.level().isClientSide()) return;
		if (pos == null) return;
		if (count == 15) {
			count = 0;
			var blockEntity = (NetNodeBlockEntity) playerInv.player.level().getBlockEntity(pos);
			syncManaData(blockEntity);
			onNetworkSynced();
		}
		count++;
	}

	protected void onNetworkSynced() {}
}
