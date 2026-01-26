package org.creepebucket.programmable_magic.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.creepebucket.programmable_magic.gui.lib.ui.Screen;
import org.creepebucket.programmable_magic.gui.wand.WandMenu;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.client.renderer.SpellEntityRenderer;
import org.creepebucket.programmable_magic.registries.ModEntityTypes;

public class ClientEventHandler {
    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.WAND_MENU.get(),
                (MenuScreens.ScreenConstructor<WandMenu, Screen<WandMenu>>)
                        (menu, inv, title) -> new Screen<>(menu, inv, title)
        );
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.SPELL_ENTITY.get(), SpellEntityRenderer::new);
        // 无 BER 批量注册：如需调试渲染请在自定义处注册
    }
}
