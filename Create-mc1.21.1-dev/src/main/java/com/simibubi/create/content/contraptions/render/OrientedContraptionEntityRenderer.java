package com.simibubi.create.content.contraptions.render;

import com.simibubi.create.AllTags.AllContraptionTypeTags;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class OrientedContraptionEntityRenderer extends ContraptionEntityRenderer<OrientedContraptionEntity> {
	public OrientedContraptionEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(OrientedContraptionEntity entity, Frustum frustum, double cameraX, double cameraY,
			double cameraZ) {
		if (!super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ))
			return false;
		if (entity.getVehicle() == null && AllContraptionTypeTags.REQUIRES_VEHICLE_FOR_RENDER.matches(entity.getContraption().getType()))
			return false;

		return true;
	}
}
