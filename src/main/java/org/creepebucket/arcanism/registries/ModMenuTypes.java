package org.creepebucket.arcanism.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.arcanism.gui.command.NetworkInfoMenu;
import org.creepebucket.arcanism.gui.machines.buffer.ManaBufferMenu;
import org.creepebucket.arcanism.gui.machines.consumer.liquid_heater.LiquidHeaterMenu;
import org.creepebucket.arcanism.gui.machines.consumer.water_pump.WaterPumpMenu;
import org.creepebucket.arcanism.gui.machines.generator.heat_exchanger.HeatExchangerMenu;
import org.creepebucket.arcanism.gui.machines.generator.pressure_relief_valve.PressureReliefValveMenu;
import org.creepebucket.arcanism.gui.machines.generator.solar_panel.SolarPanelMenu;
import org.creepebucket.arcanism.gui.machines.generator.steam_turbine.SteamTurbineMenu;
import org.creepebucket.arcanism.gui.machines.generator.wind_turbine.WindTurbineMenu;
import org.creepebucket.arcanism.gui.machines.io_dummy.IoDummyMenu;
import org.creepebucket.arcanism.gui.wand.WandMenu;

import java.util.function.Supplier;

import static org.creepebucket.arcanism.Arcanism.MODID;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

	public static final Supplier<MenuType<WandMenu>> WAND_MENU = MENUS.register("wand_menu", () -> IMenuTypeExtension.create(WandMenu::new));
	public static final Supplier<MenuType<WindTurbineMenu>> MACHINE_MENU = MENUS.register("machine_menu", () -> IMenuTypeExtension.create(WindTurbineMenu::new));
	public static final Supplier<MenuType<SolarPanelMenu>> SOLAR_PANEL_MENU = MENUS.register("solar_panel_menu", () -> IMenuTypeExtension.create(SolarPanelMenu::new));
	public static final Supplier<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_MENU = MENUS.register("heat_exchanger_menu", () -> IMenuTypeExtension.create(HeatExchangerMenu::new));
	public static final Supplier<MenuType<SteamTurbineMenu>> STEAM_TURBINE_MENU = MENUS.register("steam_turbine_menu", () -> IMenuTypeExtension.create(SteamTurbineMenu::new));
	public static final Supplier<MenuType<PressureReliefValveMenu>> PRESSURE_RELIEF_VALVE_MENU = MENUS.register("pressure_relief_valve_menu", () -> IMenuTypeExtension.create(PressureReliefValveMenu::new));
	public static final Supplier<MenuType<WaterPumpMenu>> WATER_PUMP_MENU = MENUS.register("water_pump_menu", () -> IMenuTypeExtension.create(WaterPumpMenu::new));
	public static final Supplier<MenuType<LiquidHeaterMenu>> LIQUID_HEATER_MENU = MENUS.register("liquid_heater_menu", () -> IMenuTypeExtension.create(LiquidHeaterMenu::new));
	public static final Supplier<MenuType<ManaBufferMenu>> MANA_BUFFER_MENU = MENUS.register("mana_buffer_menu", () -> IMenuTypeExtension.create(ManaBufferMenu::new));
	public static final Supplier<MenuType<NetworkInfoMenu>> NETWORK_INFO = MENUS.register("network_info", () -> IMenuTypeExtension.create(NetworkInfoMenu::new));
	public static final Supplier<MenuType<IoDummyMenu>> IO_DUMMY = MENUS.register("io_dummy", () -> IMenuTypeExtension.create(IoDummyMenu::new));


}
