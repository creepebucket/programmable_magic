package org.creepebucket.programmable_magic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.creepebucket.programmable_magic.client.ClientEventHandler;
import org.creepebucket.programmable_magic.registries.*;
import org.creepebucket.programmable_magic.data.ModDataGenerators;
import org.creepebucket.programmable_magic.mananet.NetworkNodeRegistrar;
import org.creepebucket.programmable_magic.mananet.nodes.ExampleRadiGeneratorNode;

@Mod(Programmable_magic.MODID)
public class Programmable_magic {
    public static final String MODID = "programmable_magic";

    public Programmable_magic(IEventBus modEventBus, ModContainer modContainer) {

        // 注册示例网络节点（在注册表挂载前收集条目）
        NetworkNodeRegistrar.register(new ExampleRadiGeneratorNode());

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        SpellRegistry.registerSpells(modEventBus);
        WandPluginRegistry.registerPlugins(modEventBus);
        ModEntityTypes.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {

            modEventBus.addListener(ClientEventHandler::registerScreen);
            modEventBus.addListener(ClientEventHandler::registerEntityRenderers);
        }
        modEventBus.register(ModNetworkPackets.class);
        // Remove: modEventBus.register(ModItemTagProvider.class);

        // Datagen
        modEventBus.addListener(ModDataGenerators::gatherData);
    }
}
