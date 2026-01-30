package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.creepebucket.programmable_magic.ModUtils.Mana;
import org.creepebucket.programmable_magic.mananet.api.AbstractNodeBlock;
import org.creepebucket.programmable_magic.mananet.api.MananetNodeState;

/**
 * 魔力网络的“缓冲”节点方块。
 *
 * <p>缓冲节点只提供大量容量（cache），不提供持续产出/消耗（load 保持为 0）。</p>
 */
public class ManaBufferBlock extends AbstractNodeBlock {

    public static final String ID = "mana_buffer";

    public ManaBufferBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 注册方块与对应物品。
     */
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
        // 提供更大的容量上限，用于存储网络中的当前魔力。
        node_state.cache = new Mana(512.0, 512.0, 512.0, 512.0);
    }
}
