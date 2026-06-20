package org.creepebucket.programmable_magic.gui.machines.api;

import net.minecraft.world.entity.player.Player;
import org.creepebucket.programmable_magic.gui.lib.api.hooks.Hook;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;

public class MachineHooks {
    public static class PowerSwitchHook extends Hook {
        public MachineMenu menu;

        public PowerSwitchHook(MachineMenu menu) {
            super("machine_power_switch");
            this.menu = menu;
        }

        @Override
        public void handle(Player player, Object... args) {
            if (player.level().isClientSide()) return;

            var enabled = (Boolean) args[0];
            var blockEntity = (NetNodeBlockEntity) player.level().getBlockEntity(menu.pos);
            blockEntity.enabled = enabled;
            blockEntity.setChanged();
        }
    }
}
