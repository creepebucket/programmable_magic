package org.creepebucket.programmable_magic.gui.wand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.creepebucket.programmable_magic.gui.lib.api.Widget;
import org.creepebucket.programmable_magic.items.Wand;
import org.creepebucket.programmable_magic.network.dataPackets.SpellReleasePacket;
import org.creepebucket.programmable_magic.registries.ModDataComponents;
import org.creepebucket.programmable_magic.spells.old.SpellItemLogic;
import org.creepebucket.programmable_magic.spells.old.SpellUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

public final class WandUiWidgets {
    private WandUiWidgets() { }

    public static final int RELEASE_BUTTON_WIDTH = 112;
    public static final int RELEASE_BUTTON_HEIGHT = 16;
    public static final int RELEASE_BUTTON_BOTTOM_OFFSET = 100;

    public static int release_button_screen_x(int screen_width) {
        return screen_width / 2 - RELEASE_BUTTON_WIDTH / 2;
    }

    public static int release_button_screen_y(int screen_width, int screen_height) {
        return screen_height - RELEASE_BUTTON_BOTTOM_OFFSET - WandLayout.compact_mode_y_offset(screen_width);
    }

    public static boolean is_in_release_button(MouseButtonEvent event, int screen_width, int screen_height) {
        int x = release_button_screen_x(screen_width);
        int y = release_button_screen_y(screen_width, screen_height);
        double mx = event.x();
        double my = event.y();
        return mx >= x && mx < x + RELEASE_BUTTON_WIDTH && my >= y && my < y + RELEASE_BUTTON_HEIGHT;
    }

    public static final class SpellSlotsBarWidget extends Widget {
        private final WandMenu menu;

        private final List<Identifier> numbers = List.of(
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_0.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_1.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_2.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_3.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_4.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_5.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_6.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_7.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_8.png"),
                Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_font_9.png")
        );

        public SpellSlotsBarWidget(WandMenu menu) {
            this.menu = menu;
        }

        @Override
        public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var win = Minecraft.getInstance().getWindow();
            int sw = win.getGuiScaledWidth();
            int sh = win.getGuiScaledHeight();
            int left = this.menu.ui().bounds().guiLeft();
            int top = this.menu.ui().bounds().guiTop();

            int cap = this.menu.getSpellSlotCapacity();
            int visible = WandLayout.visible_spell_slots(sw);
            int y = sh + WandLayout.SPELL_SLOT_OFFSET - WandLayout.compact_mode_y_offset(sw) - top;

            for (int i = 0; i < visible; i++) {
                int index = this.menu.spellIndexOffset + i;
                int x = WandLayout.spell_slot_x(sw, i) - left;
                if (index < cap) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED,
                            Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_slot.png"),
                            x, y, 0, 0, 16, 16, 16, 16);

