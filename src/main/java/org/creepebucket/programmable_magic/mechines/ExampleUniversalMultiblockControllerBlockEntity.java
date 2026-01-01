package org.creepebucket.programmable_magic.mechines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.mechines.api.BaseControllerBlockEntity;
import org.creepebucket.programmable_magic.mechines.api.UniversalMultiblockControllerBlock;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

import java.util.List;
import java.util.Map;

public class ExampleUniversalMultiblockControllerBlockEntity extends BaseControllerBlockEntity {

    public ExampleUniversalMultiblockControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXAMPLE_UNIVERSAL_MULTIBLOCK_CONTROLLER.get(), pos, state, pattern(state), map(state));
    }

    private static List<List<String>> pattern(BlockState state) {
        return ((UniversalMultiblockControllerBlock) state.getBlock()).pattern();
    }

    private static Map<Character, Block> map(BlockState state) {
        return ((UniversalMultiblockControllerBlock) state.getBlock()).map();
    }
}
