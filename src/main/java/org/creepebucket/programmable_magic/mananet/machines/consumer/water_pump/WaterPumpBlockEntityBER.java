package org.creepebucket.programmable_magic.mananet.machines.consumer.water_pump;

import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;

public class WaterPumpBlockEntityBER extends GeoBlockRenderer<WaterPumpBlockEntity, BlockEntityRenderState> {

	public WaterPumpBlockEntityBER(BlockEntityRendererProvider.Context context) {
		super(context, new WaterPumpGeoModel());
	}

	@Override
	public AABB getRenderBoundingBox(WaterPumpBlockEntity blockEntity) {
		var pos = blockEntity.getBlockPos();
		return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
	}
}
