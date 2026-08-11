package org.creepebucket.programmable_magic.mananet.machines.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

public class SmallManaBufferBlockEntity extends ManaBufferBlockEntity {

	public SmallManaBufferBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMALL_MANA_BUFFER_BLOCK_ENTITY.get(), pos, state);
		baseStorage = 1e9;
		baseExpansion = baseStorage * 0.2;
		baseExpansionPower = baseStorage * 0.005;
		maxChargePower = 5e6;
		chargeSlotCount = 1;
		connectHeight = 1;
	}
}
