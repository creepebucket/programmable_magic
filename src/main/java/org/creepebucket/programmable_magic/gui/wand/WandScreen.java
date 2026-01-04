package org.creepebucket.programmable_magic.gui.wand;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.SlotManipulationScreen;
import org.creepebucket.programmable_magic.network.dataPackets.SpellReleasePacket;
import org.creepebucket.programmable_magic.network.dataPackets.SimpleKvPacket;
import net.minecraft.util.Mth;
import org.creepebucket.programmable_magic.spells.SpellUtils;
import org.creepebucket.programmable_magic.items.Wand;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.registries.WandPluginRegistry;

import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;
import org.lwjgl.glfw.GLFW;

/**
 * 魔杖界面：
 * - 提供法术栏视窗、物品栏绘制、左侧法术供应选择与滚动、卷轴制作位、充能/释放按钮。
 * - 将界面关键数据（坐标、偏移、侧栏、滚动等）上报给对应的 Menu 进行布局与存储。
 */
public class WandScreen extends SlotManipulationScreen<WandMenu> {

    public int spellIndexOffset = 0;
    public int chargeTicks;
    int spellSlots;
    public String sidebar = "compute";
    public int supplyScrollRow = 0;

    public boolean isCharging = false;

    public SidebarToggleWidget sidebarCompute;
    public SidebarToggleWidget sidebarAdjust;
    public SidebarToggleWidget sidebarControl;
    public SidebarToggleWidget sidebarBase;
    public double chargeRate;

    /**
     * 全参构造：用于显式传入法术槽容量与充能功率。
     */
    public WandScreen(WandMenu menu, Inventory playerInv, Component title, int spellSlots, double chargeRate) {
        super(menu, playerInv, title);
        this.spellSlots = spellSlots;
        this.chargeRate = chargeRate;
    }

    /**
     * 便捷构造：从 Menu 读取容量与功率。
     */
    public WandScreen(WandMenu menu, Inventory playerInv, Component title) {
        this(menu, playerInv, title, menu.getSpellSlotCapacity(), menu.getChargeRate());
    }

    @Override
    /**
     * 初始化界面控件并同步屏幕边界数据到 Menu。
     */
    protected void init() {
        super.init();

        chargeTicks = 0;

        // 上报当前容器在屏幕中的上下左右坐标（用于响应式布局）
        report_screen_bounds();

        // 初始化互斥状态并同步给 menu
        setSidebar(this.sidebar);

        // 调用已安装插件的屏幕启动逻辑（统一入口）
        applyPlugins((plugin, x, y, g) -> plugin.screenStartupLogic(x, y, this), null);
    }

    /**
     * 计算法术视窗步进（支持修饰键）。
     */
    public int computeStep() {
        var window = Minecraft.getInstance().getWindow();
        boolean ctrl = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        if (ctrl) return 100;
        boolean alt = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
        if (alt) return 25;
        boolean shift = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (shift) return 5;
        return 1;
    }

    private int getVisibleSpellSlots() {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        return Math.max(1, Math.floorDiv(sw - 200, 16) - 4);
    }

    /**
     * 更新法术偏移并同步给 Menu。
     */
    public void updateSpellIndex(int delta) {
        int prev = this.spellIndexOffset;
        int cap = this.menu.getSpellSlotCapacity();
        int visible = getVisibleSpellSlots();
        int maxOffset = Math.max(0, cap - visible);
        int next = Mth.clamp(prev + delta, 0, maxOffset);
        if (next == prev) return;
        this.spellIndexOffset = next;
        sendMenuData(WandMenu.KEY_SPELL_OFFSET, this.spellIndexOffset);
    }

