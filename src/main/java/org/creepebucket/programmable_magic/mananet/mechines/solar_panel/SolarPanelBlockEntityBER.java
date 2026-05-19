package org.creepebucket.programmable_magic.mananet.mechines.solar_panel;

import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;

public class SolarPanelBlockEntityBER extends GeoBlockRenderer<SolarPanelBlockEntity, BlockEntityRenderState> {

    public SolarPanelBlockEntityBER(BlockEntityRendererProvider.Context context) {
        super(context, new SolarPanelGeoModel());
    }

    @Override
    public AABB getRenderBoundingBox(SolarPanelBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
    }
}
