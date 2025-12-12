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
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.network.dataPackets.SpellReleasePacket;
import org.creepebucket.programmable_magic.network.dataPackets.GuiDataPacket;
import net.minecraft.util.Mth;
import org.creepebucket.programmable_magic.spells.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.SpellUtils;
import org.creepebucket.programmable_magic.spells.SpellValueType;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

/**
 * 最小界面：仅展示一个按钮，点击后发送法术释放数据包。
 */
public class WandScreen extends AbstractContainerScreen<WandMenu> {

    private int spellIndexOffset = 0;
    int time;
    int spellSlots;
    private String sidebar = "compute";
    private int supplyScrollRow = 0;

    private SidebarToggleWidget sidebarCompute;
    private SidebarToggleWidget sidebarAdjust;
    private SidebarToggleWidget sidebarControl;
    private SidebarToggleWidget sidebarBase;

    public WandScreen(WandMenu menu, Inventory playerInv, Component title, int spellSlots) {
        super(menu, playerInv, title);
        this.spellSlots = spellSlots;
    }

    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        this(menu, playerInv, title, menu.getSpellSlotCapacity());
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

        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        int compactModeYOffset = spellSlotCount <= 16 ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

        // 法术控制
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X - 8 * spellSlotCount - 34, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16, prevTex, prevTex, () -> {
            int step = computeStep();
            updateSpellIndex(-step);
            sendMenuData(WandMenu.KEY_SPELL_OFFSET, this.spellIndexOffset);
        }));
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X - 8 * spellSlotCount - 18, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16, clearTex, clearTex, () -> {
            sendMenuData(WandMenu.KEY_CLEAN, true);
        }));
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X + 8 * spellSlotCount + 16, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16, nextTex, nextTex, () -> {
            int step = computeStep();
            updateSpellIndex(step);
            sendMenuData(WandMenu.KEY_SPELL_OFFSET, this.spellIndexOffset);
        }));
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X + 8 * spellSlotCount, sh + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset, 16, 16, saveTex, saveTex, () -> {
        }));

        // 法术释放
        var releaseTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_release.png");
        this.addRenderableWidget(new ImageButtonWidget(CENTER_X - 112 / 2, sh - 100 - compactModeYOffset, 112, 16, releaseTex, releaseTex, () -> {
            var payload = new SpellReleasePacket(List.of(), 0.0);
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.send(new ServerboundCustomPayloadPacket(payload));
            }
        }));

        // 上报当前容器在屏幕中的上下左右坐标（用于响应式布局）
        report_screen_bounds();

        // 侧栏（互斥）
        var computeTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute.png");
        var computePressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_compute_pressed.png");
        var adjustTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust.png");
        var adjustPressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_adjust_pressed.png");
        var controlTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control.png");
        var controlPressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_control_pressed.png");
        var baseTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base.png");
        var basePressedTex = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_sidebar_base_pressed.png");

        sidebarCompute = new SidebarToggleWidget(0, 8, 16, 48, computeTex, computePressedTex, () -> setSidebar("compute"));
        sidebarAdjust = new SidebarToggleWidget(0, 48 + 8, 16, 48, adjustTex, adjustPressedTex, () -> setSidebar("adjust"));
        sidebarControl = new SidebarToggleWidget(0, 2 * 48 + 8, 16, 48, controlTex, controlPressedTex, () -> setSidebar("control"));
        sidebarBase = new SidebarToggleWidget(0, 3 * 48 + 8, 16, 48, baseTex, basePressedTex, () -> setSidebar("base"));

        this.addRenderableWidget(sidebarCompute);
        this.addRenderableWidget(sidebarAdjust);
        this.addRenderableWidget(sidebarControl);
        this.addRenderableWidget(sidebarBase);

        // 初始化互斥状态并同步给 menu
        setSidebar(this.sidebar);
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

    private void setSidebar(String v) {
        this.sidebar = v;
        if (this.sidebarCompute != null) this.sidebarCompute.setSelected("compute".equals(v));
        if (this.sidebarAdjust != null) this.sidebarAdjust.setSelected("adjust".equals(v));
        if (this.sidebarControl != null) this.sidebarControl.setSelected("control".equals(v));
        if (this.sidebarBase != null) this.sidebarBase.setSelected("base".equals(v));

        sendMenuData(WandMenu.KEY_SPELL_SIDEBAR, v);
        this.supplyScrollRow = 0;
        sendMenuData(WandMenu.KEY_SUPPLY_SCROLL, this.supplyScrollRow);
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

        // 法术侧栏
        graphics.fill(17 - this.leftPos, - this.topPos, 17 - this.leftPos + 1, sh - 16, 0xFFFFFFFF);
        graphics.fill(17 + 82 - this.leftPos, - this.topPos, 17 + 82 - this.leftPos + 1, sh - 16, 0xFFFFFFFF);

        // 法术槽
        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        boolean compactMode = spellSlotCount <= 16;
        int compactModeYOffset = compactMode ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

        for (int i = 0; i < spellSlotCount; i++) drawSpellSlot(graphics, this.spellIndexOffset + i, CENTER_X - spellSlotCount * 8 + i * 16, BOTTOM_Y + MathUtils.SPELL_SLOT_OFFSET - compactModeYOffset);

        // 物品栏
        for (int i = 0; i < 36; i++) {
            int x = CENTER_X - ( compactMode? 81 - (i % 9) * 18 : 161 - (i % 18) * 18 );
            int y = BOTTOM_Y + ( compactMode? MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 9) * 18 - 18 : MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 18) * 18 );

            if (i < 9 && !compactMode) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_hotbar.png"), x, y, 0, 0, 16, 16, 16, 16);
            } else if (i >= 9){
                graphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_inventory.png"), x, y - compactModeYOffset, 0, 0, 16, 16, 16, 16);
            }
        }

        // 左侧法术选择菜单
        Map<Component, List<ItemStack>> spells = SpellUtils.getSpellsGroupedBySubCategory(SpellUtils.stringSpellTypeMap.get(sidebar));
        // 遍历每个键值对
        int startX = 19 - this.leftPos;
        int startY = 10 - this.topPos - this.supplyScrollRow * 16;

        int x = startX;
        int y = startY;

        for (Map.Entry<Component, List<ItemStack>> entry : spells.entrySet()) {
            Component key = entry.getKey();

            graphics.drawString(font, key.getString(), x, y, 0xFFBF360C);

            for (int i = 0; i < entry.getValue().size(); i++) graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_sidebar_slot.png"),
                    x + (i % 5) * 16 - 1, y + Math.floorDiv(i, 5) * 16 + 10, 0, 0, 16, 16, 16, 16);

            y += Math.floorDiv(entry.getValue().size() - 1, 5) * 16 + 32;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = this.width / 2 - 1 - this.leftPos;
        int BOTTOM_Y = sh - this.topPos;

        // 左侧法术列表区域（与 renderLabels 中的侧栏外框一致）
        int sideX0 = 17 - this.leftPos;
        int sideX1 = sideX0 + 82;
        int sideY0 = 8 - this.topPos;
        int sideY1 = sh - 8 - this.topPos;

        int localX = (int) mouseX - this.leftPos;
        int localY = (int) mouseY - this.topPos;

        if (localX >= sideX0 && localX <= sideX1 && localY >= sideY0 && localY <= sideY1) {
            // 基于分组的内容高度与可见高度计算最大滚动行
            Map<Component, List<ItemStack>> grouped = SpellUtils.getSpellsGroupedBySubCategory(SpellUtils.stringSpellTypeMap.get(this.sidebar));
            int contentHeightPx = 0;
            for (var e : grouped.entrySet()) {
                int n = e.getValue().size();
                int rows = Math.max(1, (int) Math.ceil(n / 5.0));
                contentHeightPx += (rows - 1) * 16 + 32; // 行间距 + 标题与组间距
            }
            int visibleHeightPx = (sh - 16) - 20; // 侧栏总高减去上下内边距
            int maxRow = Math.max(0, (int) Math.ceil(Math.max(0, contentHeightPx - visibleHeightPx) / 16.0));
            int d = scrollY > 0 ? -1 : (scrollY < 0 ? 1 : 0);
            this.supplyScrollRow = Mth.clamp(this.supplyScrollRow + d, 0, maxRow);
            sendMenuData(WandMenu.KEY_SUPPLY_SCROLL, this.supplyScrollRow);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

        if (index < spellSlots) {

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_slot.png"),
                    x, y, 0, 0, 16, 16, 16, 16);


            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    numbers.get(Math.floorDiv(index, 100) % 10),
                    x + 1, y - 3, 0, 0, 3, 5, 3, 5);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    numbers.get(Math.floorDiv(index, 10) % 10),
                    x + 5, y - 3, 0, 0, 3, 5, 3, 5);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    numbers.get(Math.floorDiv(index, 1) % 10),
                    x + 9, y - 3, 0, 0, 3, 5, 3, 5);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_out_of_bound.png"),
                    x, y, 0, 0, 16, 16, 16, 16);
        }
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

    private static class SidebarToggleWidget extends AbstractWidget {
        private final ResourceLocation normal;
        private final ResourceLocation selectedTex;
        private final Runnable onPress;
        private boolean selected;

        public SidebarToggleWidget(int x, int y, int w, int h,
                                   ResourceLocation normal, ResourceLocation selectedTex,
                                   Runnable onPress) {
            super(x, y, w, h, Component.empty());
            this.normal = normal;
            this.selectedTex = selectedTex;
            this.onPress = onPress;
        }

        public void setSelected(boolean v) { this.selected = v; }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var tex = this.selected ? this.selectedTex : this.normal;
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
