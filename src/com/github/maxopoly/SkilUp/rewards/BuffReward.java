package com.github.maxopoly.SkilUp.rewards;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.misc.RandomModule;
import com.github.maxopoly.SkilUp.skills.Skill;

/**
 * Applies a specific buff or debuff to a living entity
 * 
 * @author Maxopoly
 *
 */
public class BuffReward extends AbstractReward {
	private List<PotionEffect> pe;

	public BuffReward(Skill skill, int requiredLevel, int maximumLevel,
			String info, ItemStack itemRepresentation, String name,
			List<PotionEffect> pe, RandomModule rng) {
		super(skill, requiredLevel, maximumLevel, info, itemRepresentation,
				name, rng);
		this.pe = pe;
	}

	/**
	 * @return Which buff/debuff is being applied by this instance
	 */
	public List<PotionEffect> getBuffs() {
		return pe;
	}

	public void listenerTriggered(Event e, Player p) {
		if (shouldBeGivenOut(p)) {
			for (PotionEffect pot : pe) {
				SkilUp.getPlugin().debug(
						"Gave effect " + pot.toString() + " to "
								+ p.getUniqueId());

				p.addPotionEffect(pot);
				// p.addPotionEffect(pot,true);
				// second version would overwrite any current effect
			}
		}
	}
}
