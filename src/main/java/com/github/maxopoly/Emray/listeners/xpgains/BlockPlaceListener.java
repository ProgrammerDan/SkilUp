package com.github.maxopoly.Emray.listeners.xpgains;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.Emray.skills.Skill;

public class BlockPlaceListener extends AbstractXPListener {

	private Material material;
	private Integer durability;
	private String lore;
	
	public BlockPlaceListener(Skill skill, int xp, Material material,
			int durability, String lore) {
		super(skill, xp);
		this.material = material;
		this.durability = durability;
		this.lore = lore;
	}
	
	@EventHandler
	public void catchEvent(BlockPlaceEvent e) {
		ItemStack is = e.getItemInHand();
		if (material == null || is.getType() == material) {
			if (durability == null || durability == is.getDurability()) {
				if (lore == null || (is.hasItemMeta() && is.getItemMeta().hasLore() && is.getItemMeta().getLore().equals(lore))) {
					giveXP(e.getPlayer());
				}
			}
		}
	}
}
