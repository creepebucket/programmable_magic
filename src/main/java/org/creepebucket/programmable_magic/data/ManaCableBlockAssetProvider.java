package org.creepebucket.programmable_magic.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class ManaCableBlockAssetProvider extends AbstractAssetProvider {

    public ManaCableBlockAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.addAll(gen_mana_cable(cache));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private List<CompletableFuture<?>> gen_mana_cable(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // models/block/mana_cable_core.json
        JsonObject coreModel = new JsonObject();
        coreModel.add("textures", textures_block("block/mana_cable"));
        coreModel.add("elements", elements(List.of(
                element_cube("center", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), faces_all(uv(0, 0, 6, 6), "#0"))
        )));
        futures.add(save_block_model(cache, "mana_cable_core", coreModel));

        JsonObject core2Model = new JsonObject();
        core2Model.add("textures", textures_block("block/mana_cable"));
        JsonObject core2Faces = new JsonObject();
        core2Faces.add("north", face(uv(2, 10, 8, 16), "#0", null));
        core2Faces.add("east", face(uv(4, 10, 10, 16), "#0", null));
        core2Faces.add("south", face(uv(10, 10, 16, 16), "#0", null));
        core2Faces.add("west", face(uv(5, 10, 11, 16), "#0", null));
        core2Faces.add("up", face(uv(4, 10, 10, 16), "#0", null));
        core2Faces.add("down", face(uv(0, 10, 6, 16), "#0", null));
        core2Model.add("elements", elements(List.of(
                element_cube("center2", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), core2Faces)
        )));
        futures.add(save_block_model(cache, "mana_cable_core2", core2Model));

        JsonObject core2SingleNorthModel = new JsonObject();
        core2SingleNorthModel.add("textures", textures_block("block/mana_cable"));
        JsonObject core2SingleNorthFaces = new JsonObject();
        core2SingleNorthFaces.add("north", face(uv(2, 10, 8, 16), "#0", null));
        core2SingleNorthFaces.add("east", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleNorthFaces.add("west", face(uv(5, 10, 11, 16), "#0", null));
        core2SingleNorthFaces.add("up", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleNorthFaces.add("down", face(uv(0, 10, 6, 16), "#0", null));
        core2SingleNorthModel.add("elements", elements(List.of(
                element_cube("center2", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), core2SingleNorthFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_core2_single_north", core2SingleNorthModel));

        JsonObject core2SingleEastModel = new JsonObject();
        core2SingleEastModel.add("textures", textures_block("block/mana_cable"));
        JsonObject core2SingleEastFaces = new JsonObject();
        core2SingleEastFaces.add("north", face(uv(2, 10, 8, 16), "#0", null));
        core2SingleEastFaces.add("east", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleEastFaces.add("south", face(uv(10, 10, 16, 16), "#0", null));
        core2SingleEastFaces.add("up", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleEastFaces.add("down", face(uv(0, 10, 6, 16), "#0", null));
        core2SingleEastModel.add("elements", elements(List.of(
                element_cube("center2", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), core2SingleEastFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_core2_single_east", core2SingleEastModel));

        JsonObject core2SingleSouthModel = new JsonObject();
        core2SingleSouthModel.add("textures", textures_block("block/mana_cable"));
        JsonObject core2SingleSouthFaces = new JsonObject();
        core2SingleSouthFaces.add("east", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleSouthFaces.add("south", face(uv(10, 10, 16, 16), "#0", null));
        core2SingleSouthFaces.add("west", face(uv(5, 10, 11, 16), "#0", null));
        core2SingleSouthFaces.add("up", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleSouthFaces.add("down", face(uv(0, 10, 6, 16), "#0", null));
        core2SingleSouthModel.add("elements", elements(List.of(
                element_cube("center2", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), core2SingleSouthFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_core2_single_south", core2SingleSouthModel));

        JsonObject core2SingleWestModel = new JsonObject();
        core2SingleWestModel.add("textures", textures_block("block/mana_cable"));
        JsonObject core2SingleWestFaces = new JsonObject();
        core2SingleWestFaces.add("north", face(uv(2, 10, 8, 16), "#0", null));
        core2SingleWestFaces.add("south", face(uv(10, 10, 16, 16), "#0", null));
        core2SingleWestFaces.add("west", face(uv(5, 10, 11, 16), "#0", null));
        core2SingleWestFaces.add("up", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleWestFaces.add("down", face(uv(0, 10, 6, 16), "#0", null));
        core2SingleWestModel.add("elements", elements(List.of(
                element_cube("center2", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), core2SingleWestFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_core2_single_west", core2SingleWestModel));

        JsonObject core2SingleUpModel = new JsonObject();
        core2SingleUpModel.add("textures", textures_block("block/mana_cable"));
        JsonObject core2SingleUpFaces = new JsonObject();
        core2SingleUpFaces.add("north", face(uv(2, 10, 8, 16), "#0", null));
        core2SingleUpFaces.add("east", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleUpFaces.add("south", face(uv(10, 10, 16, 16), "#0", null));
        core2SingleUpFaces.add("west", face(uv(5, 10, 11, 16), "#0", null));
        core2SingleUpFaces.add("up", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleUpModel.add("elements", elements(List.of(
                element_cube("center2", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), core2SingleUpFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_core2_single_up", core2SingleUpModel));

        JsonObject core2SingleDownModel = new JsonObject();
        core2SingleDownModel.add("textures", textures_block("block/mana_cable"));
        JsonObject core2SingleDownFaces = new JsonObject();
        core2SingleDownFaces.add("north", face(uv(2, 10, 8, 16), "#0", null));
        core2SingleDownFaces.add("east", face(uv(4, 10, 10, 16), "#0", null));
        core2SingleDownFaces.add("south", face(uv(10, 10, 16, 16), "#0", null));
        core2SingleDownFaces.add("west", face(uv(5, 10, 11, 16), "#0", null));
        core2SingleDownFaces.add("down", face(uv(0, 10, 6, 16), "#0", null));
        core2SingleDownModel.add("elements", elements(List.of(
                element_cube("center2", 5, 5, 5, 11, 11, 11, rotation(0, "y", 5, 5, 5), core2SingleDownFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_core2_single_down", core2SingleDownModel));

        // models/block/mana_cable_arm.json（基准方向：north）
        JsonObject armNorthModel = new JsonObject();
        armNorthModel.add("textures", textures_block("block/mana_cable"));
        JsonObject armNorthFaces = new JsonObject();
        armNorthFaces.add("north", face(uv(0, 0, 6, 6), "#0", null));
        armNorthFaces.add("east", face(uv(8, 10, 13, 16), "#0", null));
        armNorthFaces.add("west", face(uv(0, 10, 5, 16), "#0", null));
        armNorthFaces.add("up", face(uv(11, 10, 16, 16), "#0", 90));
        armNorthFaces.add("down", face(uv(4, 10, 9, 16), "#0", 90));
        armNorthModel.add("elements", elements(List.of(
                element_cube("north_connected", 5, 5, 0, 11, 11, 5, rotation(0, "y", 5, 5, 0), armNorthFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_arm", armNorthModel));

        JsonObject armEastModel = new JsonObject();
        armEastModel.add("textures", textures_block("block/mana_cable"));
        JsonObject armEastFaces = new JsonObject();
        armEastFaces.add("east", face(uv(0, 0, 6, 6), "#0", null));
        armEastFaces.add("south", face(uv(8, 10, 13, 16), "#0", null));
        armEastFaces.add("north", face(uv(0, 10, 5, 16), "#0", null));
        armEastFaces.add("up", face(uv(11, 10, 16, 16), "#0", 90));
        armEastFaces.add("down", face(uv(4, 10, 9, 16), "#0", 90));
        armEastModel.add("elements", elements(List.of(
                element_cube("east_connected", 11, 5, 5, 16, 11, 11, rotation(0, "y", 11, 5, 5), armEastFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_arm_east", armEastModel));

        JsonObject armSouthModel = new JsonObject();
        armSouthModel.add("textures", textures_block("block/mana_cable"));
        JsonObject armSouthFaces = new JsonObject();
        armSouthFaces.add("south", face(uv(0, 0, 6, 6), "#0", null));
        armSouthFaces.add("east", face(uv(8, 10, 13, 16), "#0", null));
        armSouthFaces.add("west", face(uv(0, 10, 5, 16), "#0", null));
        armSouthFaces.add("up", face(uv(11, 10, 16, 16), "#0", 90));
        armSouthFaces.add("down", face(uv(4, 10, 9, 16), "#0", 90));
        armSouthModel.add("elements", elements(List.of(
                element_cube("south_connected", 5, 5, 11, 11, 11, 16, rotation(0, "y", 5, 5, 11), armSouthFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_arm_south", armSouthModel));

        JsonObject armWestModel = new JsonObject();
        armWestModel.add("textures", textures_block("block/mana_cable"));
        JsonObject armWestFaces = new JsonObject();
        armWestFaces.add("west", face(uv(0, 0, 6, 6), "#0", null));
        armWestFaces.add("south", face(uv(0, 10, 5, 16), "#0", null));
        armWestFaces.add("north", face(uv(8, 10, 13, 16), "#0", null));
        armWestFaces.add("up", face(uv(11, 10, 16, 16), "#0", 90));
        armWestFaces.add("down", face(uv(4, 10, 9, 16), "#0", 90));
        armWestModel.add("elements", elements(List.of(
                element_cube("west_connected", 0, 5, 5, 5, 11, 11, rotation(0, "y", 0, 5, 5), armWestFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_arm_west", armWestModel));

        JsonObject armUpModel = new JsonObject();
        armUpModel.add("textures", textures_block("block/mana_cable"));
        JsonObject armUpFaces = new JsonObject();
        armUpFaces.add("up", face(uv(0, 0, 6, 6), "#0", null));
        armUpFaces.add("east", face(uv(8, 10, 13, 16), "#0", null));
        armUpFaces.add("west", face(uv(0, 10, 5, 16), "#0", null));
        armUpFaces.add("north", face(uv(11, 10, 16, 16), "#0", 90));
        armUpFaces.add("south", face(uv(4, 10, 9, 16), "#0", 90));
        armUpModel.add("elements", elements(List.of(
                element_cube("up_connected", 5, 11, 5, 11, 16, 11, rotation(0, "y", 5, 11, 5), armUpFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_arm_up", armUpModel));

        JsonObject armDownModel = new JsonObject();
        armDownModel.add("textures", textures_block("block/mana_cable"));
        JsonObject armDownFaces = new JsonObject();
        armDownFaces.add("down", face(uv(0, 0, 6, 6), "#0", null));
        armDownFaces.add("east", face(uv(8, 10, 13, 16), "#0", null));
        armDownFaces.add("west", face(uv(0, 10, 5, 16), "#0", null));
        armDownFaces.add("north", face(uv(4, 10, 9, 16), "#0", 90));
        armDownFaces.add("south", face(uv(11, 10, 16, 16), "#0", 90));
        armDownModel.add("elements", elements(List.of(
                element_cube("down_connected", 5, 0, 5, 11, 5, 11, rotation(0, "y", 5, 0, 5), armDownFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_arm_down", armDownModel));

        JsonObject capNorthModel = new JsonObject();
        capNorthModel.add("textures", textures_block("block/mana_cable"));
        JsonObject capNorthFaces = new JsonObject();
        capNorthFaces.add("south", face(uv(0, 0, 6, 6), "#0", null));
        capNorthModel.add("elements", elements(List.of(
                element_cube("north_connected2", 5, 5, 11, 11, 11, 11, rotation(0, "y", 5, 5, 11), capNorthFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_cap_north", capNorthModel));

        JsonObject capEastModel = new JsonObject();
        capEastModel.add("textures", textures_block("block/mana_cable"));
        JsonObject capEastFaces = new JsonObject();
        capEastFaces.add("west", face(uv(0, 0, 6, 6), "#0", null));
        capEastModel.add("elements", elements(List.of(
                element_cube("east_connected2", 5, 5, 5, 5, 11, 11, rotation(0, "y", 5, 5, 5), capEastFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_cap_east", capEastModel));

        JsonObject capSouthModel = new JsonObject();
        capSouthModel.add("textures", textures_block("block/mana_cable"));
        JsonObject capSouthFaces = new JsonObject();
        capSouthFaces.add("north", face(uv(0, 0, 6, 6), "#0", null));
        capSouthModel.add("elements", elements(List.of(
                element_cube("south_connected2", 5, 5, 5, 11, 11, 5, rotation(0, "y", 5, 5, 5), capSouthFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_cap_south", capSouthModel));

        JsonObject capWestModel = new JsonObject();
        capWestModel.add("textures", textures_block("block/mana_cable"));
        JsonObject capWestFaces = new JsonObject();
        capWestFaces.add("east", face(uv(0, 0, 6, 6), "#0", null));
        capWestModel.add("elements", elements(List.of(
                element_cube("west_connected2", 11, 5, 5, 11, 11, 11, rotation(0, "y", 11, 5, 5), capWestFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_cap_west", capWestModel));

        JsonObject capUpModel = new JsonObject();
        capUpModel.add("textures", textures_block("block/mana_cable"));
        JsonObject capUpFaces = new JsonObject();
        capUpFaces.add("down", face(uv(0, 0, 6, 6), "#0", null));
        capUpModel.add("elements", elements(List.of(
                element_cube("up_connected2", 5, 5, 5, 11, 5, 11, rotation(0, "y", 5, 5, 5), capUpFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_cap_up", capUpModel));

        JsonObject capDownModel = new JsonObject();
        capDownModel.add("textures", textures_block("block/mana_cable"));
        JsonObject capDownFaces = new JsonObject();
        capDownFaces.add("up", face(uv(0, 0, 6, 6), "#0", null));
        capDownModel.add("elements", elements(List.of(
                element_cube("down_connected2", 5, 11, 5, 11, 11, 11, rotation(0, "y", 5, 11, 5), capDownFaces)
        )));
        futures.add(save_block_model(cache, "mana_cable_cap_down", capDownModel));

        // blockstates/mana_cable.json（multipart：center/center2 + 6 向 north_connected + 单连接 north_connected2）
        JsonObject blockstate = new JsonObject();
        JsonArray multipart = new JsonArray();

        List<JsonObject> center2States = new ArrayList<>();
        for (int mask = 0; mask < 64; mask++) {
            boolean down = (mask & 0b000001) != 0;
            boolean up = (mask & 0b000010) != 0;
            boolean north = (mask & 0b000100) != 0;
            boolean east = (mask & 0b001000) != 0;
            boolean south = (mask & 0b010000) != 0;
            boolean west = (mask & 0b100000) != 0;
            JsonObject when = when_state(north, east, south, west, up, down);
            if (Integer.bitCount(mask) >= 2) center2States.add(when);
        }

        multipart.add(part(when_state(false, false, false, false, false, false), apply(MODID + ":block/mana_cable_core", null, null, true)));
        multipart.add(part(when_state(true, false, false, false, false, false), apply(MODID + ":block/mana_cable_core2_single_north", null, null, true)));
        multipart.add(part(when_state(false, true, false, false, false, false), apply(MODID + ":block/mana_cable_core2_single_east", null, null, true)));
        multipart.add(part(when_state(false, false, true, false, false, false), apply(MODID + ":block/mana_cable_core2_single_south", null, null, true)));
        multipart.add(part(when_state(false, false, false, true, false, false), apply(MODID + ":block/mana_cable_core2_single_west", null, null, true)));
        multipart.add(part(when_state(false, false, false, false, true, false), apply(MODID + ":block/mana_cable_core2_single_up", null, null, true)));
        multipart.add(part(when_state(false, false, false, false, false, true), apply(MODID + ":block/mana_cable_core2_single_down", null, null, true)));
        multipart.add(part(when_or(center2States), apply(MODID + ":block/mana_cable_core2", null, null, true)));

        multipart.add(part(when_true("north"), apply(MODID + ":block/mana_cable_arm", null, null, true)));
        multipart.add(part(when_true("east"), apply(MODID + ":block/mana_cable_arm_east", null, null, true)));
        multipart.add(part(when_true("south"), apply(MODID + ":block/mana_cable_arm_south", null, null, true)));
        multipart.add(part(when_true("west"), apply(MODID + ":block/mana_cable_arm_west", null, null, true)));
        multipart.add(part(when_true("up"), apply(MODID + ":block/mana_cable_arm_up", null, null, true)));
        multipart.add(part(when_true("down"), apply(MODID + ":block/mana_cable_arm_down", null, null, true)));

        multipart.add(part(when_state(true, false, false, false, false, false), apply(MODID + ":block/mana_cable_cap_north", null, null, true)));
        multipart.add(part(when_state(false, true, false, false, false, false), apply(MODID + ":block/mana_cable_cap_east", null, null, true)));
        multipart.add(part(when_state(false, false, true, false, false, false), apply(MODID + ":block/mana_cable_cap_south", null, null, true)));
        multipart.add(part(when_state(false, false, false, true, false, false), apply(MODID + ":block/mana_cable_cap_west", null, null, true)));
        multipart.add(part(when_state(false, false, false, false, true, false), apply(MODID + ":block/mana_cable_cap_up", null, null, true)));
        multipart.add(part(when_state(false, false, false, false, false, true), apply(MODID + ":block/mana_cable_cap_down", null, null, true)));

        blockstate.add("multipart", multipart);
        futures.add(save_blockstate(cache, "mana_cable", blockstate));

        // models/item/mana_cable.json（使用 core 作为展示）
        JsonObject itemModel = new JsonObject();
        itemModel.addProperty("parent", MODID + ":block/mana_cable_core");
        futures.add(save_item_model(cache, "mana_cable", itemModel));

        // items/mana_cable.json（1.21+ client items）
        JsonObject clientItem = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", MODID + ":item/mana_cable");
        clientItem.add("model", model);
        futures.add(save_item(cache, "mana_cable", clientItem));

        return futures;
    }

    private static JsonObject textures_block(String texturePath) {
        JsonObject textures = new JsonObject();
        textures.addProperty("0", MODID + ":" + texturePath);
        textures.addProperty("particle", MODID + ":" + texturePath);
        return textures;
    }

    private static JsonArray elements(List<JsonObject> elements) {
        JsonArray arr = new JsonArray();
        for (JsonObject element : elements) arr.add(element);
        return arr;
    }

    private static JsonObject element_cube(String name, int fx, int fy, int fz, int tx, int ty, int tz, JsonObject rotation, JsonObject faces) {
        JsonObject element = new JsonObject();
        element.addProperty("name", name);
        element.add("from", vec3(fx, fy, fz));
        element.add("to", vec3(tx, ty, tz));
        element.add("rotation", rotation);
        element.add("faces", faces);
        return element;
    }

    private static JsonObject rotation(int angle, String axis, int ox, int oy, int oz) {
        JsonObject rotation = new JsonObject();
        rotation.addProperty("angle", angle);
        rotation.addProperty("axis", axis);
        rotation.add("origin", vec3(ox, oy, oz));
        return rotation;
    }

    private static JsonObject faces_all(JsonArray uv, String texture) {
        JsonObject faces = new JsonObject();
        faces.add("north", face(uv, texture, null));
        faces.add("east", face(uv, texture, null));
        faces.add("south", face(uv, texture, null));
        faces.add("west", face(uv, texture, null));
        faces.add("up", face(uv, texture, null));
        faces.add("down", face(uv, texture, null));
        return faces;
    }

    private static JsonObject face(JsonArray uv, String texture, Integer rotation) {
        JsonObject face = new JsonObject();
        face.add("uv", uv);
        face.addProperty("texture", texture);
        if (rotation != null) face.addProperty("rotation", rotation);
        return face;
    }

    private static JsonArray uv(int u1, int v1, int u2, int v2) {
        JsonArray arr = new JsonArray();
        arr.add(u1);
        arr.add(v1);
        arr.add(u2);
        arr.add(v2);
        return arr;
    }

    private static JsonArray vec3(int x, int y, int z) {
        JsonArray arr = new JsonArray();
        arr.add(x);
        arr.add(y);
        arr.add(z);
        return arr;
    }

    private static JsonObject when_true(String property) {
        JsonObject when = new JsonObject();
        when.addProperty(property, "true");
        return when;
    }

    private static JsonObject when_state(boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
        JsonObject when = new JsonObject();
        when.addProperty("north", north ? "true" : "false");
        when.addProperty("east", east ? "true" : "false");
        when.addProperty("south", south ? "true" : "false");
        when.addProperty("west", west ? "true" : "false");
        when.addProperty("up", up ? "true" : "false");
        when.addProperty("down", down ? "true" : "false");
        return when;
    }

    private static JsonObject when_or(List<JsonObject> whens) {
        JsonObject when = new JsonObject();
        JsonArray arr = new JsonArray();
        for (JsonObject w : whens) arr.add(w);
        when.add("OR", arr);
        return when;
    }

    private static JsonObject apply(String model, Integer x, Integer y, boolean uvlock) {
        JsonObject apply = new JsonObject();
        apply.addProperty("model", model);
        if (x != null) apply.addProperty("x", x);
        if (y != null) apply.addProperty("y", y);
        if (uvlock) apply.addProperty("uvlock", true);
        return apply;
    }

    private static JsonObject part(JsonObject apply) {
        JsonObject part = new JsonObject();
        part.add("apply", apply);
        return part;
    }

    private static JsonObject part(JsonObject when, JsonObject apply) {
        JsonObject part = new JsonObject();
        part.add("when", when);
        part.add("apply", apply);
        return part;
    }

    @Override
    public String getName() {
        return "Mananet Block Assets";
    }
}
