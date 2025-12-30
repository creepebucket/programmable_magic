package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

public class ManaBufferBlock extends AbstractNodeBlock {

    public static final String ID = "mana_buffer";

    public ManaBufferBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static DeferredBlock<ManaBufferBlock> register(DeferredRegister.Blocks blocks, DeferredRegister.Items items) {
        DeferredBlock<ManaBufferBlock> block = blocks.register(ID, registryName -> new ManaBufferBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_ORANGE)
                        .strength(2.0f)
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
        node_state.cache = new Mana(512.0, 512.0, 512.0, 512.0);
    }
}
