package org.creepebucket.programmable_magic.gui.machines.wind_turbine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.mananet.mechines.wind_turbine.WindTurbineBlockEntity;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class WindTurbineMenu extends MachineMenu {
	public DynamicValue<Double> airDensityBase, airDensityTempFact, airDensityPressureFact, airDensityHumidFact, airDensity;
	public DynamicValue<Double> windSpeedBase, windSpeedAltitudeFact, windSpeedTimeFact, windSpeedWeatherFact, windSpeed;
	public DynamicValue<Double> windShearExponent, power;
	public WindTurbineHooks.PowerSwitchHook powerSwitch;
	public boolean enabled_synced;

	public WindTurbineMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public WindTurbineMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
		powerSwitch = hook(new WindTurbineHooks.PowerSwitchHook(this));
	}

	public WindTurbineMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public WindTurbineMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.MACHINE_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected WindTurbineMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();
		airDensityBase = registerData("air_density_base", SyncMode.S2C, 0d);
		airDensityTempFact = registerData("air_density_temp_fact", SyncMode.S2C, 0d);
		airDensityPressureFact = registerData("air_density_pressure_fact", SyncMode.S2C, 0d);
		airDensityHumidFact = registerData("air_density_humid_fact", SyncMode.S2C, 0d);
		airDensity = registerData("air_density", SyncMode.S2C, 0d);
		windSpeedBase = registerData("wind_speed_base", SyncMode.S2C, 0d);
		windSpeedAltitudeFact = registerData("wind_speed_altitude_fact", SyncMode.S2C, 0d);
		windSpeedTimeFact = registerData("wind_speed_time_fact", SyncMode.S2C, 0d);
		windSpeedWeatherFact = registerData("wind_speed_weather_fact", SyncMode.S2C, 0d);
		windSpeed = registerData("wind_speed", SyncMode.S2C, 0d);
		windShearExponent = registerData("wind_shear_exponent", SyncMode.S2C, 0d);
		power = registerData("power", SyncMode.S2C, 0d);
	}

	@Override
	protected void onNetworkSynced() {
		var blockEntity = (WindTurbineBlockEntity) playerInv.player.level().getBlockEntity(pos);
		airDensityBase.set(blockEntity.airDensityBase);
		airDensityTempFact.set(blockEntity.airDensityTempFact);
		airDensityPressureFact.set(blockEntity.airDensityPressureFact);
		airDensityHumidFact.set(blockEntity.airDensityHumidFact);
		airDensity.set(blockEntity.airDensity);
		windSpeedBase.set(blockEntity.windSpeedBase);
		windSpeedAltitudeFact.set(blockEntity.windSpeedAltitudeFact);
		windSpeedTimeFact.set(blockEntity.windSpeedTimeFact);
		windSpeedWeatherFact.set(blockEntity.windSpeedWeatherFact);
		windSpeed.set(blockEntity.windSpeed);
		windShearExponent.set(blockEntity.windShearExponent);
		power.set(blockEntity.power);
		if (!enabled_synced) {
			enabled_synced = true;
			enabled.set(blockEntity.enabled);
		}
	}
}
