package com.github.maxopoly.Emray.listeners.logging;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.maxopoly.Emray.Emray;
import com.github.maxopoly.Emray.EmrayManager;

public class LoginLogoutListener implements Listener {

	Emray plugin;
	EmrayManager em;

	LoginLogoutListener(Emray plugin) {
		this.plugin = plugin;
		em = plugin.getManager();
	}

	@EventHandler
	public void playerLogin(PlayerLoginEvent e) {
		if (!em.loadPlayerDataFromDataBase(e.getPlayer().getName())) {
			em.playerFirstLogin(e.getPlayer());
		}
	}
	
	@EventHandler
	public void playerLogout(PlayerQuitEvent e) {
		em.savePlayerDataToDataBase(e.getPlayer().getName());
	}
}
