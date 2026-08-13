package org.creepebucket.arcanism.mananet.machines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.creepebucket.arcanism.mananet.NetNodeBlockEntity;
import org.creepebucket.arcanism.utils.RelativeBlockPos;

import java.util.HashMap;
import java.util.Map;

public class MachineBlockEntity extends NetNodeBlockEntity {
	public boolean enabled;
	public Map<RelativeBlockPos, ItemStacksResourceHandler> itemStorage = new HashMap<>();
	public Map<RelativeBlockPos, FluidStacksResourceHandler> fluidStorage = new HashMap<>();

	public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);

		// 根据在方块存储的IO位置信息 创建 ResourceHandler
		BasicMachine block = (BasicMachine) getBlockState().getBlock();
		for (RelativeBlockPos rpos: block.IO_DEFINITION.keySet()) {
			if (block.IO_DEFINITION.get(rpos).startsWith("item")) {
				itemStorage.put(rpos, new ItemStacksResourceHandler(16) {
					@Override
					protected void onContentsChanged(int index, ItemStack previousContents) {
						setChanged();
					}
				});
			} else {
				fluidStorage.put(rpos, new FluidStacksResourceHandler(1, 16000) {
					@Override
					protected void onContentsChanged(int index, FluidStack previousContents) {
						setChanged();
					}
				});
			}
		}

	}

	public ResourceHandler<ItemResource> getItemHandler(RelativeBlockPos pos) {
		// WARNING: 此方法仅作为外部暴露接口, 此方法获取的 ResourceHandler 会限制物品输入/输出, 在任何情况下都应该直接操作Map
		var ioType = ((BasicMachine) getBlockState().getBlock()).IO_DEFINITION.get(pos);
		if (!itemStorage.containsKey(pos)) return null;
		return new FlowControlHandler<>(
				itemStorage.get(pos),
				ioType.equals("item_input"),
				ioType.equals("item_output")
		);
	}

	public ResourceHandler<FluidResource> getFluidHandler(RelativeBlockPos pos) {
		// WARNING: 此方法仅作为外部暴露接口, 此方法获取的 ResourceHandler 会限制物品输入/输出, 在任何情况下都应该直接操作Map
		var ioType = ((BasicMachine) getBlockState().getBlock()).IO_DEFINITION.get(pos);
		if (!fluidStorage.containsKey(pos)) return null;
		return new FlowControlHandler<>(
				fluidStorage.get(pos),
				ioType.equals("fluid_input"),
				ioType.equals("fluid_output")
		);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		enabled = input.getBooleanOr("enabled", false);

		// 反序列化物品/流体
		for (RelativeBlockPos pos: itemStorage.keySet()) {
			var child = input.childOrEmpty(String.valueOf(pos.asLong()));
			itemStorage.get(pos).deserialize(child);
		}
		for (RelativeBlockPos pos: fluidStorage.keySet()) {
			var child = input.childOrEmpty(String.valueOf(pos.asLong()));
			fluidStorage.get(pos).deserialize(child);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		output.putBoolean("enabled", enabled);

		// 序列化物品/流体
		for (RelativeBlockPos pos: itemStorage.keySet()) {
			var child = output.child(String.valueOf(pos.asLong()));
			itemStorage.get(pos).serialize(child);
		}
		for (RelativeBlockPos pos: fluidStorage.keySet()) {
			var child = output.child(String.valueOf(pos.asLong()));
			fluidStorage.get(pos).serialize(child);
		}
	}
}
