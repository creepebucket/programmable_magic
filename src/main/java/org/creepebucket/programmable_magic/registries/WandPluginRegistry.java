package org.creepebucket.programmable_magic.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.gui.wand.wand_plugins.*;
import org.creepebucket.programmable_magic.spells.plugins.SpellReleaseLogicPlugin;
import org.creepebucket.programmable_magic.spells.plugins.SpellSupplyLogicPlugin;
import org.creepebucket.programmable_magic.spells.plugins.WandPluginLogic;

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
    private static final Map<Identifier, Supplier<WandPluginLogic>> PLUGIN_SUPPLIERS = new HashMap<>();
    private static final Map<Supplier<Item>, Supplier<WandPluginLogic>> REGISTERED_PLUGINS = new HashMap<>();
    private static final Supplier<WandPluginLogic> NO_OP = WandPluginLogic::new;

    /**
     * 注册所有内置插件并挂接到事件总线。
     */
    public static void registerPlugins(IEventBus eventBus) {
        // 在这里注册所有插件（分等级）

        registerPlugin("spell_supply_t1", () -> new SpellSupplyLogicPlugin(1));
        registerPlugin("spell_supply_t2", () -> new SpellSupplyLogicPlugin(2));
        registerPlugin("spell_supply_t3", () -> new SpellSupplyLogicPlugin(3));
        registerPlugin("spell_supply_t4", () -> new SpellSupplyLogicPlugin(4));
        registerPlugin("spell_supply_t5", () -> new SpellSupplyLogicPlugin(5));

        registerPlugin("spell_storage_t1", NO_OP);
        registerPlugin("spell_storage_t2", NO_OP);
        registerPlugin("spell_storage_t3", NO_OP);
        registerPlugin("spell_storage_t4", NO_OP);

        registerPlugin("spell_release_t1", () -> new SpellReleaseLogicPlugin(1));
        registerPlugin("spell_release_t2", () -> new SpellReleaseLogicPlugin(2));
        registerPlugin("spell_release_t3", () -> new SpellReleaseLogicPlugin(3));
        registerPlugin("spell_release_t4", () -> new SpellReleaseLogicPlugin(4));
        registerPlugin("spell_release_t5", () -> new SpellReleaseLogicPlugin(5));

        registerPlugin("spell_packer", NO_OP);

        ITEMS.register(eventBus);
    }

    private static void registerPlugin(String pluginName, Supplier<WandPluginLogic> pluginSupplier) {
        String registryPath = "wand_plugin_" + pluginName;
        Supplier<Item> itemSupplier = ITEMS.register(registryPath,
                registryName -> new Item(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, registryName))));

        PLUGIN_SUPPLIERS.put(Identifier.fromNamespaceAndPath(MODID, registryPath), pluginSupplier);
        REGISTERED_PLUGINS.put(itemSupplier, pluginSupplier);
    }

    /**
     * 由物品构造对应插件实例（未注册则返回 null）。
     */
    public static WandPluginLogic getPlugin(Item item) {
        Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
        Supplier<WandPluginLogic> supplier = PLUGIN_SUPPLIERS.get(registryName);
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
    public static Map<Supplier<Item>, Supplier<WandPluginLogic>> getRegisteredPlugins() {
        return REGISTERED_PLUGINS;
    }

    public static class Client {
        private static final Map<Identifier, Supplier<BasePlugin>> CLIENT_PLUGIN_SUPPLIERS = new HashMap<>();

        public static void registerClientPlugins() {
            registerClientPlugin("spell_supply_t1", () -> new SpellSupplyPlugin(1));
            registerClientPlugin("spell_supply_t2", () -> new SpellSupplyPlugin(2));
            registerClientPlugin("spell_supply_t3", () -> new SpellSupplyPlugin(3));
            registerClientPlugin("spell_supply_t4", () -> new SpellSupplyPlugin(4));
            registerClientPlugin("spell_supply_t5", () -> new SpellSupplyPlugin(5));

            registerClientPlugin("spell_storage_t1", () -> new SpellStoragePlugin(1));
            registerClientPlugin("spell_storage_t2", () -> new SpellStoragePlugin(2));
            registerClientPlugin("spell_storage_t3", () -> new SpellStoragePlugin(3));
            registerClientPlugin("spell_storage_t4", () -> new SpellStoragePlugin(4));

            registerClientPlugin("spell_release_t1", () -> new SpellReleasePlugin(1));
            registerClientPlugin("spell_release_t2", () -> new SpellReleasePlugin(2));
            registerClientPlugin("spell_release_t3", () -> new SpellReleasePlugin(3));
            registerClientPlugin("spell_release_t4", () -> new SpellReleasePlugin(4));
            registerClientPlugin("spell_release_t5", () -> new SpellReleasePlugin(5));

            registerClientPlugin("spell_packer", SpellPackerPlugin::new);
        }

        private static void registerClientPlugin(String pluginName, Supplier<BasePlugin> pluginSupplier) {
            String registryPath = "wand_plugin_" + pluginName;
            CLIENT_PLUGIN_SUPPLIERS.put(Identifier.fromNamespaceAndPath(MODID, registryPath), pluginSupplier);
        }

        public static BasePlugin getClientLogic(Item item) {
            Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
            Supplier<BasePlugin> supplier = CLIENT_PLUGIN_SUPPLIERS.get(registryName);
            return supplier != null ? supplier.get() : null;
        }
    }
}
