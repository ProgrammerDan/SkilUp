package com.github.maxopoly.SkilUp.listeners.xpgains;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTameEvent;
import com.github.maxopoly.SkilUp.skills.Skill;

public class EntityTameListener extends AbstractXPListener {
	private EntityType entityType;

	public EntityTameListener(Skill skill, int xp, EntityType type) {
		super(skill, xp);
		this.entityType = type;
	}

	@EventHandler
	public void shearing(EntityTameEvent e) {
		if (entityType == null
				|| (e.getEntity() != null && e.getEntity().getType() == entityType)) {
			if (e.getOwner() instanceof Player) {
				giveXP((Player) e.getOwner());
			}
		}
	}

}
