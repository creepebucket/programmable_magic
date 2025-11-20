package com.simibubi.create.content.logistics.funnel;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.FlapStuffs;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;

public class FunnelVisual extends AbstractBlockEntityVisual<FunnelBlockEntity> implements SimpleDynamicVisual {

	private final FlapStuffs.Visual flaps;

    public FunnelVisual(VisualizationContext context, FunnelBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

		if (!blockEntity.hasFlap()) {
			flaps = null;
			return;
		}

		var funnelFacing = FunnelBlock.getFunnelFacing(blockState);
		PartialModel flapPartial = (blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP
			: AllPartialModels.BELT_FUNNEL_FLAP);

		var commonTransform = FlapStuffs.commonTransform(getVisualPosition(), funnelFacing, -blockEntity.getFlapOffset());
		flaps = new FlapStuffs.Visual(instancerProvider(), commonTransform, FlapStuffs.FUNNEL_PIVOT, Models.partial(flapPartial));

		flaps.update(blockEntity.flap.getValue(partialTick));
	}

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        if (flaps == null) return;

        flaps.update(blockEntity.flap.getValue(ctx.partialTick()));
    }

    @Override
    public void updateLight(float partialTick) {
        if (flaps != null)
            flaps.updateLight(computePackedLight());
    }

    @Override
    protected void _delete() {
		if (flaps == null) return;

		flaps.delete();
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		if (flaps == null) return;

		flaps.collectCrumblingInstances(consumer);
	}

}
