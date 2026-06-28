package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.creepebucket.programmable_magic.registries.ModBlockEntities;

public class DummyBlockEntity extends BlockEntity {

	public IoType ioType = IoType.ITEM_INPUT;

	public DummyBlockEntity(BlockPos pos, BlockState blockState) {
		super(ModBlockEntities.DUMMY_BLOCK_ENTITY.get(), pos, blockState);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ioType = IoType.valueOf(input.getStringOr("io_type", IoType.ITEM_INPUT.name()));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("io_type", ioType.name());
	}

	public enum IoType {
		ITEM_INPUT,
		ITEM_OUTPUT,
		FLUID_INPUT,
		FLUID_OUTPUT
	}
}
