package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.skills.Skill;

/**
 * Allows to modify the raw damage of any given entity damage event with a
 * multiplier
 * 
 * @author Max
 *
 */
public class DamageModifierReward extends AbstractReward {
	private double multiplier;

	public DamageModifierReward(Skill skill, int requiredLevel, int index,
			double chance, String info, ItemStack itemRepresentation,
			String name, double multiplier) {
		super(skill, requiredLevel, index, chance,
				RewardType.ENTITYDAMAGEEVENT, info, itemRepresentation, name);
		this.multiplier = multiplier;
	}

	/**
	 * EntityDamageEvent as only argument
	 */
	public void applyEffect(Object... data) {
		EntityDamageEvent e = (EntityDamageEvent) data[0];
		if (rollForApplying()) {
			e.setDamage(e.getDamage() * multiplier);
		}
	}

	/**
	 * @return The multiplier of the damage dealt
	 */
	public double getDamageMultiplier() {
		return multiplier;
	}

}
