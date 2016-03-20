package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;

import com.github.maxopoly.SkilUp.listeners.ListenerUser;

public class EntityTameListener extends AbstractListener {

	private EntityType entity;

	public EntityTameListener(ListenerUser listenerUser, EntityType entity) {
		super(listenerUser);
		this.entity = entity;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void shearing(EntityTameEvent e) {
		if (e.getEntityType() == entity && e.getOwner() instanceof Player) {
			tellUser(e, (Player) e.getOwner());
		}
	}

}
