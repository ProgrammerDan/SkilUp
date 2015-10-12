package com.github.maxopoly.SkilUp.rewards;

import java.util.Random;

import org.bukkit.entity.Player;

import com.github.maxopoly.SkilUp.skills.Skill;

public abstract class AbstractReward<D> {

	public enum RequiredDataForReward {
		LOCATION, PLAYER;
	}

	private int requiredLevel;
	private int index;
	private Skill skill;
	private double chance;
	private Random rng;
	private RequiredDataForReward rewardData;

	public AbstractReward(Skill skill, int requiredLevel, int index,
			double chance, RequiredDataForReward rewardData) {
		this.skill = skill;
		this.rewardData = rewardData;
		this.requiredLevel = requiredLevel;
		this.index = index;
		this.chance = chance;
		rng = new Random();
	}

	public boolean rollForApplying() {
		return rng.nextDouble() <= chance;
	}

	public boolean deservesReward(Player p) {
		return skill.getStatus(p).deservesReward(index);
	}

	/**
	 * @return The level required for this skill
	 */
	public int getRequiredLevel() {
		return requiredLevel;
	}

	/**
	 * Each effect has an index in the internal tracking of a skill. This is
	 * used to determine without recalculatution during runtime whether the
	 * player deserves the skill
	 * 
	 * @return index of this reward in its skill
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return The skill this reward belongs to
	 */
	public Skill getSkill() {
		return skill;
	}
	
	/**
	 * @return What type of data this reward needs
	 */
	public RequiredDataForReward getRequiredData() {
		return rewardData;
	}

	public abstract void applyEffect(D data);

}
