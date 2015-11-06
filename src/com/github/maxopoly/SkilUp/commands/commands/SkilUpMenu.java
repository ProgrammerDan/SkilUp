package com.github.maxopoly.SkilUp.commands.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.commands.menus.MenuBuilder;
import com.github.maxopoly.SkilUp.skills.Skill;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class SkilUpMenu extends PlayerCommand {
	private MenuBuilder mb;

	public SkilUpMenu(String name) {
		super(name);
		setIdentifier("sum");
		setDescription("Opens SkilUps menu");
		setUsage("/sum");
		setArguments(0, 0);
		mb = new MenuBuilder();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to perform this command.");
			return true;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			mb.showSkillOverview(p);
		} else {
			Skill s = SkilUp.getManager().getSkillByName(args[0]);
			if (s == null) {
				p.sendMessage("This skill does not exist");
			} else {
				mb.showSkillInfo(p, s.getStatus(p));
			}
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}

}
