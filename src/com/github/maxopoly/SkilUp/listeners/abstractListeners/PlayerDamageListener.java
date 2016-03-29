package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import com.github.maxopoly.SkilUp.listeners.ListenerUser;

public class PlayerDamageListener extends AbstractListener {
	private EntityDamageEvent.DamageCause cause;
	private double minimumDamage;
	
	public PlayerDamageListener(ListenerUser listenerUser, EntityDamageEvent.DamageCause cause, double minimumDamage) {
		super(listenerUser);
		this.cause = cause;
		this.minimumDamage = minimumDamage;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST )
	public void event(EntityDamageEvent e) {
		if ((e.getEntityType() != EntityType.PLAYER) || (e.getCause() != cause) || (e.getDamage() < minimumDamage)) {
			return;
		}
		tellUser(e, (Player) e.getEntity());		
	}
	
	public double getMinimumDamage() {
		return minimumDamage;
	}
	
	public EntityDamageEvent.DamageCause getCause() {
		return cause;
	}
}
