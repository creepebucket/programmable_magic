package org.creepebucket.arcanism.mananet.machines;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class MachineGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    public final Identifier modelPath;
    public final Identifier animationPath;
    public final Identifier texturePath;

    public MachineGeoModel(Identifier modelPath, Identifier animationPath, Identifier texturePath) {
        this.modelPath = modelPath;
        this.animationPath = animationPath;
        this.texturePath = texturePath;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return modelPath;
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return animationPath;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texturePath;
    }
}
