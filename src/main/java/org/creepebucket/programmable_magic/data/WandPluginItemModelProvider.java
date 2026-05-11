package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;
import org.creepebucket.programmable_magic.spells.plugins.WandPluginLogic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 为所有注册的魔杖插件物品生成简单的 item 模型：
 * parent: minecraft:item/generated
 * layer0: programmable_magic:item/<plugin_registry_path>
 * 同时生成 client items（1.21+ 必需）
 */
public class WandPluginItemModelProvider implements DataProvider {

    private final PackOutput.PathProvider modelsProvider;
    private final PackOutput.PathProvider itemsProvider;

    public WandPluginItemModelProvider(PackOutput output) {
        this.modelsProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.itemsProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Supplier<Item>, Supplier<WandPluginLogic>> entry : WandPluginRegistry.getRegisteredPlugins().entrySet()) {
            Item item = entry.getKey().get();
            Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
            String name = registryName.getPath();

            // models/item/<name>.json
            JsonObject modelJson = new JsonObject();
            modelJson.addProperty("parent", "minecraft:item/generated");
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", MODID + ":item/" + name);
            modelJson.add("textures", textures);

            Identifier modelId = Identifier.fromNamespaceAndPath(MODID, name);
            Path modelPath = modelsProvider.file(modelId, "json");
            futures.add(DataProvider.saveStable(cache, modelJson, modelPath));

            // items/<name>.json
            JsonObject clientItem = new JsonObject();
            JsonObject model = new JsonObject();
            model.addProperty("type", "minecraft:model");
            model.addProperty("model", MODID + ":item/" + name);
            clientItem.add("model", model);

            Identifier itemId = Identifier.fromNamespaceAndPath(MODID, name);
            Path itemPath = itemsProvider.file(itemId, "json");
            futures.add(DataProvider.saveStable(cache, clientItem, itemPath));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Wand Plugin Item Models";
    }
}
