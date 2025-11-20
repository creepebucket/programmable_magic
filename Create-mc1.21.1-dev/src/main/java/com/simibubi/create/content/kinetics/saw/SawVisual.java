package com.simibubi.create.content.kinetics.saw;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawVisual extends KineticBlockEntityVisual<SawBlockEntity> {

	protected final RotatingInstance rotatingModel;

	public SawVisual(VisualizationContext context, SawBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		rotatingModel = shaft(instancerProvider(), blockState)
			.setup(blockEntity)
			.setPosition(getVisualPosition());
		rotatingModel.setChanged();
	}

	public static RotatingInstance shaft(InstancerProvider instancerProvider, BlockState state) {
		var facing = state.getValue(BlockStateProperties.FACING);
		var axis = facing
			.getAxis();
		// We could change this to return either an Oriented- or SingleAxisRotatingVisual
		if (axis.isHorizontal()) {
			Direction align = facing.getOpposite();
			return instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
				.createInstance()
				.rotateTo(0, 0, 1, align.getStepX(), align.getStepY(), align.getStepZ());
		} else {
			return instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
				.createInstance()
				.rotateToFace(state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE) ? Axis.X : Axis.Z);
		}
	}

	@Override
	public void update(float pt) {
		rotatingModel.setup(blockEntity)
			.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(rotatingModel);
	}

	@Override
	protected void _delete() {
		rotatingModel.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(rotatingModel);
	}
}
