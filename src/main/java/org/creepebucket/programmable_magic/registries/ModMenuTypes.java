package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;

import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }    public static final Supplier<MenuType<WandMenu>> WAND_MENU = MENUS.register("wand_menu", () -> IMenuTypeExtension.create(WandMenu::new));


}
