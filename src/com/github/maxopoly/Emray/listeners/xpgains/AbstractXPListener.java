package com.github.maxopoly.Emray.listeners.xpgains;


import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.github.maxopoly.Emray.Emray;
import com.github.maxopoly.Emray.skills.Skill;

public abstract class AbstractXPListener implements Listener {

	protected int xp;
	protected Skill skill;

	protected AbstractXPListener(Skill skill, int xp) {
		this.skill = skill;
		this.xp = xp;
		Emray plugin = Emray.getPlugin();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	protected void giveXP(Player player) {
		skill.getStatus(player).giveXP(xp);
	}
	

}
