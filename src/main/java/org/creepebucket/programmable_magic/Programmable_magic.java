package org.creepebucket.programmable_magic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.creepebucket.programmable_magic.client.ClientEventHandler;
import org.creepebucket.programmable_magic.items.wand.WandLeftClickLogic;
import org.creepebucket.programmable_magic.registries.*;
import org.creepebucket.programmable_magic.data.ModDataGenerators;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Programmable_magic.MODID)
public class Programmable_magic {
    public static final String MODID = "programmable_magic";

    public Programmable_magic(IEventBus modEventBus, ModContainer modContainer) {

        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        SpellRegistry.registerSpells(modEventBus);
        ModEntityTypes.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {

            modEventBus.addListener(ClientEventHandler::registerScreen);
            modEventBus.addListener(ClientEventHandler::registerEntityRenderers);
            NeoForge.EVENT_BUS.register(WandLeftClickLogic.class);
        }

        modEventBus.register(ModNetworkPackets.class);

        // Datagen
        modEventBus.addListener(ModDataGenerators::gatherData);
    }
}
