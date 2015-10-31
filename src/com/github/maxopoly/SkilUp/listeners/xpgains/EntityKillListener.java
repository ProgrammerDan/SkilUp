package com.github.maxopoly.SkilUp.listeners.xpgains;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import com.github.maxopoly.SkilUp.skills.Skill;

public class EntityKillListener extends AbstractXPListener{
	private EntityType entityType;

	public EntityKillListener(Skill skill, int xp, EntityType type) {
		super(skill, xp);
		this.entityType = type;
	}

	@EventHandler
	public void shearing(EntityDeathEvent e) {
		if (entityType == null
				|| (e.getEntity() != null && e.getEntity().getType() == entityType)) {
			if (e.getEntity().getKiller() instanceof Player) {
				giveXP((Player) e.getEntity().getKiller());
			}
		}
	}

}
