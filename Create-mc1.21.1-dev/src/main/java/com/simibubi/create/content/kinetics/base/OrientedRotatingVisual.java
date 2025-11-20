package com.simibubi.create.content.kinetics.base;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllPartialModels.GantryShaftKey;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock.Part;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlockEntity;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class OrientedRotatingVisual<T extends KineticBlockEntity> extends KineticBlockEntityVisual<T> {
	protected final RotatingInstance rotatingModel;

	/**
	 * @param from  The source model orientation to rotate away from.
	 * @param to    The orientation to rotate to.
	 * @param model The model to spin.
	 */
	public OrientedRotatingVisual(VisualizationContext context, T blockEntity, float partialTick, Direction from, Direction to, Model model) {
		super(context, blockEntity, partialTick);

		rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, model)
			.createInstance()
			.rotateToFace(from, to)
			.setup(blockEntity)
			.setPosition(getVisualPosition());

		rotatingModel.setChanged();
	}

	public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> of(PartialModel partial) {
		return (context, blockEntity, partialTick) -> {
			Direction facing = blockEntity.getBlockState()
				.getValue(BlockStateProperties.FACING);
			return new OrientedRotatingVisual<>(context, blockEntity, partialTick, Direction.SOUTH, facing, Models.partial(partial));
		};
	}

	public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> backHorizontal(PartialModel partial) {
		return (context, blockEntity, partialTick) -> {
			Direction facing = blockEntity.getBlockState()
				.getValue(BlockStateProperties.HORIZONTAL_FACING)
				.getOpposite();
			return new OrientedRotatingVisual<>(context, blockEntity, partialTick, Direction.SOUTH, facing, Models.partial(partial));
		};
	}

	public static BlockEntityVisual<? super GantryShaftBlockEntity> gantryShaft(VisualizationContext visualizationContext, GantryShaftBlockEntity gantryShaftBlockEntity, float partialTick) {
		var blockState = gantryShaftBlockEntity.getBlockState();

		Part part = blockState.getValue(GantryShaftBlock.PART);

		boolean isPowered = blockState.getValue(GantryShaftBlock.POWERED);
		boolean isFlipped = blockState.getValue(GantryShaftBlock.FACING)
			.getAxisDirection() == AxisDirection.NEGATIVE;

		var model = Models.partial(AllPartialModels.GANTRY_SHAFTS.get(new GantryShaftKey(part, isPowered, isFlipped)));

		return new OrientedRotatingVisual<>(visualizationContext, gantryShaftBlockEntity, partialTick, Direction.UP, blockState.getValue(GantryShaftBlock.FACING), model);
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
