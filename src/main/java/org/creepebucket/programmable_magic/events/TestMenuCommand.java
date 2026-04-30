package org.creepebucket.programmable_magic.events;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.creepebucket.programmable_magic.gui.machines.MachineMenu;

import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class TestMenuCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("testmenu")
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(ctx -> {
                            var player = (ServerPlayer) ctx.getSource().getEntity();
                            player.openMenu(new SimpleMenuProvider(
                                    (containerId, inventory, p) -> new MachineMenu(containerId, inventory),
                                    Component.literal("")
                            ));
                            return 1;
                        })
        );
    }
}
