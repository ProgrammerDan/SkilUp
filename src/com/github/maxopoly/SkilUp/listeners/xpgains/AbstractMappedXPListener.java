package com.github.maxopoly.SkilUp.listeners.xpgains;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.skills.Skill;

public class AbstractMappedXPListener<E> implements Listener {
	private HashMap<E, Integer> xpValues;
	private Skill skill;

	public AbstractMappedXPListener(Skill skill, HashMap<E, Integer> xpValues) {
		this.skill = skill;
		this.xpValues = xpValues;
		SkilUp plugin = SkilUp.getPlugin();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	protected void giveXP(Player p, E e) {
		Integer v = xpValues.get(e);
		if (v != null) {
			skill.getStatus(p).giveXP(v);
		}

	}
}
