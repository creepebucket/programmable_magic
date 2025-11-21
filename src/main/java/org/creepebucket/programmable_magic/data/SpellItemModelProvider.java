package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.creepebucket.programmable_magic.registries.SpellRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 为所有注册的法术物品生成简单的 item 模型：
 * parent: minecraft:item/generated
 * layer0: programmable_magic:spell/<spell_registry_name>
 * 模型文件名：models/item/spell_display_<spell_registry_name>.json
 */
public class SpellItemModelProvider implements DataProvider {

    private final PackOutput output;
    private final PackOutput.PathProvider modelsProvider;
    private final PackOutput.PathProvider itemsProvider;

    public SpellItemModelProvider(PackOutput output) {
        this.output = output;
        // 通过 PathProvider 确保路径与资源包结构一致（assets/<modid>/models/item/...）
        this.modelsProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.itemsProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<java.util.function.Supplier<Item>, java.util.function.Supplier<org.creepebucket.programmable_magic.spells.SpellItemLogic>> entry : SpellRegistry.getRegisteredSpells().entrySet()) {
            String spellName = entry.getValue().get().getRegistryName();
            String itemModelName = "spell_display_" + spellName;

            // 1) 生成 models/item/spell_display_<name>.json
            JsonObject modelJson = new JsonObject();
            modelJson.addProperty("parent", "minecraft:item/generated");
            JsonObject textures = new JsonObject();
            // 使用物品图集路径：assets/<modid>/textures/item/spell/<spellName>.png
            textures.addProperty("layer0", MODID + ":item/spell/" + spellName);
            modelJson.add("textures", textures);

            ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(MODID, itemModelName);
            Path modelPath = modelsProvider.file(modelId, "json");
            futures.add(DataProvider.saveStable(cache, modelJson, modelPath));
            // 兼容清理：删除此前错误生成的双点扩展文件（如果存在）
            Path wrong = modelsProvider.file(modelId, ".json");
            try { Files.deleteIfExists(wrong); } catch (Exception ignored) {}

            // 2) 生成 1.21.8 必需的 client item：items/spell_display_<name>.json
            JsonObject clientItem = new JsonObject();
            JsonObject model = new JsonObject();
            model.addProperty("type", "minecraft:model");
            model.addProperty("model", MODID + ":item/" + itemModelName);
            clientItem.add("model", model);

            ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(MODID, itemModelName);
            Path itemPath = itemsProvider.file(itemId, "json");
            futures.add(DataProvider.saveStable(cache, clientItem, itemPath));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Spell Item Models";
    }
}
