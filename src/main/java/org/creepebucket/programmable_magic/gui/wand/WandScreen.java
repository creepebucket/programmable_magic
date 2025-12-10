package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.creepebucket.programmable_magic.network.dataPackets.SpellReleasePacket;
import org.creepebucket.programmable_magic.network.dataPackets.GuiDataPacket;
import net.minecraft.util.Mth;

import java.util.List;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 最小界面：仅展示一个按钮，点击后发送法术释放数据包。
 */
public class WandScreen extends AbstractContainerScreen<WandMenu> {

    private int spellIndexOffset = 0;
    int time;

    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();

        time = 0;

        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = sw / 2;

        var prevTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_prev.png");
        var nextTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_next.png");
        var clearTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_clear.png");
        var saveTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_button_save.png");

        // 法术控制
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X - 16 * 15, sh + MathUtils.SPELL_SLOT_OFFSET, 16, 16, prevTex, prevTex, () -> {
            int step = computeStep();
            updateSpellIndex(-step);
            sendMenuData(WandMenu.KEY_SPELL_OFFSET, this.spellIndexOffset);
        }));
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X - 16 * 14, sh + MathUtils.SPELL_SLOT_OFFSET, 16, 16, clearTex, clearTex, () -> {
        }));
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X + 16 * 14, sh + MathUtils.SPELL_SLOT_OFFSET, 16, 16, nextTex, nextTex, () -> {
            int step = computeStep();
            updateSpellIndex(step);
            sendMenuData(WandMenu.KEY_SPELL_OFFSET, this.spellIndexOffset);
        }));
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X + 16 * 13, sh + MathUtils.SPELL_SLOT_OFFSET, 16, 16, saveTex, saveTex, () -> {
        }));

        // 法术释放
        var releaseTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_release.png");
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X - 112 / 2, sh - 100, 112, 16, releaseTex, releaseTex, () -> {
            var payload = new SpellReleasePacket(List.of(), 0.0);
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.send(new ServerboundCustomPayloadPacket(payload));
            }
        }));

        // 上报当前容器在屏幕中的上下左右坐标（用于响应式布局）
        report_screen_bounds();
    }

    private int computeStep() {
        if (Screen.hasControlDown()) return 100;
        if (Screen.hasAltDown()) return 25;
        if (Screen.hasShiftDown()) return 5;
        return 1;
    }

    private void updateSpellIndex(int delta) {
        int prev = this.spellIndexOffset;
        int next = Mth.clamp(prev + delta, 0, 999);
        this.spellIndexOffset = next;
        sendMenuData(WandMenu.KEY_SPELL_OFFSET, this.spellIndexOffset);
    }

    private void sendMenuData(String key, Object value) {
        // 本地先写入客户端 Menu，确保 Screen 侧立即可见
        this.menu.setClientData(key, value);

        // 同步给服务端 Menu（如服务端也需要使用这些数据）
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            var payload = new GuiDataPacket(key, value);
            connection.send(new ServerboundCustomPayloadPacket(payload));
        }
    }

    private void report_screen_bounds() {
        // 上报容器原点（Screen 的 leftPos/topPos），供 Menu 进行屏幕→容器坐标转换
        sendMenuData(WandMenu.KEY_GUI_LEFT, this.leftPos);
        sendMenuData(WandMenu.KEY_GUI_TOP, this.topPos);
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        report_screen_bounds();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.time++;

        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 背景层此处不绘制，避免与容器背景职责混淆
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = this.width / 2 - 1 - this.leftPos;
        int BOTTOM_Y = sh - this.topPos;

        // 法术槽
        for (int i = 0; i < 25; i++) drawSpellSlot(graphics, this.spellIndexOffset + i, CENTER_X - 25 * 16 + 25 * 8 + i * 16, BOTTOM_Y + MathUtils.SPELL_SLOT_OFFSET);

        // 物品栏
        for (int i = 0; i < 36; i++) {
            int x = CENTER_X - 161 + (i % 18) * 18;
            int y = BOTTOM_Y + MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 18) * 18;

            if (i < 9) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_hotbar.png"), x, y, 0, 0, 16, 16, 16, 16);
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_inventory.png"), x, y, 0, 0, 16, 16, 16, 16);
            }
        }

    }

    public void drawSpellSlot(GuiGraphics guiGraphics, int index, int x, int y) {
        List<ResourceLocation> numbers = List.of(
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_1.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_2.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_3.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_4.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_5.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_6.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_7.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_8.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_font_9.png"));

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_slot.png"),
                x, y, 0, 0, 16, 16, 16, 16);


        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                numbers.get(Math.floorDiv(index, 100) % 10),
                x+1, y-3, 0, 0, 3, 5, 3, 5);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                numbers.get(Math.floorDiv(index, 10) % 10),
                x+5, y-3, 0, 0, 3, 5, 3, 5);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                numbers.get(Math.floorDiv(index, 1) % 10),
                x+9, y-3, 0, 0, 3, 5, 3, 5);
    }

    private static class ImageButtonWidget extends AbstractWidget {
        private final ResourceLocation normal;
        private final ResourceLocation pressed;
        private final Runnable onPress;

        public ImageButtonWidget(int x, int y, int w, int h,
                                 ResourceLocation normal, ResourceLocation pressed,
                                 Runnable onPress) {
            super(x, y, w, h, Component.empty());
            this.normal = normal;
            this.pressed = pressed;
            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var tex = this.isHoveredOrFocused() ? this.pressed : this.normal;
            graphics.blit(RenderPipelines.GUI_TEXTURED, tex,
                    this.getX(), this.getY(),
                    0, 0,
                    this.getWidth(), this.getHeight(),
                    this.getWidth(), this.getHeight());
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (this.onPress != null) this.onPress.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            this.defaultButtonNarrationText(narration);
        }
    }
}
