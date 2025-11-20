package org.creepebucket.programmable_magic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.creepebucket.programmable_magic.blockentity.ManaCableBlockEntity;

/**
 * 在方块上方绘制单一网络 ID（simpleNetId）。
 */
public class ManaCableLabelRenderer {

    public static void render(ManaCableBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        var level = be.getLevel();
        if (level == null || !level.isClientSide) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        String text = "ID=" + Long.toUnsignedString(be.getSimpleNetId());

        Vec3 pos = Vec3.atCenterOf(be.getBlockPos()).add(0, 0.8, 0);
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        var cam = Minecraft.getInstance().getEntityRenderDispatcher().camera;
        poseStack.translate(pos.x - cam.getPosition().x, pos.y - cam.getPosition().y, pos.z - cam.getPosition().z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        font.drawInBatch(text, -font.width(text)/2f, 0, 0xFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
        poseStack.popPose();
    }
}