                    graphics.blit(RenderPipelines.GUI_TEXTURED, numbers.get(Math.floorDiv(index, 100) % 10), x + 1, y - 3, 0, 0, 3, 5, 3, 5);
                    graphics.blit(RenderPipelines.GUI_TEXTURED, numbers.get(Math.floorDiv(index, 10) % 10), x + 5, y - 3, 0, 0, 3, 5, 3, 5);
                    graphics.blit(RenderPipelines.GUI_TEXTURED, numbers.get(Math.floorDiv(index, 1) % 10), x + 9, y - 3, 0, 0, 3, 5, 3, 5);
                } else {
                    graphics.blit(RenderPipelines.GUI_TEXTURED,
                            Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_out_of_bound.png"),
                            x, y, 0, 0, 16, 16, 16, 16);
                }
            }
        }
    }

    public static final class BaseBackgroundWidget extends Widget {
        private final WandMenu menu;

        public BaseBackgroundWidget(WandMenu menu) {
            this.menu = menu;
        }

        @Override
        public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var win = Minecraft.getInstance().getWindow();
            int sw = win.getGuiScaledWidth();
            int sh = win.getGuiScaledHeight();
            int left = this.menu.ui().bounds().guiLeft();
            int top = this.menu.ui().bounds().guiTop();

            boolean compactMode = WandLayout.compact_mode(sw);
            for (int i = 0; i < 36; i++) {
                int x = WandLayout.inventory_bg_x(sw, i) - left;
                int y = WandLayout.inventory_bg_y(sw, sh, i) - top;
                if (i < 9 && !compactMode) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_hotbar.png"), x, y, 0, 0, 16, 16, 16, 16);
                } else if (i >= 9) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_inventory.png"), x, y, 0, 0, 16, 16, 16, 16);
                }
            }

            int pluginCount = this.menu.getPluginSlotCapacity();
            int pluginX = WandLayout.plugin_slot_x(sw) - 1 - left;
            for (int i = 0; i < pluginCount; i++) {
                int y = WandLayout.plugin_slot_y(i) - top;
                graphics.blit(RenderPipelines.GUI_TEXTURED,
                        Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_sidebar_slot.png"),
                        pluginX, y, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    public static final class SpellSupplyBackgroundWidget extends Widget {
        private final WandMenu menu;

        public SpellSupplyBackgroundWidget(WandMenu menu) {
            this.menu = menu;
        }

        @Override
        public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var win = Minecraft.getInstance().getWindow();
            int sh = win.getGuiScaledHeight();
            int left = this.menu.ui().bounds().guiLeft();
            int top = this.menu.ui().bounds().guiTop();

            graphics.fill(17 - left, 0 - top, 18 - left, sh - top, 0xFFFFFFFF);
            graphics.fill(17 + 82 - left, 0 - top, 17 + 82 + 1 - left, sh - top, 0xFFFFFFFF);

            SpellItemLogic.SpellType type = SpellUtils.stringSpellTypeMap.getOrDefault(this.menu.selectedSidebar(), SpellItemLogic.SpellType.COMPUTE_MOD);
            Map<Component, List<ItemStack>> spells = SpellUtils.getSpellsGroupedBySubCategory(type);

            int x = 19 - left;
            int y = 10 - this.menu.supplyScrollRow() * 16 - top;

            for (Map.Entry<Component, List<ItemStack>> entry : spells.entrySet()) {
                Component key = entry.getKey();
                List<ItemStack> items = entry.getValue();

                graphics.drawString(Minecraft.getInstance().font, key.getString(), x, y, 0xFFBF360C);
                for (int i = 0; i < items.size(); i++) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED,
                            Identifier.fromNamespaceAndPath(MODID, "textures/gui/wand_spell_sidebar_slot.png"),
                            x + (i % 5) * 16 - 1, y + Math.floorDiv(i, 5) * 16 + 10, 0, 0, 16, 16, 16, 16);
                }
                y += Math.floorDiv(items.size() - 1, 5) * 16 + 32;
            }
        }
    }

    public static final class SpellReleaseOnMouseReleasedWidget extends Widget {
        private final WandMenu menu;

        public SpellReleaseOnMouseReleasedWidget(WandMenu menu) {
            this.menu = menu;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            if (!this.menu.isCharging) return false;

            var bounds = this.menu.ui().bounds();
            boolean in_release_button = is_in_release_button(event, bounds.sw(), bounds.sh());

            if (in_release_button) {
                double chargeSec = Math.max(0, this.menu.chargeTicks) / 20.0;
                List<ItemStack> spells = List.of();

                List<ItemStack> plugins = new ArrayList<>();
                {
                    ItemStack main = Minecraft.getInstance().player.getMainHandItem();
                    ItemStack off = Minecraft.getInstance().player.getOffhandItem();
                    ItemStack wand = main.getItem() instanceof Wand ? main : off;
                    List<ItemStack> saved = wand.get(ModDataComponents.WAND_PLUGINS.get());
                    if (saved != null) for (var it : saved) if (it != null && !it.isEmpty()) plugins.add(it.copy());
                }

                var payload = new SpellReleasePacket(spells, chargeSec, plugins);
                var connection = Minecraft.getInstance().getConnection();
                if (connection != null) connection.send(new ServerboundCustomPayloadPacket(payload));
            }

            this.menu.isCharging = false;
            this.menu.chargeTicks = 0;
            return in_release_button;
        }
    }

    public static final class ChargeTickWidget extends Widget {
        private final WandMenu menu;

        public ChargeTickWidget(WandMenu menu) {
            this.menu = menu;
        }

        @Override
        public void onTick() {
            if (this.menu.isCharging) this.menu.chargeTicks++;
        }
    }

    public static final class AutoChargeWidget extends Widget {
        private final WandMenu menu;

        public AutoChargeWidget(WandMenu menu) {
            this.menu = menu;
        }

        @Override
        public void onTick() {
            this.menu.isCharging = true;
            this.menu.chargeTicks += 1;
        }
    }
}
