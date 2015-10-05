package com.github.maxopoly.Emray;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import static com.github.maxopoly.Emray.Emray.sendConsoleMessage;

public class ConfigParser {
	JavaPlugin plugin;

	ConfigParser(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void parseConfig() {
		sendConsoleMessage("Initializing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		//TODO  Implement this
	}
}
