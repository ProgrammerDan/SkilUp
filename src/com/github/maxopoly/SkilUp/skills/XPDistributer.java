package com.github.maxopoly.SkilUp.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.github.maxopoly.SkilUp.listeners.ListenerUser;

public class XPDistributer implements ListenerUser {
	private Skill skill;
	private int xp;
	
	public XPDistributer(Skill skill, int xp) {
		this.skill = skill;
		this.xp = xp;
	}

	public void listenerTriggered(Event e, Player p) {
		if (p != null) {
			skill.getStatus(p).giveXP(xp);
			System.out.println("Giving " + xp);
		}
	}
}
