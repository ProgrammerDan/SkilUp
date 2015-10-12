package com.github.maxopoly.SkilUp.listeners.effects;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.rewards.AbstractReward;

public class EffectBlockPlaceListener {
	private Material material;
	private Integer durability;
	private AbstractReward reward;
	private String lore;

	public EffectBlockPlaceListener(AbstractReward reward, Material material,
			Integer durability, String lore) {
		this.material = material;
		this.durability = durability;
		this.reward = reward;
		this.lore = lore;
	}
	
	@EventHandler
	public void catchEvent(BlockPlaceEvent e) {
		if (reward.deservesReward(e.getPlayer())) {
			ItemStack hand = e.getItemInHand();
			if (material == null || material == hand.getType()) {
				if (durability == null || durability == hand.getDurability()) {
					if (lore == null || (hand.hasItemMeta() && hand.getItemMeta().getLore().equals(lore))) {
						switch (reward.getRequiredData()) {
						case LOCATION:
							reward.applyEffect(e.getBlock().getLocation());
							break;
						case PLAYER:
							reward.applyEffect(e.getPlayer());
							break;
						default:
							//should never happen
						}
					}
				}
			}
		}
	}
}
