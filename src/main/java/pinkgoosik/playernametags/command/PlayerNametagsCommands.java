package pinkgoosik.playernametags.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import pinkgoosik.playernametags.PlayerNametagsMod;
import pinkgoosik.playernametags.config.PlayerNametagsConfig;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerNametagsCommands {

	public static final SuggestionProvider<ServerCommandSource> SUGGEST_PERMISSION = (context, builder) -> {
		String remains = builder.getRemaining();

		for(String permission : PlayerNametagsMod.config.formatPerPermission.keySet()) {
			if(permission.contains(remains)) {
				builder.suggest(permission);
			}
		}
		return builder.buildFuture();
	};

	static final String[] modes = new String[]{"gray-out", "hide", "none"};

	public static final SuggestionProvider<ServerCommandSource> SUGGEST_SNEAKING_MODE = (context, builder) -> {
		String remains = builder.getRemaining();

		for(String mode : modes) {
			if(mode.contains(remains)) {
				builder.suggest(mode);
			}
		}
		return builder.buildFuture();
	};

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("player-nametags").requires(source -> source.hasPermissionLevel(3)).then(literal("reload").executes(context -> {
			PlayerNametagsMod.config = PlayerNametagsConfig.read();
			context.getSource().sendFeedback(() -> Text.of("Config successfully reloaded!"), true);
			return 1;
		})).then(literal("format").then(argument("format", StringArgumentType.greedyString()).executes(context -> {
			String format = StringArgumentType.getString(context, "format");
			PlayerNametagsMod.config.format = format;
			PlayerNametagsMod.config.save();
			context.getSource().sendFeedback(() -> Text.of("Format successfully changed to: " + format), true);
			return 1;
		}))).then(literal("enabled").then(argument("enabled", BoolArgumentType.bool()).executes(context -> {
			boolean enabled = BoolArgumentType.getBool(context, "enabled");
			PlayerNametagsMod.config.enabled = enabled;
			PlayerNametagsMod.config.save();
			context.getSource().sendFeedback(() -> Text.of("Player Nametags is now " + (enabled ? "enabled" : "disabled")), true);
			return 1;
		}))).then(literal("update-rate").then(argument("ticks", IntegerArgumentType.integer()).executes(context -> {
			int updateRate = IntegerArgumentType.getInteger(context, "ticks");
			PlayerNametagsMod.config.updateRate = updateRate;
			PlayerNametagsMod.config.save();
			context.getSource().sendFeedback(() -> Text.of("Update rate successfully changed to: " + updateRate + " ticks"), true);
			return 1;
		}))).then(literal("when-sneaking").then(argument("mode", StringArgumentType.word()).suggests(SUGGEST_SNEAKING_MODE).executes(context -> {
			String mode = StringArgumentType.getString(context, "mode");
			PlayerNametagsMod.config.whenSneaking = mode;
			PlayerNametagsMod.config.save();
			context.getSource().sendFeedback(() -> Text.of("Sneaking mode successfully changed to: " + mode), true);
			return 1;
		}))).then(literal("format-per-permission").then(literal("add").then(argument("permission", StringArgumentType.word()).then(argument("format", StringArgumentType.greedyString()).executes(context -> {
			String format = StringArgumentType.getString(context, "format");
			String permission = StringArgumentType.getString(context, "permission");

			var map = PlayerNametagsMod.config.formatPerPermission;

			if(map.containsKey(permission)) {
				context.getSource().sendFeedback(() -> Text.of("Format for permission " + permission + " changed to: " + format), true);
			}
			else {
				context.getSource().sendFeedback(() -> Text.of("Added format for permission " + permission + ": " + format), true);
			}
			map.put(permission, format);

			PlayerNametagsMod.config.save();
			return 1;
		})))).then(literal("remove").then(argument("permission", StringArgumentType.word()).suggests(SUGGEST_PERMISSION).executes(context -> {
			String permission = StringArgumentType.getString(context, "permission");

			var map = PlayerNametagsMod.config.formatPerPermission;

			if(map.containsKey(permission)) {
				map.remove(permission);
				PlayerNametagsMod.config.save();
				context.getSource().sendFeedback(() -> Text.of("Removed format for permission " + permission), true);
			}
			else {
				context.getSource().sendFeedback(() -> Text.of("There is no format for permission " + permission), true);
			}

			return 1;
		})))));
	}
}
