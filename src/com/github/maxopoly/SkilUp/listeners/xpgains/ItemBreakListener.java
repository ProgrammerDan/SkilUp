package com.github.maxopoly.SkilUp.listeners.xpgains;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.skills.Skill;

public class ItemBreakListener extends AbstractXPListener {
	private Material material;
	private Integer durability;
	private String lore;

	public ItemBreakListener(Skill skill, int xp, Material material,
			Integer durability, String lore) {
		super(skill, xp);
		this.material = material;
		this.durability = durability;
		this.lore = lore;
	}

	@EventHandler
	public void catchEvent(PlayerItemBreakEvent e) {
		ItemStack is = e.getBrokenItem();
		if (material == null || is.getType() == material) {
			if (durability == null || durability == is.getDurability()) {
				if (lore == null
						|| (is.hasItemMeta() && is.getItemMeta().hasLore() && is
								.getItemMeta().getLore().equals(lore))) {
					giveXP(e.getPlayer());
				}
			}
		}
	}
}
