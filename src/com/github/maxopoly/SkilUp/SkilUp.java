package com.github.maxopoly.SkilUp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.maxopoly.SkilUp.commands.CommandHandler;

public class SkilUp extends JavaPlugin {
	private CommandHandler commandHandler;
	private static SkilUp plugin;
	private static SkilUpManager manager;

	@Override
	public void onEnable() {
		commandHandler = new CommandHandler(this);
		plugin = this;
	}

	@Override
	public void onDisable() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		return commandHandler.onCommand(sender, cmd, label, args);
	}

	public static void sendConsoleMessage(String message) {
		plugin.getLogger().info("[Emray] " + message);
	}

	public static SkilUpManager getManager() {
		return manager;
	}
	
	public static SkilUp getPlugin() {
		return plugin;
	}

}
