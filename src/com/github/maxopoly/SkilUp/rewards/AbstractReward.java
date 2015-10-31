package com.github.maxopoly.SkilUp.rewards;

import java.util.Random;

import org.bukkit.entity.Player;

import com.github.maxopoly.SkilUp.skills.Skill;

public abstract class AbstractReward<D> {

	public enum RewardType {
		BUFF, DROP;
	}

	private int requiredLevel;
	private int index;
	private Skill skill;
	private double chance;
	private static Random rng = new Random();
	private RewardType rewardType;

	public AbstractReward(Skill skill, int requiredLevel, int index,
			double chance, RewardType rewardType) {
		this.skill = skill;
		this.rewardType = rewardType;
		this.requiredLevel = requiredLevel;
		this.index = index;
		this.chance = chance;
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
	 * To make stuff easier, both the skill and the the reward keep a reference
	 * to each other. But because we have to create one of them first, this is
	 * used to set the skill of this reward after it was created. DO NOT EVER
	 * USE THIS OUTSIDE FROM CONFIGPARSING
	 * 
	 * @param skill
	 *            The skill this reward references to
	 */
	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	/**
	 * @return What type of data this reward needs
	 */
	public RewardType getRequiredData() {
		return rewardType;
	}

	public abstract void applyEffect(D data);

}
