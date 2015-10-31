package com.github.maxopoly.SkilUp.listeners.xpgains;


import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.skills.Skill;

public abstract class AbstractXPListener implements Listener {

	protected int xp;
	protected Skill skill;

	protected AbstractXPListener(Skill skill, int xp) {
		this.skill = skill;
		this.xp = xp;
		SkilUp plugin = SkilUp.getPlugin();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	protected void giveXP(Player player) {
		skill.getStatus(player).giveXP(xp);
	}
	
	protected void giveXP(Player player, int xp) {
		skill.getStatus(player).giveXP(xp);
	}
	

}
