package org.creepebucket.arcanism.mananet.machines.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.arcanism.registries.ModBlockEntities;

public class LargeManaBufferBlockEntity extends ManaBufferBlockEntity {

	public LargeManaBufferBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LARGE_MANA_BUFFER_BLOCK_ENTITY.get(), pos, state);
		baseStorage = 3e10;
		baseExpansion = baseStorage * 0.2;
		baseExpansionPower = baseStorage * 0.005;
		maxChargePower = 5e6;
		chargeSlotCount = 5;
		connectHeight = 4;
	}
}
