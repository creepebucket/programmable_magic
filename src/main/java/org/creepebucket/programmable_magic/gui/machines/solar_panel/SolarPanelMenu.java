package org.creepebucket.programmable_magic.gui.machines.solar_panel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.mananet.mechines.solar_panel.SolarPanelBlockEntity;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class SolarPanelMenu extends Menu {
    public SyncedValue<Double> solarConstant, panelArea, directIrradiance, diffuseIrradiance, atmosphericTransmittanceDiffuse;
    public SyncedValue<Double> atmosphericTransmittanceDirect, airMass, weatherFactDirect, weatherFactDiffuse, altitudeFact;
    public SyncedValue<Double> baseTemperature, cellTemperature, thermalFact, materialFact;
    public SyncedValue<Double> power, efficiencyFact;
    public SyncedValue<Double> radiationPowerW, temperaturePowerW, momentumPowerW, pressurePowerW;
    public SyncedValue<Double> radiationStorageJ, temperatureStorageJ, momentumStorageJ, pressureStorageJ;
    public SyncedValue<Double> radiationCacheJ, temperatureCacheJ, momentumCacheJ, pressureCacheJ;
    public SyncedValue<Boolean> enabled;
    public SolarPanelHooks.PowerSwitchHook powerSwitch;
    public BlockPos pos;
    private boolean enabled_synced;

    public SolarPanelMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        this(containerId, playerInv, extra.readBlockPos());
    }

    public SolarPanelMenu(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv);
        this.pos = pos;
        powerSwitch = hook(new SolarPanelHooks.PowerSwitchHook(this));
        this.count = 15;
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

    public int count;
    @Override
    public void tick() {
        if (playerInv.player.level().isClientSide()) return;
        if (count == 15) {
            count = 0;
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

            if (!enabled_synced) {
                enabled_synced = true;
                enabled.set(blockEntity.enabled);
            }
        }
        count++;
    }
}
