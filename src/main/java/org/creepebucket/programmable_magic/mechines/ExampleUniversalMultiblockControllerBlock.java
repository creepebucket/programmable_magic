package org.creepebucket.programmable_magic.mechines;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.mechines.api.BaseControllerBlockEntity;
import org.creepebucket.programmable_magic.mechines.api.UniversalMultiblockControllerBlock;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

import java.util.List;
import java.util.Map;

public class ExampleUniversalMultiblockControllerBlock extends UniversalMultiblockControllerBlock {

    public static final MapCodec<ExampleUniversalMultiblockControllerBlock> CODEC = simpleCodec(ExampleUniversalMultiblockControllerBlock::new);

    public ExampleUniversalMultiblockControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public List<List<String>> pattern() {
        return List.of(List.of("# ", "AA", "AA"),
                       List.of("  ", "A ", "A "));
    }

    @Override
    public Map<Character, Block> map() {
        return Map.of('A', Blocks.IRON_BLOCK, ' ', Blocks.AIR);
    }

    @Override
    protected BaseControllerBlockEntity new_controller_block_entity(BlockPos pos, BlockState state) {
        return new ExampleUniversalMultiblockControllerBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<? extends BaseControllerBlockEntity> controller_block_entity_type() {
        return ModBlockEntities.EXAMPLE_UNIVERSAL_MULTIBLOCK_CONTROLLER.get();
    }
}
