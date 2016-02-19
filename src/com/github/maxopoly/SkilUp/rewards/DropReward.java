package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.skills.Skill;

/**
 * Drops a specific item stack as a reward
 * 
 * @author Maxopoly
 *
 */
public class DropReward extends AbstractReward {

	private ItemMap im;

	public DropReward(Skill skill, int requiredLevel, int index, double chance,
			String info, ItemStack itemRepresentation, String name, ItemMap drop) {
		super(skill, requiredLevel, chance, info,
				itemRepresentation, name);
		this.im = drop;
	}

	/**
	 * Drops the itemstack of this instance at the location, which was given as
	 * the first parameter
	 */
	public void applyEffect(Object... data) {
		Location loc = (Location) data[0];
		if (rollForApplying()) {
			for (ItemStack is : im.getItemStackRepresentation()) {
				SkilUp.getPlugin().debug(
						"Dropped " + is.toString() + " at " + loc.toString());
				loc.getWorld().dropItemNaturally(loc, is);
			}
		}
	}

	/**
	 * @return Which item stacks are dropped by this instance
	 */
	public ItemMap getDrop() {
		return im;
	}
	
	public void listenerTriggered(Event e, Player p) {
		
	}
}
