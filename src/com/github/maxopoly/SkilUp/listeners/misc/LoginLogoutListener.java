package com.github.maxopoly.SkilUp.listeners.misc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.SkilUpManager;
import com.github.maxopoly.SkilUp.essences.EssenceTracker;

public class LoginLogoutListener implements Listener {
	private SkilUpManager em;

	public LoginLogoutListener(SkilUp plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		em = plugin.getManager();
	}

	@EventHandler
	public void playerLogin(PlayerLoginEvent e) {
		em.loadPlayerDataFromDataBase(e.getPlayer());
		EssenceTracker et = SkilUp.getEssenceTracker();
		if (et != null) {
			if (!e.getPlayer().hasPlayedBefore()) {
				et.handleFirstLogin(e.getPlayer());
			}
			et.checkForGiveOut(e.getPlayer());
		}
	}

	@EventHandler
	public void playerLogout(PlayerQuitEvent e) {
		em.savePlayerDataToDataBase(e.getPlayer());
	}
}
