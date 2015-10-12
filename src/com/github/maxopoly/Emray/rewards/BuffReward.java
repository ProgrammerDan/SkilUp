package com.github.maxopoly.Emray.rewards;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.github.maxopoly.Emray.skills.Skill;

public class BuffReward extends AbstractReward<Player> {
	PotionEffect pe;

	BuffReward(Skill skill, int requiredLevel, int index, double chance,
			PotionEffect pe) {
		super(skill, requiredLevel, index, chance, RequiredDataForReward.PLAYER);
		this.pe = pe;
	}

	public void applyEffect(Player p) {
		if (rollForApplying()) {
			p.addPotionEffect(pe);
			// p.addPotionEffect(pe,true);
			// second version would overwrite any current effect
		}
	}
}
