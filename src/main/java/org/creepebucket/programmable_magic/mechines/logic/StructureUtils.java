package org.creepebucket.programmable_magic.mechines.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public interface StructureUtils {
    static boolean matches(Level level, BlockPos controller_pos, List<List<String>> pattern, Map<Character, List<String>> map) {
        int controller_z = 0;
        int controller_y = 0;
        int controller_x = 0;
        // 寻找控制器锚点 '#': 用它把 pattern 的相对坐标换算成世界坐标。
        for (int z = 0; z < pattern.size(); z++) {
            List<String> layer = pattern.get(z);
            for (int y = 0; y < layer.size(); y++) {
                String row = layer.get(y);
                for (int x = 0; x < row.length(); x++) {
                    if (row.charAt(x) != '#') continue;
                    controller_z = z;
                    controller_y = y;
                    controller_x = x;
                }
            }
        }

        boolean formed = true;
        for (int z = 0; z < pattern.size(); z++) {
            List<String> layer = pattern.get(z);
            for (int y = 0; y < layer.size(); y++) {
                String row = layer.get(y);
                for (int x = 0; x < row.length(); x++) {
                    char ch = row.charAt(x);
                    if (ch == '#') continue;

                    BlockPos target_pos = controller_pos.offset(x - controller_x, y - controller_y, z - controller_z);
                    BlockState target_state = level.getBlockState(target_pos);
                    boolean matched = false;
                    // map.get(ch) 是一组“可接受的方块条件”，任意命中即可。
                    // - "namespace:block_id"：按方块 id 匹配
                    // - "#namespace:tag_id" ：按方块标签匹配
                    for (String v : map.get(ch)) {
                        if (v.startsWith("#")) {
                            if (target_state.is(TagKey.create(Registries.BLOCK, Identifier.parse(v.substring(1))))) {
                                matched = true;
                                break;
                            }
                        } else {
                            if (target_state.is(BuiltInRegistries.BLOCK.getValue(Identifier.parse(v)))) {
                                matched = true;
                                break;
                            }
                        }
                    }
                    if (!matched) formed = false;
                }
            }
        }
        return formed;
    }

    List<List<String>> pattern();

    default List<List<String>> rotated_mirrored_pattern(BlockState state) {
        // 在“旋转后的坐标系”里，再做一次单轴镜像（只翻 x 或 z 的其中一个轴）。
        // 轴的选择与朝向绑定：同一朝向下只有一种镜像形态，便于推导与复用。
        List<List<String>> rotated = rotated_pattern(state);
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> {
                // x 轴镜像：每一行字符串 reverse（只翻转 x 序）。
                int z_len = rotated.size();
                int y_len = rotated.getFirst().size();

                ArrayList<List<String>> out = new ArrayList<>(z_len);
                for (int z = 0; z < z_len; z++) {
                    ArrayList<String> layer = new ArrayList<>(y_len);
                    for (int y = 0; y < y_len; y++)
                        layer.add(new StringBuilder(rotated.get(z).get(y)).reverse().toString());
                    out.add(layer);
                }
                yield out;
            }
            case EAST, WEST -> {
                // z 轴镜像：反转 z 层顺序（只翻转 z 序），每层内容不变。
                ArrayList<List<String>> out = new ArrayList<>(rotated.size());
                for (int z = rotated.size() - 1; z >= 0; z--) out.add(rotated.get(z));
                yield out;
            }
            default -> rotated;
        };
    }

    /*
     * pattern 约定：外层 list 是 z，内层 list 是 y，string 的 char 下标是 x。
     * 旋转/镜像只作用于水平面（x/z），y 不变。
     */
    default List<List<String>> rotated_pattern(BlockState state) {
        // NORTH: 0 次；EAST: 顺时针 90°；SOUTH: 180°；WEST: 270°。
        int steps = switch (state.getValue(FACING)) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };

        List<List<String>> rotated = pattern();
        for (int i = 0; i < steps; i++) {
            // 原坐标：z/y/x。旋转仅发生在 x/z 平面，y 不变。
            // 顺时针 90°：out_x = old_z_len - 1 - old_z；out_z = old_x。
            int z_len = rotated.size();
            int y_len = rotated.getFirst().size();
            int x_len = rotated.getFirst().getFirst().length();

            ArrayList<List<String>> out = new ArrayList<>(x_len);
            for (int out_z = 0; out_z < x_len; out_z++) {
                ArrayList<String> layer = new ArrayList<>(y_len);
                for (int y = 0; y < y_len; y++) {
                    StringBuilder sb = new StringBuilder(z_len);
                    for (int out_x = 0; out_x < z_len; out_x++) {
                        int old_z = z_len - 1 - out_x;
                        int old_x = out_z;
                        sb.append(rotated.get(old_z).get(y).charAt(old_x));
                    }
                    layer.add(sb.toString());
                }
                out.add(layer);
            }
            rotated = out;
        }
        return rotated;
    }
}
