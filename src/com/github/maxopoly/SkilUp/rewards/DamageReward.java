package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.skills.Skill;

/**
 * Deals damage to any given living entity
 * 
 * @author Maxopoly
 *
 */
public class DamageReward extends AbstractReward {
	private int damage;

	public DamageReward(Skill skill, int requiredLevel, int index,
			double chance, String info, ItemStack itemRepresentation,
			String name, int damage) {
		super(skill, requiredLevel, index, chance, RewardType.PLAYER, info,
				itemRepresentation, name);
		this.damage = damage;
	}

	/**
	 * LivingEntity to deal damage to as only argument
	 */
	public void applyEffect(Object... data) {
		LivingEntity l = (LivingEntity) data[0];
		if (rollForApplying()) {
			l.damage(damage);
			SkilUp.getPlugin().debug(
					name + " dealt " + String.valueOf(damage) + " damage to "
							+ l.getUniqueId() + ", which is "
							+ l.getClass().toString());
		}
	}

	/**
	 * @return How much damage is dealt
	 */
	public int getDamage() {
		return damage;
	}

}
