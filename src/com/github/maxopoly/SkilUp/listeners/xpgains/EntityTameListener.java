package com.github.maxopoly.SkilUp.listeners.xpgains;

import java.util.HashMap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTameEvent;

import com.github.maxopoly.SkilUp.skills.Skill;

public class EntityTameListener extends AbstractMappedXPListener<EntityType> {

	public EntityTameListener(Skill skill, HashMap<EntityType, Integer> entities) {
		super(skill, entities);
	}

	@EventHandler
	public void shearing(EntityTameEvent e) {
		if (e.getOwner() instanceof Player) {
			giveXP((Player) e.getOwner(), e.getEntityType());
		}
	}

}
