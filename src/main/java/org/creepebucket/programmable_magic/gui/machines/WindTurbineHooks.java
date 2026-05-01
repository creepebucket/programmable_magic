package org.creepebucket.programmable_magic.gui.machines;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;
import org.creepebucket.programmable_magic.mananet.mechines.wind_turbine.WindTurbineBlockEntity;

public class WindTurbineHooks {
    public static void onSwitch(WindTurbineMenu menu, boolean enabled) {
        menu.powerSwitch.trigger(enabled);
    }

    public static class PowerSwitchHook extends Hook {
        public WindTurbineMenu menu;

        public PowerSwitchHook(WindTurbineMenu menu) {
            super("wind_turbine_switch");
            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (player.level().isClientSide()) return;

            var enabled = (Boolean) args[0];
            var blockEntity = (WindTurbineBlockEntity) player.level().getBlockEntity(menu.pos);
            blockEntity.enabled = enabled;
            blockEntity.setChanged();
        }
    }
}
