package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.misc.RandomModule;
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

	public DamageModifierReward(Skill skill, int requiredLevel,
			int maximumLevel, String info, ItemStack itemRepresentation,
			String name, RandomModule rng, double multiplier) {
		super(skill, requiredLevel, maximumLevel, info, itemRepresentation,
				name, rng);
		this.multiplier = multiplier;
	}

	/**
	 * @return The multiplier of the damage dealt
	 */
	public double getDamageMultiplier() {
		return multiplier;
	}

	public void listenerTriggered(Event e, Player p) {
		EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) e;
		edbee.setDamage(edbee.getDamage() * multiplier);
		// changes only raw damage, not the actual damage
	}

}
