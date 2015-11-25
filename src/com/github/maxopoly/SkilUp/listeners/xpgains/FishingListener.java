package com.github.maxopoly.SkilUp.listeners.xpgains;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;

import com.github.maxopoly.SkilUp.skills.Skill;

public class FishingListener extends
		AbstractMappedXPListener<PlayerFishEvent.State> {

	public FishingListener(Skill skill,
			HashMap<PlayerFishEvent.State, Integer> xp) {
		super(skill, xp);
	}

	@EventHandler
	public void fishing(PlayerFishEvent e) {
		giveXP(e.getPlayer(), e.getState());
	}

}
