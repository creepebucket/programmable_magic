package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 为通用物品生成简单的 generated 模型 + client items
 */
public class GeneralItemModelProvider implements DataProvider {

    private final PackOutput.PathProvider modelsProvider;
    private final PackOutput.PathProvider itemsProvider;

    public GeneralItemModelProvider(PackOutput output) {
        this.modelsProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.itemsProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (String name : List.of(
                "small_mana_cell",
                "rg_alloy_wand",
                "wand_item_placeholder"
        )) {
            // models/item/<name>.json
            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:item/generated");
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", MODID + ":item/" + name);
            model.add("textures", textures);

            Identifier id = Identifier.fromNamespaceAndPath(MODID, name);
            Path modelPath = modelsProvider.file(id, "json");
            futures.add(DataProvider.saveStable(cache, model, modelPath));

            // items/<name>.json
            JsonObject clientItem = new JsonObject();
            JsonObject modelRef = new JsonObject();
            modelRef.addProperty("type", "minecraft:model");
            modelRef.addProperty("model", MODID + ":item/" + name);
            clientItem.add("model", modelRef);
            Path itemPath = itemsProvider.file(id, "json");
            futures.add(DataProvider.saveStable(cache, clientItem, itemPath));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() { return "General Item Models"; }
}
