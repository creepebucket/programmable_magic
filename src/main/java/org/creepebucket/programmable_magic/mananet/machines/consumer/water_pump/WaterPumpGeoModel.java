package org.creepebucket.programmable_magic.mananet.machines.consumer.water_pump;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WaterPumpGeoModel extends GeoModel<WaterPumpBlockEntity> {

	public final Identifier modelPath = Identifier.fromNamespaceAndPath(MODID, "block/machines/water_pump");
	public final Identifier animationPath = Identifier.fromNamespaceAndPath(MODID, "block/machines/water_pump");
	public final Identifier texturePath = Identifier.fromNamespaceAndPath(MODID, "textures/machines/water_pump.png");

	@Override
	public Identifier getModelResource(GeoRenderState renderState) {
		return modelPath;
	}

	@Override
	public Identifier getAnimationResource(WaterPumpBlockEntity animatable) {
		return animationPath;
	}

	@Override
	public Identifier getTextureResource(GeoRenderState renderState) {
		return texturePath;
	}
}
