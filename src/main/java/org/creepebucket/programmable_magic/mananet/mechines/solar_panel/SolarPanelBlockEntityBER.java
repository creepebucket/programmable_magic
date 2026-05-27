package org.creepebucket.programmable_magic.mananet.mechines.solar_panel;

import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;

public class SolarPanelBlockEntityBER extends GeoBlockRenderer<SolarPanelBlockEntity, BlockEntityRenderState> {

    public SolarPanelBlockEntityBER(BlockEntityRendererProvider.Context context) {
        super(context, new SolarPanelGeoModel());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> render_pass_info, BoneSnapshots snapshots) {
        var time = Minecraft.getInstance().level.getOverworldClockTime() % 24000;
        var rot_x = (time <= 12000 ? 6000 - time : time - 18000) * (float) Math.PI / 12000f;

        var group13 = snapshots.get("group13");
        if (group13.isPresent()) group13.get().setRotX(rot_x);
    }

    @Override
    public AABB getRenderBoundingBox(SolarPanelBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
    }
}