    /**
     * 向本地与服务端 Menu 发送界面数据。
     */
    public void sendMenuData(String key, Object value) {
        // 本地先写入客户端 Menu，确保 Screen 侧立即可见
        this.menu.setClientData(key, value);

        // 同步给服务端 Menu（如服务端也需要使用这些数据）
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            var payload = new SimpleKvPacket(key, value);
            connection.send(new ServerboundCustomPayloadPacket(payload));
        }
    }

    /**
     * 上报当前容器在屏幕中的左上角坐标。
     */
    private void report_screen_bounds() {
        // 上报容器原点（Screen 的 leftPos/topPos），供 Menu 进行屏幕→容器坐标转换
        sendMenuData(WandMenu.KEY_GUI_LEFT, this.leftPos);
        sendMenuData(WandMenu.KEY_GUI_TOP, this.topPos);
    }

    /**
     * 设置左侧法术类别（互斥切换），并重置滚动行。
     */
    public void setSidebar(String v) {
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
    /**
     * 窗口尺寸变化时重新上报容器坐标。
     */
    public void resize(int width, int height) {
        super.resize(width, height);
        report_screen_bounds();
    }

    @Override
    /**
     * 常规渲染：交给父类并绘制提示。
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    /**
     * 每 tick 自增充能计数（当按下发射按钮后）。
     */
    protected void containerTick() {
        super.containerTick();
        if (this.isCharging) this.chargeTicks++;
        applyPlugins((plugin, x, y, g) -> plugin.screenTick(x, y, this), null);
    }

    @Override
    /**
     * 背景层不绘制（由各自纹理负责）。
     */
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    /**
     * 容器背景不绘制，避免与界面职责混淆。
     */
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 背景层此处不绘制，避免与容器背景职责混淆
    }

    @Override
    /**
     * 绘制各元素：侧栏边框、法术槽、物品栏、侧栏分组、充能读数、卷轴制作。
     */
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {

        // 插件自绘制逻辑
        applyPlugins((plugin, x, y, g) -> plugin.screenRenderLogic(graphics, x, y, this), graphics);

        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int sh = win.getGuiScaledHeight();

        int CENTER_X = this.width / 2 - 1 - this.leftPos;
        int BOTTOM_Y = sh - this.topPos;

        int spellSlotCount = Math.floorDiv(sw - 200, 16) - 4;
        boolean compactMode = spellSlotCount <= 16;
        int compactModeYOffset = compactMode ? 18 : 0; // 当物品栏与法术侧栏重叠时调整位置

        // 物品栏
        for (int i = 0; i < 36; i++) {
            int x = CENTER_X - ( compactMode? 81 - (i % 9) * 18 : 161 - (i % 18) * 18 );
            int y = BOTTOM_Y + ( compactMode? MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 9) * 18 - 18 : MathUtils.INVENTORY_OFFSET + Math.floorDiv(i, 18) * 18 );

            if (i < 9 && !compactMode) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_hotbar.png"), x, y, 0, 0, 16, 16, 16, 16);
            } else if (i >= 9){
                graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_inventory.png"), x, y - compactModeYOffset, 0, 0, 16, 16, 16, 16);
            }
        }

        // 法术卷轴制作菜单

        // 插件槽可视化（使用统一槽纹理）
        int pluginCount = this.menu.getPluginSlotCapacity();
        int pluginX = sw - 24 - this.leftPos;
        int pluginStartY = 10 - this.topPos;
        for (int i = 0; i < pluginCount; i++) {
            int yy = pluginStartY + i * 18;
            graphics.blit(RenderPipelines.GUI_TEXTURED,
                    Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_sidebar_slot.png"),
                    pluginX - 1, yy, 0, 0, 16, 16, 16, 16);
        }
    }

    @FunctionalInterface
    private interface ScreenPluginAction { void run(org.creepebucket.programmable_magic.wand_plugins.BasePlugin plugin, int x, int y, GuiGraphics g); }

    private void applyPlugins(ScreenPluginAction action, GuiGraphics graphics) {
        var win = Minecraft.getInstance().getWindow();
        int sw = win.getGuiScaledWidth();
        int x = sw - 24;
        int startY = 10;
        int n = this.menu.getPluginSlotCapacity();
        for (int i = 0; i < n; i++) {
            var st = this.menu.getPluginItem(i);
            if (st == null || st.isEmpty()) continue;
            var plugin = WandPluginRegistry.createPlugin(st.getItem());
            int y = startY + i * 18;
            action.run(plugin, x, y, graphics);
        }
    }

    /**
     * 插件列表变动后，重新初始化界面以触发插件启动逻辑与控件重建。
     */
    

    @Override
    /**
     * 鼠标滚动：当在左侧栏区域内时按行滚动并同步给 Menu。
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var win = Minecraft.getInstance().getWindow();
        int sh = win.getGuiScaledHeight();

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

    @Override
    /**
     * 鼠标释放：若处于充能状态，则计算充能时长并发送释放数据包。
     */
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean ret = super.mouseReleased(event);
        if (this.isCharging) {
            double chargeSec = Math.max(0, this.chargeTicks) / 20.0;

            // 由服务端重建当前魔杖中的法术清单，这里只传递充能时间
            java.util.List<ItemStack> spells = java.util.List.of();
            // 附带插件列表（从当前持有魔杖的数据组件读取）
            java.util.List<net.minecraft.world.item.ItemStack> plugins = new java.util.ArrayList<>();
            {
                net.minecraft.world.item.ItemStack main = this.minecraft.player.getMainHandItem();
                net.minecraft.world.item.ItemStack off = this.minecraft.player.getOffhandItem();
                net.minecraft.world.item.ItemStack wand = main.getItem() instanceof Wand ? main : off;
                java.util.List<net.minecraft.world.item.ItemStack> saved = wand.get(ModDataComponents.WAND_PLUGINS.get());
                if (saved != null) for (var it : saved) { if (it != null && !it.isEmpty()) plugins.add(it.copy()); }
            }
            var payload = new SpellReleasePacket(spells, chargeSec, plugins);
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) connection.send(new ServerboundCustomPayloadPacket(payload));

            this.isCharging = false;
            this.chargeTicks = 0; // 松开后重置充能时间
            return true;
        }
        return ret;
    }

    /**
     * 绘制一个法术视窗槽位：索引在容量内绘制普通槽，否则绘制越界槽。
     */
    public void drawSpellSlot(GuiGraphics guiGraphics, int index, int x, int y) {
        List<Identifier> numbers = List.of(
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_0.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_1.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_2.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_3.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_4.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_5.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_6.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_7.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_8.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_9.png"));

        int cap = this.menu.getSpellSlotCapacity();
        if (index < cap) {

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_slot.png"),
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
                    Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_out_of_bound.png"),
                    x, y, 0, 0, 16, 16, 16, 16);
        }
    }

    /**
     * 供插件获取当前聚合后的充能功率（W）。
     */
    public double getMenuChargeRate() { return this.menu.getChargeRate(); }

    /**
     * 纹理按钮：悬停/按下两态，回调触发。
     */
    public static class ImageButtonWidget extends AbstractWidget {
        private final Identifier normal;
        private final Identifier pressed;
        private final Runnable onPress;

        public ImageButtonWidget(int x, int y, int w, int h,
                                 Identifier normal, Identifier pressed,
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
        public void onClick(MouseButtonEvent event, boolean fromMouse) {
            if (this.onPress != null) this.onPress.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            this.defaultButtonNarrationText(narration);
        }
    }

    /**
     * 侧栏互斥开关按钮。
     */
    public static class SidebarToggleWidget extends AbstractWidget {
        private final Identifier normal;
        private final Identifier selectedTex;
        private final Runnable onPress;
        private boolean selected;

        public SidebarToggleWidget(int x, int y, int w, int h,
                                   Identifier normal, Identifier selectedTex,
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
        public void onClick(MouseButtonEvent event, boolean fromMouse) {
            if (this.onPress != null) this.onPress.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            this.defaultButtonNarrationText(narration);
        }
    }

    /**
     * 向屏幕添加可渲染组件（透明转发父类实现，便于外部统一调用）。
     */
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }
}
