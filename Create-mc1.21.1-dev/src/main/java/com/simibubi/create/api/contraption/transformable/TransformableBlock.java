package com.simibubi.create.api.contraption.transformable;

import com.simibubi.create.content.contraptions.StructureTransform;

import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface TransformableBlock {
	BlockState transform(BlockState state, StructureTransform transform);
}
