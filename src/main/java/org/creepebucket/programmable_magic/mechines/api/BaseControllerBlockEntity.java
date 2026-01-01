package org.creepebucket.programmable_magic.mechines.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.mechines.logic.StructureUtils.matches;

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
    // map value: List<String>，不带'#'表示方块id，带'#'表示方块标签
    public Map<Character, List<String>> map;
    short nextStructureCheck = 5; // 下一次结构完整性检查还剩 (tick)
    boolean formed; // 是否成型

    public BaseControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, List<List<String>> pattern, Map<Character, List<String>> map) {
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
        be.tick_formed(level, pos, state);
    }

    protected void tick_formed(Level level, BlockPos pos, BlockState state) {}

    private void checkStructure() {
        Level level = this.getLevel();
        BlockPos controller_pos = this.getBlockPos();

        BlockState controller_state = level.getBlockState(controller_pos);
        if (controller_state.getBlock() instanceof UniversalMultiblockControllerBlock controller_block) {
            // 通用控制器：pattern/map 由 block 决定，并且要按朝向做旋转/镜像匹配。
            Map<Character, List<String>> map = controller_block.map;
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
}
