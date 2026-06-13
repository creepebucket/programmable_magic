package org.creepebucket.programmable_magic.events.commands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.literal;
import static org.creepebucket.programmable_magic.Programmable_magic.MODID;

@EventBusSubscriber(modid = MODID)
public class PrmCommand {
	public static final List<CommandHandler> HANDLERS = new ArrayList<>();

	static {
		registerHandler(new CommandHandler.ManaNetworkInfoHandler());
		registerHandler(new CommandHandler.ManaNetworkSetHandler());
	}

	public static void registerHandler(CommandHandler handler) {
		HANDLERS.add(handler);
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		var root = literal("prm");
		for (var handler : HANDLERS) {
			root.then(handler.build());
		}
		event.getDispatcher().register(root);
	}
}
