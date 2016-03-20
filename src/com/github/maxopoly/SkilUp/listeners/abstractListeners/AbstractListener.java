package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.listeners.ListenerUser;

public abstract class AbstractListener implements Listener {

	protected ListenerUser listenerUser;

	protected AbstractListener(ListenerUser listenerUser) {
		this.listenerUser = listenerUser;
		SkilUp.getPlugin().getServer().getPluginManager().registerEvents(this, SkilUp.getPlugin());
	}
	
	public ListenerUser getListenerUser() {
		return listenerUser;
	}
	
	protected void tellUser(Event e, Player p) {
		listenerUser.listenerTriggered(e, p);
	}
}
