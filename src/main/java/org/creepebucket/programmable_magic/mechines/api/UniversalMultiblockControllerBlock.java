package org.creepebucket.programmable_magic.mechines.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public abstract class UniversalMultiblockControllerBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public UniversalMultiblockControllerBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public abstract List<List<String>> pattern();
    public abstract Map<Character, Block> map();

    public final List<List<String>> rotated_pattern(BlockState state) {
        int steps = switch (state.getValue(FACING)) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };

        List<List<String>> rotated = pattern();
        for (int i = 0; i < steps; i++) rotated = rotate_pattern_clockwise(rotated);
        return rotated;
    }

    public final List<List<String>> rotated_mirrored_pattern(BlockState state) {
        List<List<String>> rotated = rotated_pattern(state);
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> mirror_z(rotated);
            case EAST, WEST -> mirror_x(rotated);
            default -> rotated;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static List<List<String>> rotate_pattern_clockwise(List<List<String>> pattern) {
        int x_len = pattern.size();
        int y_len = pattern.getFirst().size();
        int z_len = pattern.getFirst().getFirst().length();

        java.util.ArrayList<List<String>> out = new java.util.ArrayList<>(z_len);
        for (int new_x = 0; new_x < z_len; new_x++) {
            java.util.ArrayList<String> layer = new java.util.ArrayList<>(y_len);
            for (int y = 0; y < y_len; y++) {
                StringBuilder sb = new StringBuilder(x_len);
                for (int new_z = 0; new_z < x_len; new_z++) {
                    int old_x = new_z;
                    int old_z = z_len - 1 - new_x;
                    sb.append(pattern.get(old_x).get(y).charAt(old_z));
                }
                layer.add(sb.toString());
            }
            out.add(layer);
        }
        return out;
    }

    private static List<List<String>> mirror_x(List<List<String>> pattern) {
        java.util.ArrayList<List<String>> out = new java.util.ArrayList<>(pattern.size());
        for (int x = pattern.size() - 1; x >= 0; x--) out.add(pattern.get(x));
        return out;
    }

    private static List<List<String>> mirror_z(List<List<String>> pattern) {
        int x_len = pattern.size();
        int y_len = pattern.getFirst().size();

        java.util.ArrayList<List<String>> out = new java.util.ArrayList<>(x_len);
        for (int x = 0; x < x_len; x++) {
            java.util.ArrayList<String> layer = new java.util.ArrayList<>(y_len);
            for (int y = 0; y < y_len; y++) layer.add(new StringBuilder(pattern.get(x).get(y)).reverse().toString());
            out.add(layer);
        }
        return out;
    }

    protected abstract BaseControllerBlockEntity new_controller_block_entity(BlockPos pos, BlockState state);
    protected abstract BlockEntityType<? extends BaseControllerBlockEntity> controller_block_entity_type();

    @Nullable
    @Override
    public final BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new_controller_block_entity(pos, state);
    }

    @Nullable
    @Override
    public final <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == controller_block_entity_type()
                ? (lvl, pos, st, be) -> BaseControllerBlockEntity.tick(lvl, pos, st, (BaseControllerBlockEntity) be)
                : null;
    }
}
