package com.github.maxopoly.SkilUp.listeners.xpgains;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;

import com.github.maxopoly.SkilUp.skills.Skill;

public class FishingListener extends AbstractXPListener {
	private HashMap<PlayerFishEvent.State, Integer> xps;

	public FishingListener(Skill skill,
			HashMap<PlayerFishEvent.State, Integer> xp) {
		super(skill, 0); // 0 because this can give out different amounts of XP
							// based on what's caught/not caught
		this.xps = xp;
	}

	@EventHandler
	public void fishing(PlayerFishEvent e) {
		Integer i = xps.get(e.getState());
		if (i != null) {
			giveXP(e.getPlayer(), i);
		}
	}

}
