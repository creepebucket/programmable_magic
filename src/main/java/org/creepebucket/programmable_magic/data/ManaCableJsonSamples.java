package org.creepebucket.programmable_magic.data;

/**
 * 便于拷贝粘贴的 ManaCable 资源 JSON 字符串。
 *
 * - blockstates/mana_cable.json：multipart，中心模型随 center 属性切换，六向连接叠加；
 * - models/block/*.json：中心/中心(有连接)/北部/底部；均使用 cutout 渲染；
 * - models/item/mana_cable.json：手持模型，指向中心模型。
 *
 * 贴图统一使用 programmable_magic:block/mana_cable。
 */
public final class ManaCableJsonSamples {
    private ManaCableJsonSamples() {}

    /** blockstates/mana_cable.json */
    public static final String BLOCKSTATE = """
    {
      "multipart": [
        { "when": { "center": true },  "apply": { "model": "programmable_magic:block/mana_cable_center_conn" } },
        { "when": { "center": false }, "apply": { "model": "programmable_magic:block/mana_cable_center" } },

        { "when": { "north": true }, "apply": { "model": "programmable_magic:block/mana_cable_side" } },
        { "when": { "east":  true }, "apply": { "model": "programmable_magic:block/mana_cable_side", "y": 90 } },
        { "when": { "south": true }, "apply": { "model": "programmable_magic:block/mana_cable_side", "y": 180 } },
        { "when": { "west":  true }, "apply": { "model": "programmable_magic:block/mana_cable_side", "y": 270 } },

        { "when": { "down":  true }, "apply": { "model": "programmable_magic:block/mana_cable_down" } },
        { "when": { "up":    true }, "apply": { "model": "programmable_magic:block/mana_cable_down", "x": 180 } }
      ]
    }
    """;

    /** models/block/mana_cable_center.json */
    public static final String MODEL_CENTER = """
    {
      "format_version": "1.21.6",
      "credit": "Made with Blockbench",
      "render_type": "minecraft:cutout",
      "textures": {
        "0": "programmable_magic:block/mana_cable",
        "particle": "programmable_magic:block/mana_cable"
      },
      "elements": [
        {
          "from": [5, 5, 5],
          "to": [11, 11, 11],
          "rotation": {"angle": 0, "axis": "y", "origin": [7, 7, 9]},
          "faces": {
            "north": {"uv": [5, 0, 11, 6], "texture": "#0"},
            "east":  {"uv": [5, 0, 11, 6], "texture": "#0"},
            "south": {"uv": [5, 0, 11, 6], "texture": "#0"},
            "west":  {"uv": [5, 0, 11, 6], "texture": "#0"},
            "up":    {"uv": [5, 0, 11, 6], "texture": "#0"},
            "down":  {"uv": [5, 0, 11, 6], "texture": "#0"}
          }
        }
      ]
    }
    """;

    /** models/block/mana_cable_center_conn.json */
    public static final String MODEL_CENTER_CONN = """
    {
      "format_version": "1.21.6",
      "credit": "Made with Blockbench",
      "render_type": "minecraft:cutout",
      "textures": {
        "0": "programmable_magic:block/mana_cable",
        "particle": "programmable_magic:block/mana_cable"
      },
      "elements": [
        {
          "from": [5, 5, 5],
          "to": [11, 11, 11],
          "rotation": {"angle": 0, "axis": "y", "origin": [7, 7, 9]},
          "faces": {
            "north": {"uv": [0, 0, 6, 6], "texture": "#0"},
            "east":  {"uv": [0, 0, 6, 6], "texture": "#0"},
            "south": {"uv": [0, 0, 6, 6], "texture": "#0"},
            "west":  {"uv": [0, 0, 6, 6], "texture": "#0"},
            "up":    {"uv": [0, 0, 6, 6], "texture": "#0"},
            "down":  {"uv": [0, 0, 6, 6], "texture": "#0"}
          }
        }
      ]
    }
    """;

    /** models/block/mana_cable_side.json */
    public static final String MODEL_SIDE = """
    {
      "format_version": "1.21.6",
      "credit": "Made with Blockbench",
      "render_type": "minecraft:cutout",
      "textures": {
        "0": "programmable_magic:block/mana_cable",
        "particle": "programmable_magic:block/mana_cable"
      },
      "elements": [
        {
          "from": [5, 5, 0],
          "to": [11, 11, 5],
          "rotation": {"angle": 0, "axis": "y", "origin": [7, 7, 4]},
          "faces": {
            "north": {"uv": [5, 0, 11, 6], "texture": "#0"},
            "east":  {"uv": [0, 0, 5, 6], "texture": "#0"},
            "south": {"uv": [5, 0, 11, 6], "texture": "#0"},
            "west":  {"uv": [0, 0, 5, 6], "texture": "#0"},
            "up":    {"uv": [0, 0, 6, 5], "texture": "#0"},
            "down":  {"uv": [0, 0, 6, 5], "texture": "#0"}
          }
        }
      ]
    }
    """;

    /** models/block/mana_cable_down.json */
    public static final String MODEL_DOWN = """
    {
      "format_version": "1.21.6",
      "credit": "Made with Blockbench",
      "render_type": "minecraft:cutout",
      "textures": {
        "0": "programmable_magic:block/mana_cable",
        "particle": "programmable_magic:block/mana_cable"
      },
      "elements": [
        {
          "from": [5, 0, 5],
          "to": [11, 5, 11],
          "rotation": {"angle": 0, "axis": "y", "origin": [5, 0, 5]},
          "faces": {
            "north": {"uv": [0, 0, 6, 5], "texture": "#0"},
            "east":  {"uv": [0, 0, 6, 5], "texture": "#0"},
            "south": {"uv": [0, 0, 6, 5], "texture": "#0"},
            "west":  {"uv": [0, 0, 6, 5], "texture": "#0"},
            "up":    {"uv": [5, 0, 11, 6], "texture": "#0"},
            "down":  {"uv": [5, 0, 11, 6], "texture": "#0"}
          }
        }
      ]
    }
    """;

    /** models/item/mana_cable.json */
    public static final String MODEL_ITEM = """
    {
      "parent": "programmable_magic:block/mana_cable_center"
    }
    """;

    /** items/mana_cable.json （NeoForge 客户端映射）*/
    public static final String CLIENT_ITEM = """
    {
      "model": { "type": "minecraft:model", "model": "programmable_magic:item/mana_cable" }
    }
    """;
}
