package org.creepebucket.programmable_magic.events.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import org.creepebucket.programmable_magic.ModUtils;
import org.creepebucket.programmable_magic.gui.command.NetworkInfoMenu;
import org.creepebucket.programmable_magic.mananet.NetNodeBlockEntity;
import org.creepebucket.programmable_magic.mananet.NetworkManaData;
import org.creepebucket.programmable_magic.mananet.NetworkManaManager;
import org.creepebucket.programmable_magic.registries.ModAttachments;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public abstract class CommandHandler {
	public final String subCommand;

	public CommandHandler(String subCommand) {
		this.subCommand = subCommand;
	}

	public abstract void handle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;

	public ArgumentBuilder<CommandSourceStack, ?> build() {
		String[] parts = subCommand.split("/");
		var leaf = literal(parts[parts.length - 1])
			.executes(ctx -> {
				handle(ctx);
				return 1;
			});
		for (int i = parts.length - 2; i >= 0; i--) {
			leaf = literal(parts[i]).then(leaf);
		}
		return leaf;
	}

	public static class ManaNetworkInfoHandler extends CommandHandler {

		public ManaNetworkInfoHandler() {
			super("network/info");
		}

		@Override
		public void handle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
			ServerPlayer player = ctx.getSource().getPlayerOrException();
			player.openMenu(
				new SimpleMenuProvider((containerId, inventory, extra) -> new NetworkInfoMenu(containerId, inventory), Component.literal("Mana Network")),
				buf -> {}
			);
		}
	}

	public static class ManaNetworkSetHandler extends CommandHandler {

		private static final SuggestionProvider<CommandSourceStack> SUGGEST_NETWORK_IDS = (ctx, builder) ->
			SharedSuggestionProvider.suggest(
				NetworkManaManager.getAllData(ctx.getSource().getLevel()).keySet().stream().map(String::valueOf).toList(),
				builder
			);

		public ManaNetworkSetHandler() {
			super("");
		}

		@Override
		public ArgumentBuilder<CommandSourceStack, ?> build() {
			var manaArgs = argument("radiation", DoubleArgumentType.doubleArg())
				.then(argument("temperature", DoubleArgumentType.doubleArg())
					.then(argument("momentum", DoubleArgumentType.doubleArg())
						.then(argument("pressure", DoubleArgumentType.doubleArg())
							.executes(ctx -> { handle(ctx); return 1; }))));

			var idBranch = literal("id")
				.then(argument("network_id", LongArgumentType.longArg())
					.suggests(SUGGEST_NETWORK_IDS)
					.then(manaArgs));

			var posBranch = literal("pos")
				.then(argument("pos", BlockPosArgument.blockPos()).then(manaArgs));

			return literal("network")
				.then(literal("set")
					.then(idBranch)
					.then(posBranch));
		}

		@Override
		public void handle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
			ServerPlayer player = ctx.getSource().getPlayerOrException();
			var level = player.level();

			double r = ctx.getArgument("radiation", Double.class) / 1000.0;
			double t = ctx.getArgument("temperature", Double.class) / 1000.0;
			double m = ctx.getArgument("momentum", Double.class) / 1000.0;
			double p = ctx.getArgument("pressure", Double.class) / 1000.0;
			ModUtils.Mana mana = new ModUtils.Mana(r, t, m, p);

			Long networkId;
			if (ctx.getNodes().stream().anyMatch(n -> n.getNode().getName().equals("network_id"))) {
				networkId = ctx.getArgument("network_id", Long.class);
			} else {
				BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
				if (!(level.getBlockEntity(pos) instanceof NetNodeBlockEntity node)) {
					ctx.getSource().sendFailure(Component.literal("该坐标不是魔力网络节点"));
					return;
				}
				networkId = node.getData(ModAttachments.NETWORK_ID);
			}

			var data = NetworkManaManager.getManaData(level, networkId);
			data.setCurrent(mana);
			NetworkManaManager.update(data);
			NetworkManaManager.touch(level, networkId);

			ctx.getSource().sendSuccess(() -> Component.literal("已将网络 " + networkId + " 魔力设置为 辐射:" + r + "KJ 温度:" + t + "KJ 动量:" + m + "KJ 压力:" + p + "KJ"), true);
		}
	}
}
