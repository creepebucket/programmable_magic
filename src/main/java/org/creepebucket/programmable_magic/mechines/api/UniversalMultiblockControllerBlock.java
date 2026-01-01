package org.creepebucket.programmable_magic.mechines.api;

import com.mojang.serialization.MapCodec;
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

import org.creepebucket.programmable_magic.mechines.logic.StructureUtils;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class UniversalMultiblockControllerBlock<BE extends BaseControllerBlockEntity> extends HorizontalDirectionalBlock implements EntityBlock, StructureUtils {

    public BlockEntityType.BlockEntitySupplier<BE> block_entity_type;
    public List<List<String>> pattern;
    public Map<Character, List<String>> map;

    /*
     * 该基类强制使用水平四向朝向（HorizontalDirectionalBlock.FACING）。
     * pattern 本体按“面向 NORTH”定义，实际匹配时会按 FACING 做旋转与可选镜像。
     */
    public UniversalMultiblockControllerBlock(Properties blockProperties, List<List<String>> pattern, Map<Character, List<String>> map, BlockEntityType.BlockEntitySupplier<BE> block_entity_type) {
        super(blockProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.pattern = pattern;
        this.map = map;
        this.block_entity_type = block_entity_type;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public final BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return block_entity_type.create(pos, state);
    }

    @Nullable
    @Override
    public final <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> BaseControllerBlockEntity.tick(lvl, pos, st, (BE) be);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    public List<List<String>> pattern() {
        return pattern;
    }
}
