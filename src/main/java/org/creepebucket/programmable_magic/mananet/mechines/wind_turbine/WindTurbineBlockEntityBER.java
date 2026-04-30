package org.creepebucket.programmable_magic.mananet.mechines.wind_turbine;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class WindTurbineBlockEntityBER<R extends BlockEntityRenderState & GeoRenderState> extends GeoBlockRenderer<WindTurbineBlockEntity, R> {

    public WindTurbineBlockEntityBER(BlockEntityType<WindTurbineBlockEntity> blockEntityType) {
        super(new WindTurbineGeoModel());
    }

    @Override
    public AABB getRenderBoundingBox(WindTurbineBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 2, pos.getY() - 1, pos.getZ() - 2, pos.getX() + 3, pos.getY() + 7, pos.getZ() + 3);
    }
}
