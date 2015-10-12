package com.github.maxopoly.Emray;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.maxopoly.Emray.commands.CommandHandler;

public class Emray extends JavaPlugin {
	private CommandHandler commandHandler;
	private static Emray plugin;
	private static EmrayManager manager;

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

	public static EmrayManager getManager() {
		return manager;
	}
	
	public static Emray getPlugin() {
		return plugin;
	}

}
