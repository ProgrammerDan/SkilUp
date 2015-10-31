package com.github.maxopoly.SkilUp.commands.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;


public class SkilUpMenu extends PlayerCommand{
	public SkilUpMenu(String name) {
		super(name);
		setIdentifier("sum");
		setDescription("Opens SkilUps menu");
		setUsage("/sum");
		setArguments(0, 0);
	}
	
	@Override
	public boolean execute(CommandSender sender, String [] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to perform this command.");
			return true;
		}
		Player p = (Player) sender;
		//openMenu(p);
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}
	
	
}
