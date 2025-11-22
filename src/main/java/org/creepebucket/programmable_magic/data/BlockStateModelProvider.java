package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonObject;
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
 * 生成 primitive_alloy_smelter 的 blockstate 与 block/item 模型
 */
public class BlockStateModelProvider implements DataProvider {

    private final PackOutput.PathProvider blockStates;
    private final PackOutput.PathProvider blockModels;
    private final PackOutput.PathProvider itemModels;
    private final PackOutput.PathProvider clientItems;

    public BlockStateModelProvider(PackOutput output) {
        this.blockStates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.blockModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.itemModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        this.clientItems = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // blockstates/primitive_alloy_smelter.json 带朝向与状态
        JsonObject variants = new JsonObject();
        // facing: north/east/south/west with y rotation 0/90/180/270
        String[] facings = {"north", "east", "south", "west"};
        int[] ys = {0, 90, 180, 270};
        String[] statuses = {"empty", "burn", "blocked"};
        String[] models = {
                MODID + ":block/primitive_alloy_smelter_empty",
                MODID + ":block/primitive_alloy_smelter_burn",
                MODID + ":block/primitive_alloy_smelter_blocked"
        };
        for (int si = 0; si < statuses.length; si++) {
            for (int i = 0; i < facings.length; i++) {
                String key = "facing=" + facings[i] + ",status=" + statuses[si];
                variants.add(key, modelRotObj(models[si], ys[i]));
            }
        }
        JsonObject bs = new JsonObject();
        bs.add("variants", variants);
        Path bsPath = blockStates.file(ResourceLocation.fromNamespaceAndPath(MODID, "primitive_alloy_smelter"), "json");
        futures.add(DataProvider.saveStable(cache, bs, bsPath));

        // models/block/* for three states
        futures.add(saveCube(cache, "primitive_alloy_smelter_empty", "primitive_alloy_smelter_front_empty", "primitive_alloy_smelter_side_empty"));
        futures.add(saveCube(cache, "primitive_alloy_smelter_burn", "primitive_alloy_smelter_front_burn", "primitive_alloy_smelter_side_burn"));
        futures.add(saveCube(cache, "primitive_alloy_smelter_blocked", "primitive_alloy_smelter_front_blocked", "primitive_alloy_smelter_side_blocked"));

        // item model referencing empty state (默认手持空状态)
        JsonObject itemModel = new JsonObject();
        itemModel.addProperty("parent", MODID + ":block/primitive_alloy_smelter_empty");
        Path im = itemModels.file(ResourceLocation.fromNamespaceAndPath(MODID, "primitive_alloy_smelter"), "json");
        futures.add(DataProvider.saveStable(cache, itemModel, im));

        JsonObject clientItem = new JsonObject();
        JsonObject modelRef = new JsonObject();
        modelRef.addProperty("type", "minecraft:model");
        modelRef.addProperty("model", MODID + ":item/primitive_alloy_smelter");
        clientItem.add("model", modelRef);
        Path ci = clientItems.file(ResourceLocation.fromNamespaceAndPath(MODID, "primitive_alloy_smelter"), "json");
        futures.add(DataProvider.saveStable(cache, clientItem, ci));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveCube(CachedOutput cache, String modelName, String front, String side) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/cube");
        JsonObject textures = new JsonObject();
        textures.addProperty("down", "minecraft:block/bricks");
        textures.addProperty("up", "minecraft:block/bricks");
        textures.addProperty("north", MODID + ":block/" + front);
        textures.addProperty("south", MODID + ":block/" + side);
        textures.addProperty("east", MODID + ":block/" + side);
        textures.addProperty("west", MODID + ":block/" + side);
        // 破坏与粒子效果使用侧面贴图，避免默认粒子
        textures.addProperty("particle", MODID + ":block/" + side);
        model.add("textures", textures);
        Path path = blockModels.file(ResourceLocation.fromNamespaceAndPath(MODID, modelName), "json");
        return DataProvider.saveStable(cache, model, path);
    }

    private JsonObject modelObj(String model) { // 保留：无旋转
        JsonObject o = new JsonObject();
        o.addProperty("model", model);
        return o;
    }

    private JsonObject modelRotObj(String model, int y) { // 含旋转
        JsonObject o = new JsonObject();
        o.addProperty("model", model);
        if (y != 0) o.addProperty("y", y);
        return o;
    }

    @Override
    public String getName() { return "BlockState & Model Provider"; }
}
