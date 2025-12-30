package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public class ManaCableBlock extends AbstractNodeBlock {

    public static final String ID = "mana_cable";

    public ManaCableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static DeferredBlock<ManaCableBlock> register(DeferredRegister.Blocks blocks, DeferredRegister.Items items) {
        DeferredBlock<ManaCableBlock> block = blocks.register(ID, registryName -> new ManaCableBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(1.0f)
                        .setId(ResourceKey.create(Registries.BLOCK, registryName))
        ));
        items.registerSimpleBlockItem(ID, block::get);
        return block;
    }

    @Override
    protected String getNodeRegistryIdInternal() {
        return ID;
    }

    @Override
    public void init_node_state(ServerLevel level, BlockPos pos, BlockState state, MananetNodeState node_state) {
        super.init_node_state(level, pos, state, node_state);
    }
}
