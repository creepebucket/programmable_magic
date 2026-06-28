package org.creepebucket.programmable_magic.mananet.machines.generator.solar_panel;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public class SolarPanelGeoModel extends GeoModel<SolarPanelBlockEntity> {

    public final Identifier modelPath = Identifier.fromNamespaceAndPath(MODID, "block/machines/solar_panel");
    public final Identifier animationPath = Identifier.fromNamespaceAndPath(MODID, "block/machines/solar_panel");
    public final Identifier texturePath = Identifier.fromNamespaceAndPath(MODID, "textures/machines/solar_panel.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return modelPath;
    }

    @Override
    public Identifier getAnimationResource(SolarPanelBlockEntity animatable) {
        return animationPath;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texturePath;
    }
}
