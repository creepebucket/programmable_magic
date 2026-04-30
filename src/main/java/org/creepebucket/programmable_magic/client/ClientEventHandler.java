package org.creepebucket.programmable_magic.client;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import org.creepebucket.programmable_magic.gui.machines.MachineScreen;
import org.creepebucket.programmable_magic.gui.wand.WandScreen;
import org.creepebucket.programmable_magic.mananet.connectors.NetNodeBlockEntityBER;
import org.creepebucket.programmable_magic.mananet.mechines.wind_turbine.WindTurbineBlockEntityBER;
import org.creepebucket.programmable_magic.particles.client.FastDustParticle;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;
import org.creepebucket.programmable_magic.registries.ModEntityTypes;
import org.creepebucket.programmable_magic.registries.ModMenuTypes;
import org.creepebucket.programmable_magic.registries.ModParticleTypes;
import org.creepebucket.programmable_magic.renderer.SpellEntityRenderer;
import org.creepebucket.programmable_magic.renderer.api.RenderHelper;
import org.creepebucket.programmable_magic.spells.PackedSpellSpecialRenderer;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ClientEventHandler {
    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.WAND_MENU.get(),
                WandScreen::new
        );
        event.register(
                ModMenuTypes.MACHINE_MENU.get(),
                MachineScreen::new
        );
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.SPELL_ENTITY.get(), SpellEntityRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.NET_NODE_BLOCK_ENTITY.get(), context -> new NetNodeBlockEntityBER());
        event.registerBlockEntityRenderer(ModBlockEntities.WIND_TURBINE_BLOCK_ENTITY.get(), context -> new WindTurbineBlockEntityBER<>(ModBlockEntities.WIND_TURBINE_BLOCK_ENTITY.get()));
    }

    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.FAST_DUST.get(), FastDustParticle.Provider::new);
    }

    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(RenderHelper.SOLID_FACE_PIPELINE);
    }

    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(MODID, "packed_spell"),
                PackedSpellSpecialRenderer.Unbaked.MAP_CODEC
        );
    }
}
