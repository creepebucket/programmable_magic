package org.creepebucket.programmable_magic.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;

public class MachineBlockEntity extends NetNodeBlockEntity {

	public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	public ResourceHandler<ItemResource> getItemInput(int index) {
		var block = (BasicMachine) getBlockState().getBlock();
		var facing = getBlockState().hasProperty(BlockStateProperties.FACING) ? getBlockState().getValue(BlockStateProperties.FACING) : Direction.NORTH;
		int count = 0;
		for (int i = 0; i < block.IO_OFFSETS.size(); i++) {
			if (!(block.IO_TYPES.get(i).get() instanceof IoDummies.ItemInputBlock)) continue;
			if (count++ != index) continue;
			var ioPos = getBlockPos().offset(DummyBlock.transformOffset(facing, block.IO_OFFSETS.get(i).getX(), block.IO_OFFSETS.get(i).getY(), block.IO_OFFSETS.get(i).getZ()));
			var be = (DummyBlockEntities.ItemInput) getLevel().getBlockEntity(ioPos);
			return ((FlowControlHandler<ItemResource>) be.wrapper).handler;
		}
		return null;
	}

	public ResourceHandler<ItemResource> getItemOutput(int index) {
		var block = (BasicMachine) getBlockState().getBlock();
		var facing = getBlockState().hasProperty(BlockStateProperties.FACING) ? getBlockState().getValue(BlockStateProperties.FACING) : Direction.NORTH;
		int count = 0;
		for (int i = 0; i < block.IO_OFFSETS.size(); i++) {
			if (!(block.IO_TYPES.get(i).get() instanceof IoDummies.ItemOutputBlock)) continue;
			if (count++ != index) continue;
			var ioPos = getBlockPos().offset(DummyBlock.transformOffset(facing, block.IO_OFFSETS.get(i).getX(), block.IO_OFFSETS.get(i).getY(), block.IO_OFFSETS.get(i).getZ()));
			var be = (DummyBlockEntities.ItemOutput) getLevel().getBlockEntity(ioPos);
			return ((FlowControlHandler<ItemResource>) be.wrapper).handler;
		}
		return null;
	}

	public ResourceHandler<FluidResource> getFluidInput(int index) {
		var block = (BasicMachine) getBlockState().getBlock();
		var facing = getBlockState().hasProperty(BlockStateProperties.FACING) ? getBlockState().getValue(BlockStateProperties.FACING) : Direction.NORTH;
		int count = 0;
		for (int i = 0; i < block.IO_OFFSETS.size(); i++) {
			if (!(block.IO_TYPES.get(i).get() instanceof IoDummies.FluidInputBlock)) continue;
			if (count++ != index) continue;
			var ioPos = getBlockPos().offset(DummyBlock.transformOffset(facing, block.IO_OFFSETS.get(i).getX(), block.IO_OFFSETS.get(i).getY(), block.IO_OFFSETS.get(i).getZ()));
			var be = (DummyBlockEntities.FluidInput) getLevel().getBlockEntity(ioPos);
			return ((FlowControlHandler<FluidResource>) be.wrapper).handler;
		}
		return null;
	}

	public ResourceHandler<FluidResource> getFluidOutput(int index) {
		var block = (BasicMachine) getBlockState().getBlock();
		var facing = getBlockState().hasProperty(BlockStateProperties.FACING) ? getBlockState().getValue(BlockStateProperties.FACING) : Direction.NORTH;
		int count = 0;
		for (int i = 0; i < block.IO_OFFSETS.size(); i++) {
			if (!(block.IO_TYPES.get(i).get() instanceof IoDummies.FluidOutputBlock)) continue;
			if (count++ != index) continue;
			var ioPos = getBlockPos().offset(DummyBlock.transformOffset(facing, block.IO_OFFSETS.get(i).getX(), block.IO_OFFSETS.get(i).getY(), block.IO_OFFSETS.get(i).getZ()));
			var be = (DummyBlockEntities.FluidOutput) getLevel().getBlockEntity(ioPos);
			return ((FlowControlHandler<FluidResource>) be.wrapper).handler;
		}
		return null;
	}
}