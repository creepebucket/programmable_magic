package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.wand_plugins.BasePlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WandPluginRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final Map<ResourceLocation, Supplier<BasePlugin>> PLUGIN_SUPPLIERS = new HashMap<>();
    private static final Map<Supplier<Item>, Supplier<BasePlugin>> REGISTERED_PLUGINS = new HashMap<>();

    public static void registerPlugins(IEventBus eventBus) {
        // 在这里注册所有插件
        ITEMS.register(eventBus);
    }

    private static void registerPlugin(Supplier<BasePlugin> pluginSupplier) {
        BasePlugin pluginInstance = pluginSupplier.get();
        String name = pluginInstance.getRegistryName();
        Supplier<Item> itemSupplier = ITEMS.register(name,
                registryName -> new Item(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName))));

        PLUGIN_SUPPLIERS.put(ResourceLocation.fromNamespaceAndPath(MODID, name), pluginSupplier);
        REGISTERED_PLUGINS.put(itemSupplier, pluginSupplier);
    }

    public static BasePlugin createPlugin(Item item) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        Supplier<BasePlugin> supplier = PLUGIN_SUPPLIERS.get(registryName);
        return supplier != null ? supplier.get() : null;
    }

    public static boolean isPlugin(Item item) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        return PLUGIN_SUPPLIERS.containsKey(registryName);
    }

    public static Map<Supplier<Item>, Supplier<BasePlugin>> getRegisteredPlugins() {
        return REGISTERED_PLUGINS;
    }
}
