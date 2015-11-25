package com.github.maxopoly.SkilUp.listeners.xpgains;

import java.util.HashMap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import com.github.maxopoly.SkilUp.skills.Skill;

public class EntityKillListener extends AbstractMappedXPListener<EntityType> {

	public EntityKillListener(Skill skill, HashMap<EntityType, Integer> entities) {
		super(skill, entities);
	}

	@EventHandler
	public void kill(EntityDeathEvent e) {
		if (e.getEntity().getKiller() instanceof Player) {
			giveXP((Player)e.getEntity().getKiller(), e.getEntityType());
		}
	}

}
