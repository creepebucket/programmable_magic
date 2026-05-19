package org.creepebucket.programmable_magic.gui.machines.solar_panel;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;
import org.creepebucket.programmable_magic.mananet.mechines.solar_panel.SolarPanelBlockEntity;

public class SolarPanelHooks {
    public static void onSwitch(SolarPanelMenu menu, boolean enabled) {
        menu.powerSwitch.trigger(enabled);
    }

    public static class PowerSwitchHook extends Hook {
        public SolarPanelMenu menu;

        public PowerSwitchHook(SolarPanelMenu menu) {
            super("solar_panel_switch");
            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (player.level().isClientSide()) return;

            var enabled = (Boolean) args[0];
            var blockEntity = (SolarPanelBlockEntity) player.level().getBlockEntity(menu.pos);
            blockEntity.enabled = enabled;
            blockEntity.setChanged();
        }
    }
}
