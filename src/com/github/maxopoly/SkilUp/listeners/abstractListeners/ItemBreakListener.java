package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.ListenerUser;

public class ItemBreakListener extends AbstractListener {
	private Material material;
	private List <String> lore;

	public ItemBreakListener(ListenerUser listenerUser, Material material, List <String> lore) {
		super(listenerUser);
		this.material = material;
		this.lore = lore;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void catchEvent(PlayerItemBreakEvent e) {
		ItemStack is = e.getBrokenItem();
		if (material == null || is.getType() == material) {
			if (lore == null
					|| (is.hasItemMeta() && is.getItemMeta().hasLore() && is
							.getItemMeta().getLore().equals(lore))) {
				tellUser(e, e.getPlayer());
			}
		}
	}
}
