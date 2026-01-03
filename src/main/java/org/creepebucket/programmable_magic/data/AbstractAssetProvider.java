package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public abstract class AbstractAssetProvider implements DataProvider {

    protected final PackOutput.PathProvider blockstates_provider;
    protected final PackOutput.PathProvider block_models_provider;
    protected final PackOutput.PathProvider item_models_provider;
    protected final PackOutput.PathProvider items_provider;

    protected AbstractAssetProvider(PackOutput output) {
        this.blockstates_provider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.block_models_provider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.item_models_provider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.items_provider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    protected final CompletableFuture<?> save_blockstate(CachedOutput cache, String name, JsonObject json) {
        Identifier id = Identifier.fromNamespaceAndPath(MODID, name);
        Path path = blockstates_provider.file(id, "json");
        return DataProvider.saveStable(cache, json, path);
    }

    protected final CompletableFuture<?> save_block_model(CachedOutput cache, String name, JsonObject json) {
        Identifier id = Identifier.fromNamespaceAndPath(MODID, name);
        Path path = block_models_provider.file(id, "json");
        return DataProvider.saveStable(cache, json, path);
    }

    protected final CompletableFuture<?> save_item_model(CachedOutput cache, String name, JsonObject json) {
        Identifier id = Identifier.fromNamespaceAndPath(MODID, name);
        Path path = item_models_provider.file(id, "json");
        return DataProvider.saveStable(cache, json, path);
    }

    protected final CompletableFuture<?> save_item(CachedOutput cache, String name, JsonObject json) {
        Identifier id = Identifier.fromNamespaceAndPath(MODID, name);
        Path path = items_provider.file(id, "json");
        return DataProvider.saveStable(cache, json, path);
    }
}
