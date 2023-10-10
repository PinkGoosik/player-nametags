package pinkgoosik.playernametags.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import pinkgoosik.playernametags.PlayerNametagsMod;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerNametagsConfig {
	public static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().disableHtmlEscaping().create();

	public boolean enabled = false;
	public String format = "%player:name%";
	public int updateRate = 20;
	public String whenSneaking = "gray-out";

	public LinkedHashMap<String, String> formatPerPermission = new LinkedHashMap<>(Map.of("example.admin", "<red>[Admin] %player:name%"));

	public static PlayerNametagsConfig read() {
		String filePath = FabricLoader.getInstance().getConfigDir().resolve("player-nametags.json").toString();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
			var config = GSON.fromJson(reader, PlayerNametagsConfig.class);
			config.save();
			return config;
		}
		catch(FileNotFoundException e) {
			PlayerNametagsMod.LOGGER.info("File " + filePath + " is not found! Setting to default.");
			var conf = new PlayerNametagsConfig();
			conf.save();
			return conf;
		}
		catch(Exception e) {
			PlayerNametagsMod.LOGGER.info("Failed to read player-nametags config due to an exception. " +
				"Please delete player-nametags.json to regenerate config or fix the issue:\n" + e);
			e.printStackTrace();
			System.exit(0);
			return new PlayerNametagsConfig();
		}
	}

	public void save() {
		try {
			String filePath = FabricLoader.getInstance().getConfigDir().resolve("player-nametags.json").toString();
			try(FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
				writer.write(GSON.toJson(this));
			}
		}
		catch(Exception e) {
			PlayerNametagsMod.LOGGER.info("Failed to save player-nametags config due to an exception:\n" + e);
		}
	}
}
