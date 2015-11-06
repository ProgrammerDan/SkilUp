package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.github.maxopoly.SkilUp.skills.Skill;

/**
 * Applies a specific buff or debuff to a player
 * 
 * @author Maxopoly
 *
 */
public class BuffReward extends AbstractReward {
	private PotionEffect pe;

	public BuffReward(Skill skill, int requiredLevel, int index, double chance,
			String info, ItemStack itemRepresentation, String name,
			PotionEffect pe) {
		super(skill, requiredLevel, index, chance, RewardType.BUFF, info,
				itemRepresentation, name);
		this.pe = pe;
	}

	/**
	 * Applies the potion buff to the given player. Give the player as the first
	 * argument, everything else will be ignored
	 */
	public void applyEffect(Object... data) {
		Player p = (Player) data[0];
		if (rollForApplying()) {
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
