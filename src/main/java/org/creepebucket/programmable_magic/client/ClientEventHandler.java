package org.creepebucket.programmable_magic.client;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.creepebucket.programmable_magic.client.renderer.SpellEntityRenderer;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.registries.ModEntityTypes;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.spells.PackedSpellSpecialRenderer;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ClientEventHandler {
    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.WAND_MENU.get(),
                WandScreen::new
        );
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.SPELL_ENTITY.get(), SpellEntityRenderer::new);
        // 无 BER 批量注册：如需调试渲染请在自定义处注册
    }

    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(MODID, "packed_spell"),
                PackedSpellSpecialRenderer.Unbaked.MAP_CODEC
        );
    }
}
