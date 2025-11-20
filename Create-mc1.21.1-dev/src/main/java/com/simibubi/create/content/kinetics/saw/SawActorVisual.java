package com.simibubi.create.content.kinetics.saw;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public class SawActorVisual extends ActorVisual {
	private final RotatingInstance shaft;

	public SawActorVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
		super(visualizationContext, simulationWorld, movementContext);

		var state = movementContext.state;
		var localPos = movementContext.localPos;
		shaft = SawVisual.shaft(instancerProvider, state);

		var axis = KineticBlockEntityVisual.rotationAxis(state);
		shaft.setRotationAxis(axis)
			.setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, localPos))
			.setPosition(localPos)
			.light(localBlockLight(), 0)
			.setChanged();
	}

	@Override
	protected void _delete() {
		shaft.delete();
	}
}
