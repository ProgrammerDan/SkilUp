package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.ListenerUser;

public class ConsumptionListener extends AbstractListener {
	private Material material;
	private Integer durability;
	private List <String> lore;

	public ConsumptionListener(ListenerUser listenerUser, Material material,
			Integer durability, List <String> lore) {
		super(listenerUser);
		this.material = material;
		this.durability = durability;
		this.lore = lore;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void catchEvent(PlayerItemConsumeEvent e) {
		ItemStack is = e.getItem();
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
