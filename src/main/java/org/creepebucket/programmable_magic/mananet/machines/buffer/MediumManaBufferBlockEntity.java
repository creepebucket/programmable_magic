package org.creepebucket.programmable_magic.mananet.machines.buffer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

public class MediumManaBufferBlockEntity extends ManaBufferBlockEntity {

	public MediumManaBufferBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MEDIUM_MANA_BUFFER_BLOCK_ENTITY.get(), pos, state);
		baseStorage = 5e9;
		baseExpansion = baseStorage * 0.2;
		baseExpansionPower = baseStorage * 0.005;
		maxChargePower = 5e6;
		chargeSlotCount = 3;
		connectHeight = 3;
	}
}
