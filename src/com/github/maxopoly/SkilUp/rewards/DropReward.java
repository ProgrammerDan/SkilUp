package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.skills.Skill;

public class DropReward extends AbstractReward<Location> {

	private ItemStack is;

	public DropReward(Skill skill, int requiredLevel, int index, double chance,
			ItemStack is) {
		super(skill, requiredLevel, index, chance,
				RequiredDataForReward.LOCATION);
		this.is = is;
	}

	public void applyEffect(Location loc) {
		if (rollForApplying()) {
			loc.getWorld().dropItemNaturally(loc, is);
		}
	}
}
