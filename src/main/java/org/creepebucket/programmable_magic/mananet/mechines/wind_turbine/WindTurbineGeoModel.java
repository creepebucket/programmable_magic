package org.creepebucket.programmable_magic.mananet.mechines.wind_turbine;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class WindTurbineGeoModel extends GeoModel<WindTurbineBlockEntity> {

    public final Identifier modelPath = Identifier.fromNamespaceAndPath(MODID, "block/mechines/wind_turbine");
    public final Identifier animationPath = Identifier.fromNamespaceAndPath(MODID, "block/mechines/wind_turbine");
    public final Identifier texturePath = Identifier.fromNamespaceAndPath(MODID, "textures/mechines/wind_turbine.png");

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

