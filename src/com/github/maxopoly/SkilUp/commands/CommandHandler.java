package com.github.maxopoly.SkilUp.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.command.Command;

import com.github.maxopoly.SkilUp.commands.commands.SkilUpMenu;

public class CommandHandler {
	private Map<String, Command> commands = new HashMap<String, Command>();

	public void registerCommands() {
		addCommands(new SkilUpMenu("Menu"));
	}

	private void addCommands(Command command) {
		commands.put(command.getIdentifier().toLowerCase(), command);
	}

	public boolean execute(CommandSender sender,
			org.bukkit.command.Command cmd, String[] args) {
		if (commands.containsKey(cmd.getName().toLowerCase())) {
			Command command = commands.get(cmd.getName().toLowerCase());
			if (args.length < command.getMinArguments()
					|| args.length > command.getMaxArguments()) {
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}

	public List<String> complete(CommandSender sender,
			org.bukkit.command.Command cmd, String[] args) {
		if (commands.containsKey(cmd.getName().toLowerCase())) {
			Command command = commands.get(cmd.getName().toLowerCase());
			return command.tabComplete(sender, args);
		}
		return null;
	}

	public void helpPlayer(Command command, CommandSender sender) {
		sender.sendMessage(new StringBuilder()
				.append(ChatColor.RED + "Command: ").append(command.getName())
				.toString());
		sender.sendMessage(new StringBuilder()
				.append(ChatColor.RED + "Description: ")
				.append(command.getDescription()).toString());
		sender.sendMessage(new StringBuilder()
				.append(ChatColor.RED + "Usage: ").append(command.getUsage())
				.toString());
	}
}
