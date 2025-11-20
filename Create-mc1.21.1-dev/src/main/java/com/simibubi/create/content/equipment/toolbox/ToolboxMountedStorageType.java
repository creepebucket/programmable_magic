package com.simibubi.create.content.equipment.toolbox;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ToolboxMountedStorageType extends MountedItemStorageType<ToolboxMountedStorage> {
	public ToolboxMountedStorageType() {
		super(ToolboxMountedStorage.CODEC);
	}

	@Override
	@Nullable
	public ToolboxMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return be instanceof ToolboxBlockEntity toolbox ? ToolboxMountedStorage.fromToolbox(toolbox) : null;
	}
}
