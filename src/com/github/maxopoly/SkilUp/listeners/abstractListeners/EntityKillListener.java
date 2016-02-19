package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import com.github.maxopoly.SkilUp.ListenerUser;

public class EntityKillListener extends AbstractListener {

	private EntityType entity;
	public EntityKillListener(ListenerUser listenerUser, EntityType entity) {
		super(listenerUser);
		this.entity = entity;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void kill(EntityDeathEvent e) {
		if (e.getEntity().getType() == entity && e.getEntity().getKiller() instanceof Player) {
			tellUser(e, e.getEntity().getKiller());
		}
	}

}
