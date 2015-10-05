package com.github.maxopoly.DankMeme;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.maxopoly.DankMeme.commands.CommandHandler;

public class DankMeme extends JavaPlugin {
	CommandHandler commandHandler;

	@Override
	public void onEnable() {
		commandHandler = new CommandHandler(this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		return commandHandler.onCommand(sender, cmd, label, args);
	}
}
