package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.github.maxopoly.SkilUp.listeners.ListenerUser;

public class ShearListener extends AbstractListener {
	
	private EntityType entity;
	
	public ShearListener(ListenerUser listenerUser, EntityType entity) {
		super(listenerUser);
		this.entity = entity;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void shearing(PlayerShearEntityEvent e) {
		if (e.getEntity().getType() == entity) {
			tellUser(e, e.getPlayer());
		}
	}
}
