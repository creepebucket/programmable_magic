package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.gui.machines.solar_panel.SolarPanelMenu;
import org.creepebucket.programmable_magic.gui.machines.wind_turbine.WindTurbineMenu;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

    public static final Supplier<MenuType<WandMenu>> WAND_MENU = MENUS.register("wand_menu", () -> IMenuTypeExtension.create(WandMenu::new));
    public static final Supplier<MenuType<WindTurbineMenu>> MACHINE_MENU = MENUS.register("machine_menu", () -> IMenuTypeExtension.create(WindTurbineMenu::new));
    public static final Supplier<MenuType<SolarPanelMenu>> SOLAR_PANEL_MENU = MENUS.register("solar_panel_menu", () -> IMenuTypeExtension.create(SolarPanelMenu::new));


}
