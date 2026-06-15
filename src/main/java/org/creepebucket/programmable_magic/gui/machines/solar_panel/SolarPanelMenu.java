package org.creepebucket.programmable_magic.gui.machines.solar_panel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.DynamicValue;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.gui.machines.api.MachineMenu;
import org.creepebucket.programmable_magic.mananet.mechines.solar_panel.SolarPanelBlockEntity;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class SolarPanelMenu extends MachineMenu {
	public DynamicValue<Double> solarConstant, panelArea, directIrradiance, diffuseIrradiance, atmosphericTransmittanceDiffuse;
	public DynamicValue<Double> atmosphericTransmittanceDirect, airMass, weatherFactDirect, weatherFactDiffuse, altitudeFact;
	public DynamicValue<Double> baseTemperature, cellTemperature, thermalFact, materialFact;
	public DynamicValue<Double> power, efficiencyFact;
	public SolarPanelHooks.PowerSwitchHook powerSwitch;
	public boolean enabled_synced;

	public SolarPanelMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
		this(containerId, playerInv, extra.readBlockPos());
	}

	public SolarPanelMenu(int containerId, Inventory playerInv, BlockPos pos) {
		this(containerId, playerInv);
		setBlockPos(pos);
		powerSwitch = hook(new SolarPanelHooks.PowerSwitchHook(this));
	}

	public SolarPanelMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, InteractionHand.MAIN_HAND);
	}

	public SolarPanelMenu(int containerId, Inventory playerInv, InteractionHand hand) {
		super(ModMenuTypes.SOLAR_PANEL_MENU.get(), containerId, playerInv, hand, Menu::init);
	}

	protected SolarPanelMenu(MenuType<?> type, int containerId, Inventory playerInv, InteractionHand hand, Definition definition) {
		super(type, containerId, playerInv, hand, definition);
	}

	@Override
	public void init() {
		initNetworkData();
		solarConstant = registerData("solar_constant", SyncMode.S2C, 0d);
		panelArea = registerData("panel_area", SyncMode.S2C, 0d);
		directIrradiance = registerData("direct_irradiance", SyncMode.S2C, 0d);
		diffuseIrradiance = registerData("diffuse_irradiance", SyncMode.S2C, 0d);
		atmosphericTransmittanceDiffuse = registerData("atmospheric_transmittance_diffuse", SyncMode.S2C, 0d);
		atmosphericTransmittanceDirect = registerData("atmospheric_transmittance_direct", SyncMode.S2C, 0d);
		airMass = registerData("air_mass", SyncMode.S2C, 0d);
		weatherFactDirect = registerData("weather_fact_direct", SyncMode.S2C, 0d);
		weatherFactDiffuse = registerData("weather_fact_diffuse", SyncMode.S2C, 0d);
		altitudeFact = registerData("altitude_fact", SyncMode.S2C, 0d);
		baseTemperature = registerData("base_temperature", SyncMode.S2C, 0d);
		cellTemperature = registerData("cell_temperature", SyncMode.S2C, 0d);
		thermalFact = registerData("thermal_fact", SyncMode.S2C, 0d);
		materialFact = registerData("material_fact", SyncMode.S2C, 0d);
		power = registerData("power", SyncMode.S2C, 0d);
		efficiencyFact = registerData("efficiency_fact", SyncMode.S2C, 0d);
	}

	@Override
	protected void onNetworkSynced() {
		var blockEntity = (SolarPanelBlockEntity) playerInv.player.level().getBlockEntity(pos);
		solarConstant.set(blockEntity.solarConstant);
		panelArea.set(blockEntity.panelArea);
		directIrradiance.set(blockEntity.directIrradiance);
		diffuseIrradiance.set(blockEntity.diffuseIrradiance);
		atmosphericTransmittanceDiffuse.set(blockEntity.atmosphericTransmittanceDiffuse);
		atmosphericTransmittanceDirect.set(blockEntity.atmosphericTransmittanceDirect);
		airMass.set(blockEntity.airMass);
		weatherFactDirect.set(blockEntity.weatherFactDirect);
		weatherFactDiffuse.set(blockEntity.weatherFactDiffuse);
		altitudeFact.set(blockEntity.altitudeFact);
		baseTemperature.set(blockEntity.baseTemperature);
		cellTemperature.set(blockEntity.cellTemperature);
		thermalFact.set(blockEntity.thermalFact);
		materialFact.set(blockEntity.materialFact);
		power.set(blockEntity.power);
		efficiencyFact.set(blockEntity.efficiencyFact);
		if (!enabled_synced) {
			enabled_synced = true;
			enabled.set(blockEntity.enabled);
		}
	}
}
