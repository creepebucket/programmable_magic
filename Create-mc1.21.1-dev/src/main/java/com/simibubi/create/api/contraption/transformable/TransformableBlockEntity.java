package com.simibubi.create.api.contraption.transformable;

import com.simibubi.create.content.contraptions.StructureTransform;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface TransformableBlockEntity {
	void transform(BlockEntity blockEntity, StructureTransform transform);
}
