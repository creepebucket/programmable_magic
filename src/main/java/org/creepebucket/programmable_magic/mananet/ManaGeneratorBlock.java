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
 * 魔力网络的“发电机”节点方块。
 *
 * <p>通过 {@link #init_node_state(ServerLevel, BlockPos, BlockState, MananetNodeState)} 设定：</p>
 * <ul>
 *     <li>较小的缓存上限（cache）</li>
 *     <li>持续产出（load 为负）</li>
 * </ul>
 */
public class ManaGeneratorBlock extends AbstractNodeBlock {

    public static final String ID = "mana_generator";

    public ManaGeneratorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * 注册方块与对应物品。
     */
    public static DeferredBlock<ManaGeneratorBlock> register(DeferredRegister.Blocks blocks, DeferredRegister.Items items) {
        DeferredBlock<ManaGeneratorBlock> block = blocks.register(ID, registryName -> new ManaGeneratorBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.GOLD)
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
        // 发电机提供少量容量，并按固定速率向网络注入魔力（load 为负数）。
        node_state.cache = new Mana(32.0, 32.0, 32.0, 32.0);
        node_state.load = new Mana(-8.0, -8.0, -8.0, -8.0);
    }
}
