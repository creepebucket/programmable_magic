package org.creepebucket.programmable_magic.mechines.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.Map;

public class BaseControllerBlockEntity extends BlockEntity {
    /*
     * pattern: 描述一个多方块结构.
     * 格式为:
     *
     * List.of(
     *     List.of("AAAABBBB", "CCCCDDDD"),
     *     List.of("EEEE#FFF", "GGGGGGGG"),
     * )
     *
     * 约定如下: 称外层列表为i, 内层为j, 则:
     * i[x] 代表z坐标, j[x]表示y坐标, String[x]表示x坐标
     * String内的每个字符即表示一个方块, 且必须有(且仅有)一个"#"表示控制器位置.
     */
    public List<List<String>> pattern;
    /*
     * map: 决定pattern内的方块如何被映射为方块.
     */
    public Map<Character, Block> map;
    short nextStructureCheck = 5; // 下一次结构完整性检查还剩 (tick)
    boolean formed; // 是否成型

    public BaseControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, List<List<String>> pattern, Map<Character, Block> map) {
        super(type, pos, blockState);
        this.pattern = pattern;
        this.map = map;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BaseControllerBlockEntity be) {
        // 检查结构完整性
        if (--be.nextStructureCheck == 0) {
            be.nextStructureCheck = (short) Math.floor(60 + Math.random() * 20); // 在3-4秒随机, 均摊负载
            be.checkStructure();
        }

        if (!be.formed) return;

        System.out.println("我成型了");
        // 主要逻辑
    }

    private void checkStructure() {
        Level level = this.getLevel();
        BlockPos controller_pos = this.getBlockPos();

        BlockState controller_state = level.getBlockState(controller_pos);
        if (controller_state.getBlock() instanceof UniversalMultiblockControllerBlock controller_block) {
            Map<Character, Block> map = controller_block.map();
            List<List<String>> pattern = controller_block.rotated_pattern(controller_state);
            if (matches(level, controller_pos, pattern, map)) {
                this.formed = true;
                return;
            }
            this.formed = matches(level, controller_pos, controller_block.rotated_mirrored_pattern(controller_state), map);
            return;
        }

        this.formed = matches(level, controller_pos, this.pattern, this.map);
    }

    private static boolean matches(Level level, BlockPos controller_pos, List<List<String>> pattern, Map<Character, Block> map) {
        int controller_z = 0;
        int controller_y = 0;
        int controller_x = 0;
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

                    Block expected = map.get(ch);
                    BlockPos target_pos = controller_pos.offset(x - controller_x, y - controller_y, z - controller_z);
                    if (!level.getBlockState(target_pos).is(expected)) formed = false;
                }
            }
        }
        return formed;
    }
}
