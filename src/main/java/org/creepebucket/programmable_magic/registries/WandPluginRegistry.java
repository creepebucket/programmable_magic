package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.wand_plugins.BasePlugin;
import org.creepebucket.programmable_magic.wand_plugins.TestPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 魔杖插件注册表：
 * - 每个插件对应一个物品，物品注册名与插件创建器一一映射。
 * - 通过 isPlugin/getPlugin 判定与构造插件实例。
 */
public class WandPluginRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final Map<Identifier, Supplier<BasePlugin>> PLUGIN_SUPPLIERS = new HashMap<>();
    private static final Map<Supplier<Item>, Supplier<BasePlugin>> REGISTERED_PLUGINS = new HashMap<>();

    /**
     * 注册所有内置插件并挂接到事件总线。
     */
    public static void registerPlugins(IEventBus eventBus) {
        // 在这里注册所有插件（分等级）

        registerPlugin(TestPlugin::new);

        ITEMS.register(eventBus);
    }

    private static void registerPlugin(Supplier<BasePlugin> pluginSupplier) {
        BasePlugin pluginInstance = pluginSupplier.get();
        String registryPath = pluginInstance.getRegistryName();
        Supplier<Item> itemSupplier = ITEMS.register(registryPath,
                registryName -> new Item(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName))));

        PLUGIN_SUPPLIERS.put(Identifier.fromNamespaceAndPath(MODID, registryPath), pluginSupplier);
        REGISTERED_PLUGINS.put(itemSupplier, pluginSupplier);
    }

    /**
     * 由物品构造对应插件实例（未注册则返回 null）。
     */
    public static BasePlugin getPlugin(Item item) {
        Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
        Supplier<BasePlugin> supplier = PLUGIN_SUPPLIERS.get(registryName);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * 判断物品是否为插件物品。
     */
    public static boolean isPlugin(Item item) {
        Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
        return PLUGIN_SUPPLIERS.containsKey(registryName);
    }

    /**
     * 返回已注册插件的物品与创建器映射表。
     */
    public static Map<Supplier<Item>, Supplier<BasePlugin>> getRegisteredPlugins() {
        return REGISTERED_PLUGINS;
    }
}
