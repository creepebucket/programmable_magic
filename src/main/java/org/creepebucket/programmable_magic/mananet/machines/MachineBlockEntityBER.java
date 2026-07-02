package org.creepebucket.programmable_magic.mananet.machines;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.function.Function;

public class MachineBlockEntityBER<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T, BlockEntityRenderState> {
    public final Function<? super T, AABB> boundingBox;

    public MachineBlockEntityBER(BlockEntityRendererProvider.Context context, MachineGeoModel<T> model, Function<? super T, AABB> boundingBox) {
        super(context, model);
        this.boundingBox = boundingBox;
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return boundingBox.apply(blockEntity);
    }
}
