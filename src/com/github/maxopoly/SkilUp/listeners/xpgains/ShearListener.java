package com.github.maxopoly.SkilUp.listeners.xpgains;

import java.util.HashMap;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.github.maxopoly.SkilUp.skills.Skill;

public class ShearListener extends AbstractMappedXPListener<EntityType> {
	public ShearListener(Skill skill, HashMap<EntityType, Integer> entities) {
		super(skill, entities);
	}

	@EventHandler
	public void shearing(PlayerShearEntityEvent e) {
		giveXP(e.getPlayer(), e.getEntity().getType());
	}
}
