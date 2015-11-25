package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.skills.Skill;

/**
 * Applies a specific buff or debuff to a living entity
 * 
 * @author Maxopoly
 *
 */
public class BuffReward extends AbstractReward {
	private PotionEffect pe;

	public BuffReward(Skill skill, int requiredLevel, int index, double chance,
			String info, ItemStack itemRepresentation, String name,
			PotionEffect pe) {
		super(skill, requiredLevel, index, chance, RewardType.LIVINGENTITY,
				info, itemRepresentation, name);
		this.pe = pe;
	}

	/**
	 * Applies the potion buff to the given entity. Give the living entity as
	 * the only argument
	 */
	public void applyEffect(Object... data) {
		LivingEntity p = (LivingEntity) data[0];
		if (rollForApplying()) {
			SkilUp.getPlugin().debug(
					"Gave effect " + pe.toString() + " to " + p.getUniqueId()
							+ " which is " + p.getClass().toString());
			p.addPotionEffect(pe);
			// p.addPotionEffect(pe,true);
			// second version would overwrite any current effect
		}
	}

	/**
	 * @return Which buff/debuff is being applied by this instance
	 */
	public PotionEffect getBuff() {
		return pe;
	}
}
