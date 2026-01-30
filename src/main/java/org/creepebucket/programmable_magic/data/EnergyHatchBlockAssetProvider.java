package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class EnergyHatchBlockAssetProvider extends AbstractAssetProvider {

    private static final String[] SIDE_TEXTURE = {
            null,
            "minecraft:block/iron_block",
            "minecraft:block/copper_block",
            "minecraft:block/lapis_block",
            "minecraft:block/obsidian",
            "minecraft:block/ancient_debris"
    };

    private static final String[] FRONT_TEXTURE = {
            null,
            "minecraft:block/redstone_block",
            "minecraft:block/gold_block",
            "minecraft:block/diamond_block",
            "minecraft:block/emerald_block",
            "minecraft:block/netherite_block"
    };

    public EnergyHatchBlockAssetProvider(PackOutput output) {
        super(output);
    }

    private static JsonObject variant_apply(String model, Integer x, Integer y, boolean uvlock) {
        JsonObject apply = new JsonObject();
        apply.addProperty("model", model);
        if (x != null) apply.addProperty("x", x);
        if (y != null) apply.addProperty("y", y);
        if (uvlock) apply.addProperty("uvlock", true);
        return apply;
    }

    private static JsonObject textures_for_tier(int tier) {
        String side = SIDE_TEXTURE[tier];
        String front = FRONT_TEXTURE[tier];

        JsonObject textures = new JsonObject();
        textures.addProperty("down", side);
        textures.addProperty("up", side);
        textures.addProperty("north", front);
        textures.addProperty("south", side);
        textures.addProperty("east", side);
        textures.addProperty("west", side);
        textures.addProperty("particle", side);
        return textures;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (int tier = 1; tier <= 5; tier++) futures.addAll(gen_energy_hatch(cache, tier));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private List<CompletableFuture<?>> gen_energy_hatch(CachedOutput cache, int tier) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        String name = "energy_hatch_t" + tier;

        JsonObject blockstate = new JsonObject();
        JsonObject variants = new JsonObject();
        variants.add("facing=down", variant_apply(MODID + ":block/" + name, 90, null, true));
        variants.add("facing=up", variant_apply(MODID + ":block/" + name, 270, null, true));
        variants.add("facing=north", variant_apply(MODID + ":block/" + name, null, null, true));
        variants.add("facing=south", variant_apply(MODID + ":block/" + name, null, 180, true));
        variants.add("facing=west", variant_apply(MODID + ":block/" + name, null, 270, true));
        variants.add("facing=east", variant_apply(MODID + ":block/" + name, null, 90, true));
        blockstate.add("variants", variants);
        futures.add(save_blockstate(cache, name, blockstate));

        JsonObject blockModel = new JsonObject();
        blockModel.addProperty("parent", "minecraft:block/cube");
        blockModel.add("textures", textures_for_tier(tier));
        futures.add(save_block_model(cache, name, blockModel));

        JsonObject itemModel = new JsonObject();
        itemModel.addProperty("parent", MODID + ":block/" + name);
        futures.add(save_item_model(cache, name, itemModel));

        JsonObject clientItem = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", MODID + ":item/" + name);
        clientItem.add("model", model);
        futures.add(save_item(cache, name, clientItem));

        return futures;
    }

    @Override
    public String getName() {
        return "energy_hatch_assets";
    }
}
