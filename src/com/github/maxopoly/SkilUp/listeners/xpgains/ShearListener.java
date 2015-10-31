package com.github.maxopoly.SkilUp.listeners.xpgains;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.github.maxopoly.SkilUp.skills.Skill;

public class ShearListener extends AbstractXPListener {
	private EntityType entityType;

	public ShearListener(Skill skill, int xp, EntityType type) {
		super(skill, xp);
		this.entityType = type;
	}

	@EventHandler
	public void shearing(PlayerShearEntityEvent e) {
		if (entityType == null
				|| (e.getEntity() != null && e.getEntity().getType() == entityType)) {
			giveXP(e.getPlayer());
		}
	}
}
