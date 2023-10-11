package pinkgoosik.playernametags;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pinkgoosik.playernametags.command.PlayerNametagsCommands;
import pinkgoosik.playernametags.config.PlayerNametagsConfig;

import java.util.*;

public class PlayerNametagsMod implements ModInitializer {
	public static final String MOD_ID = "player-nametags";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static LinkedHashMap<UUID, ElementHolder> holders = new LinkedHashMap<>();

	public static PlayerNametagsConfig config;

	@Override
	public void onInitialize() {
		config = PlayerNametagsConfig.read();
		PlayerNametagsCommands.init();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if(config.enabled) {
				server.getPlayerManager().getPlayerList().forEach(PlayerNametagsMod::updateHolder);

				if(config.updateRate >= 1 && server.getTicks() % config.updateRate == 0) {
					updateNametags(server);
				}
			}
			else {
				if(!holders.isEmpty()) {
					holders.forEach((uuid, holder) -> holder.destroy());
					holders.clear();
				}
			}
		});
	}

	public static ElementHolder updateHolder(ServerPlayerEntity player) {
		ElementHolder holder;

		if (!holders.containsKey(player.getUuid())) {
			holder = new ElementHolder();

			ItemDisplayElement element = new ItemDisplayElement();
			element.setBillboardMode(DisplayEntity.BillboardMode.CENTER);

			Text text = Placeholders.parseText(TextParserUtils.formatText(getFormat(player)), PlaceholderContext.of(player));

			element.setCustomName(text);
			element.setCustomNameVisible(!player.isInvisible());

			holder.addElement(element);
			holders.put(player.getUuid(), holder);
		}
		else {
			holder = holders.get(player.getUuid());
		}

		holder.getElements().forEach(virtualElement -> {
			if(virtualElement instanceof ItemDisplayElement element) {
				if(player.isSneaking()) {
					switch (config.whenSneaking) {
						case "gray-out" -> element.setSneaking(true);
						case "hide" -> element.setCustomNameVisible(false);
					}
				}
				else {
					element.setSneaking(false);
				}
			}
		});

		EntityAttachment.of(holder, player);
		VirtualEntityUtils.addVirtualPassenger(player, holder.getEntityIds().getInt(0));

		return holder;
	}

	public static void updateNametags(MinecraftServer server) {
		holders.forEach((uuid, holder) -> {
			var player = server.getPlayerManager().getPlayer(uuid);
			if(player != null) {
				Text text = Placeholders.parseText(TextParserUtils.formatText(getFormat(player)), PlaceholderContext.of(player));
				holder.getElements().forEach(virtualElement -> {
					if(virtualElement instanceof ItemDisplayElement element) {
						element.setCustomName(text);
						element.tick();
					}
				});
			}
		});
	}

	public static String getFormat(ServerPlayerEntity player) {

		for(Map.Entry<String, String> entry : config.formatPerPermission.entrySet()) {
			if(Permissions.check(player, entry.getKey())) return entry.getValue();
		}

		return config.format;
	}
}
