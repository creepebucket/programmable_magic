package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

public class EnergyHatch extends AbstractNodeBlock {

    public static final String ID = "energy_hatch";
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public int tier;

    protected EnergyHatch(Properties properties, int tier) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.tier = tier;
    }

    public static DeferredBlock<EnergyHatch> register(DeferredRegister.Blocks blocks, DeferredRegister.Items items, int tier) {
        DeferredBlock<EnergyHatch> block = blocks.register(ID + "_t" + tier, registryName -> new EnergyHatch(
                BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, registryName)), tier
        ));
        items.registerSimpleBlockItem(ID + "_t" + tier, block::get);
        return block;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void init_node_state(ServerLevel level, BlockPos pos, BlockState state, MananetNodeState node_state) {
        node_state.connectivityMask = 1 << state.getValue(FACING).ordinal();
    }

    @Override
    protected String getNodeRegistryIdInternal() {
        return ID + "_t" + tier;
    }
}
