package com.github.maxopoly.SkilUp.listeners.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.SkilUpManager;
import com.github.maxopoly.SkilUp.essences.EssenceTracker;

public class LoginLogoutListener implements Listener {

	public LoginLogoutListener() {
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void playerLogin(PlayerLoginEvent e) {
		if (SkilUp.getManager().isBehindBungee()) return;
		System.out.println("Not behind Bungee: "+ SkilUp.getManager().isBehindBungee());
		final EssenceTracker et = SkilUp.getEssenceTracker();
		if (et != null) {
			final Player p = e.getPlayer();
			new BukkitRunnable() {
				@Override
				public void run() {
					System.out.println("Player: " + p.getUniqueId() + " played: " + p.hasPlayedBefore());
					if (!p.hasPlayedBefore()) {
						et.handleFirstLogin(p);
					} else {
						et.handleLogin(p);
					}
					et.checkForGiveOut(p);
				}
			}.runTask(SkilUp.getPlugin());
		}
	}
}
