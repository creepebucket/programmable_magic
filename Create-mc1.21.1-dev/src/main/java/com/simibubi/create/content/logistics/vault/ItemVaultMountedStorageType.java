package com.simibubi.create.content.logistics.vault;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemVaultMountedStorageType extends MountedItemStorageType<ItemVaultMountedStorage> {
	public ItemVaultMountedStorageType() {
		super(ItemVaultMountedStorage.CODEC);
	}

	@Override
	@Nullable
	public ItemVaultMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return be instanceof ItemVaultBlockEntity vault ? ItemVaultMountedStorage.fromVault(vault) : null;
	}
}
