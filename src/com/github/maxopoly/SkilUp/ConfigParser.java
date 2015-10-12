package com.github.maxopoly.SkilUp;

import org.bukkit.configuration.file.FileConfiguration;

import static com.github.maxopoly.SkilUp.SkilUp.sendConsoleMessage;

public class ConfigParser {
	SkilUp plugin;

	ConfigParser(SkilUp plugin) {
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
