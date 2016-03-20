package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.listeners.ListenerUser;

public class FishingListener extends AbstractListener {

	private Material material;
	private Integer durability;
	private List<String> lore;

	public FishingListener(ListenerUser listenerUser, Material material,
			Integer durability, List<String> lore) {
		super(listenerUser);
		this.material = material;
		this.durability = durability;
		this.lore = lore;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void fishing(PlayerFishEvent e) {
		ItemStack is = null;
		if (e.getCaught() != null && e.getCaught() instanceof Item) {
			is = ((Item) e.getCaught()).getItemStack();
		}
		else {
			return;
		}
		if (material == null || is.getType() == material) {
			if (durability == null || durability == is.getDurability()) {
				if (lore == null
						|| (is.hasItemMeta() && is.getItemMeta().hasLore() && is
								.getItemMeta().getLore().equals(lore))) {
					tellUser(e, e.getPlayer());
				}
			}
		}
	}

}
