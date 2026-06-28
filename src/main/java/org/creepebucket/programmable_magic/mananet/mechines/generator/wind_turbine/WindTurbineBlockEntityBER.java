package org.creepebucket.programmable_magic.mananet.mechines.generator.wind_turbine;

import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;

public class WindTurbineBlockEntityBER extends GeoBlockRenderer<WindTurbineBlockEntity, BlockEntityRenderState> {

    public WindTurbineBlockEntityBER(BlockEntityRendererProvider.Context context) {
        super(context, new WindTurbineGeoModel());
    }

    @Override
    public AABB getRenderBoundingBox(WindTurbineBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 2, pos.getY() - 1, pos.getZ() - 2, pos.getX() + 3, pos.getY() + 7, pos.getZ() + 3);
    }
}
