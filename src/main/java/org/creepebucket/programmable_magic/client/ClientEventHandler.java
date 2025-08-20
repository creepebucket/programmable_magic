package org.creepebucket.programmable_magic.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.gui.wand.WandSpellStorageMenu;
import org.creepebucket.programmable_magic.gui.wand.WandSpellStorageScreen;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.client.renderer.SpellEntityRenderer;
import org.creepebucket.programmable_magic.registries.ModEntityTypes;

public class ClientEventHandler {
    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.WAND_MENU.get(), WandScreen::new);
        event.register(ModMenuTypes.WAND_SPELL_STORAGE_MENU.get(), WandSpellStorageScreen::new);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.SPELL_ENTITY.get(), SpellEntityRenderer::new);
    }
}
