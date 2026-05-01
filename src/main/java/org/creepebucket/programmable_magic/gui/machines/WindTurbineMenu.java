package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.creepebucket.programmable_magic.gui.lib.api.SyncMode;
import org.creepebucket.programmable_magic.gui.lib.api.SyncedValue;
import org.creepebucket.programmable_magic.gui.lib.ui.Menu;
import org.creepebucket.programmable_magic.mananet.mechines.wind_turbine.WindTurbineBlockEntity;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;

public class WindTurbineMenu extends Menu {
    public SyncedValue<Double> airDensityBase, airDensityTempFact, airDensityPressureFact, airDensityHumidFact, airDensity;
    public SyncedValue<Double> windSpeedBase, windSpeedAltitudeFact, windSpeedTimeFact, windSpeedWeatherFact, windSpeed;
    public SyncedValue<Double> windShearExponent, power;
    public SyncedValue<Double> radiationPowerW, temperaturePowerW, momentumPowerW, pressurePowerW;
    public SyncedValue<Double> radiationStorageJ, temperatureStorageJ, momentumStorageJ, pressureStorageJ;
    public SyncedValue<Double> radiationCacheJ, temperatureCacheJ, momentumCacheJ, pressureCacheJ;
    public SyncedValue<Boolean> enabled;
    public WindTurbineHooks.PowerSwitchHook powerSwitch;
    public BlockPos pos;
    private boolean enabled_synced;

    public WindTurbineMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extra) {
        this(containerId, playerInv, extra.readBlockPos());
    }

    public WindTurbineMenu(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv);
        this.pos = pos;
        powerSwitch = hook(new WindTurbineHooks.PowerSwitchHook(this));
        this.count = 15;
        tick();
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
