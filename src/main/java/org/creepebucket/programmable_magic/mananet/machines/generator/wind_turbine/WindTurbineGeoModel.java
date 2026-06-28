package org.creepebucket.programmable_magic.mananet.machines.generator.wind_turbine;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WindTurbineGeoModel extends GeoModel<WindTurbineBlockEntity> {

    public final Identifier modelPath = Identifier.fromNamespaceAndPath(MODID, "block/machines/wind_turbine");
    public final Identifier animationPath = Identifier.fromNamespaceAndPath(MODID, "block/machines/wind_turbine");
    public final Identifier texturePath = Identifier.fromNamespaceAndPath(MODID, "textures/machines/wind_turbine.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return modelPath;
    }

    @Override
    public Identifier getAnimationResource(WindTurbineBlockEntity animatable) {
        return animationPath;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texturePath;
    }
}
