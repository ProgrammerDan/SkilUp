package com.github.maxopoly.SkilUp.listeners.logging;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.SkilUpManager;

public class LoginLogoutListener implements Listener {
	SkilUpManager em;

	LoginLogoutListener(SkilUp plugin) {
		plugin.getServer().getPluginManager()
		.registerEvents(this, plugin);
		em = plugin.getManager();
	}

	@EventHandler
	public void playerLogin(PlayerLoginEvent e) {
		if (!em.loadPlayerDataFromDataBase(e.getPlayer())) {
			em.playerFirstLogin(e.getPlayer());
		}
	}
	
	@EventHandler
	public void playerLogout(PlayerQuitEvent e) {
		em.savePlayerDataToDataBase(e.getPlayer());
	}
}
