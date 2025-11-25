package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * Mana Cable 的 blockstate 与模型 Datagen（独立 Provider）。
 * 基于用户提供的三份几何：中心 / 北部 / 底部。
 */
public class ManaCableModelProvider implements DataProvider {

    private final PackOutput.PathProvider blockStates;
    private final PackOutput.PathProvider blockModels;
    private final PackOutput.PathProvider itemModels;
    private final PackOutput.PathProvider clientItems;

    public ManaCableModelProvider(PackOutput output) {
        this.blockStates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.blockModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.itemModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.clientItems = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> fs = new ArrayList<>();

        // 直接用字符串样本生成（便于拷贝粘贴），KISS
        Path bsPath = blockStates.file(ResourceLocation.fromNamespaceAndPath(MODID, "mana_cable"), "json");
        fs.add(DataProvider.saveStable(cache, parse(ManaCableJsonSamples.BLOCKSTATE), bsPath));

        Path centerPath = blockModels.file(ResourceLocation.fromNamespaceAndPath(MODID, "mana_cable_center"), "json");
        fs.add(DataProvider.saveStable(cache, parse(ManaCableJsonSamples.MODEL_CENTER), centerPath));

        Path centerConnPath = blockModels.file(ResourceLocation.fromNamespaceAndPath(MODID, "mana_cable_center_conn"), "json");
        fs.add(DataProvider.saveStable(cache, parse(ManaCableJsonSamples.MODEL_CENTER_CONN), centerConnPath));

        Path sidePath = blockModels.file(ResourceLocation.fromNamespaceAndPath(MODID, "mana_cable_side"), "json");
        fs.add(DataProvider.saveStable(cache, parse(ManaCableJsonSamples.MODEL_SIDE), sidePath));

        Path downPath = blockModels.file(ResourceLocation.fromNamespaceAndPath(MODID, "mana_cable_down"), "json");
        fs.add(DataProvider.saveStable(cache, parse(ManaCableJsonSamples.MODEL_DOWN), downPath));

        Path im = itemModels.file(ResourceLocation.fromNamespaceAndPath(MODID, "mana_cable"), "json");
        fs.add(DataProvider.saveStable(cache, parse(ManaCableJsonSamples.MODEL_ITEM), im));

        // 客户端 items 包（NeoForge 客户端物品模型映射）
        Path ci = clientItems.file(ResourceLocation.fromNamespaceAndPath(MODID, "mana_cable"), "json");
        fs.add(DataProvider.saveStable(cache, parse(ManaCableJsonSamples.CLIENT_ITEM), ci));

        return CompletableFuture.allOf(fs.toArray(CompletableFuture[]::new));
    }

    private static JsonObject parse(String s) { return JsonParser.parseString(s).getAsJsonObject(); }

    @Override
    public String getName() { return "Mana Cable Model & BlockState Provider"; }
}
