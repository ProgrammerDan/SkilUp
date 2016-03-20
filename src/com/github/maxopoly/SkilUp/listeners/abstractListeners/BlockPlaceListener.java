package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.listeners.ListenerUser;

public class BlockPlaceListener extends AbstractListener {

	private Material material;
	private Integer durability;
	private List <String> lore;
	
	public BlockPlaceListener(ListenerUser listenerUser, Material material,
			Integer durability, List <String> lore) {
		super(listenerUser);
		this.material = material;
		this.durability = durability;
		this.lore = lore;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void catchEvent(BlockPlaceEvent e) {
		ItemStack is = e.getItemInHand();
		if (material == null || is.getType() == material) {
			if (durability == null || durability == is.getDurability()) {
				if (lore == null || (is.hasItemMeta() && is.getItemMeta().hasLore() && is.getItemMeta().getLore().equals(lore))) {
					tellUser(e, e.getPlayer());
				}
			}
		}
	}
}
