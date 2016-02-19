package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

	public DamageReward(Skill skill, int requiredLevel, double chance,
			String info, ItemStack itemRepresentation, String name, int damage) {
		super(skill, requiredLevel, chance, info, itemRepresentation, name);
		this.damage = damage;
	}

	/**
	 * @return How much damage is dealt
	 */
	public int getDamage() {
		return damage;
	}

	public void listenerTriggered(Event e, Player p) {

	}

}
