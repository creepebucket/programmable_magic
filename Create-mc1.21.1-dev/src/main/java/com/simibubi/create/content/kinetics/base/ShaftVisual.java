package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;

public class ShaftVisual<T extends KineticBlockEntity> extends SingleAxisRotatingVisual<T> {
	public ShaftVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFT));
	}
}
