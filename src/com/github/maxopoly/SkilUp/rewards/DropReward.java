package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.skills.Skill;

/**
 * Drops a specific item stack as a reward
 * 
 * @author Maxopoly
 *
 */
public class DropReward extends AbstractReward {

	private ItemStack is;

	public DropReward(Skill skill, int requiredLevel, int index, double chance,
			String info, ItemStack itemRepresentation, String name,
			ItemStack drop) {
		super(skill, requiredLevel, index, chance, RewardType.LOCATION, info,
				itemRepresentation, name);
		this.is = drop;
	}

	/**
	 * Drops the itemstack of this instance at the location, which was given as
	 * the first parameter
	 */
	public void applyEffect(Object... data) {
		Location loc = (Location) data[0];
		if (rollForApplying()) {
			SkilUp.getPlugin().debug(
					"Dropped " + is.toString() + " at " + loc.toString());
			loc.getWorld().dropItemNaturally(loc, is);
		}
	}

	/**
	 * @return Which item stack is dropped by this instance
	 */
	public ItemStack getDrop() {
		return is;
	}
}
