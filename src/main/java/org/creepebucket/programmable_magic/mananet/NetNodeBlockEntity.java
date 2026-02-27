package org.creepebucket.programmable_magic.mananet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.registries.ModAttachments;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

import java.util.HashMap;

public class NetNodeBlockEntity extends BlockEntity {
    public NetNodeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.NET_NODE_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!hasData(ModAttachments.CONNECTIONS)) setData(ModAttachments.CONNECTIONS, new HashMap<>());
    }
}
