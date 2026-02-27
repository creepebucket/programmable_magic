package org.creepebucket.programmable_magic;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import org.creepebucket.programmable_magic.client.ClientEventHandler;
import org.creepebucket.programmable_magic.data.ModDataGenerators;
import org.creepebucket.programmable_magic.registries.*;

@Mod(Programmable_magic.MODID)
public class Programmable_magic {
    public static final String MODID = "programmable_magic";
    public static final TicketController SPELL_ENTITY_TICKET_CONTROLLER = new TicketController(Identifier.fromNamespaceAndPath(MODID, "spell_entity"));

    public Programmable_magic(IEventBus modEventBus, ModContainer modContainer) {

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        MananetNodeBlocks.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        SpellRegistry.registerSpells(modEventBus);
        WandPluginRegistry.registerPlugins(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModParticleTypes.register(modEventBus);
        modEventBus.addListener((RegisterTicketControllersEvent event) -> event.register(SPELL_ENTITY_TICKET_CONTROLLER));

        if (FMLEnvironment.getDist() == Dist.CLIENT) {

            WandPluginRegistry.Client.registerClientPlugins();
            modEventBus.addListener(ClientEventHandler::registerScreen);
            modEventBus.addListener(ClientEventHandler::registerEntityRenderers);
            modEventBus.addListener(ClientEventHandler::registerParticleProviders);
            modEventBus.addListener(ClientEventHandler::registerRenderPipelines);
            modEventBus.addListener(ClientEventHandler::registerSpecialModelRenderers);
        }
        modEventBus.register(ModNetworkPackets.class);

        // Datagen
        modEventBus.addListener(ModDataGenerators::gatherData);
    }
}
