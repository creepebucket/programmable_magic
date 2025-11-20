package com.simibubi.create.content.logistics.packager.repackager;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class RepackagerBlock extends PackagerBlock {

	public RepackagerBlock(Properties properties) {
		super(properties);
	}
	
	@Override
	public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.REPACKAGER.get();
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

}
