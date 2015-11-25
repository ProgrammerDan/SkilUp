package com.github.maxopoly.SkilUp.rewards;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.skills.Skill;

public abstract class AbstractReward {
	protected int requiredLevel;
	protected int index;
	protected Skill skill;
	protected double chance;
	protected static Random rng = new Random();
	protected RewardType rewardType;
	protected String info;
	protected String name;
	protected ItemStack itemRepresentation;

	public AbstractReward(Skill skill, int requiredLevel, int index,
			double chance, RewardType rewardType, String info,
			ItemStack itemRepresentation, String name) {
		this.skill = skill;
		this.rewardType = rewardType;
		this.requiredLevel = requiredLevel;
		this.index = index;
		this.chance = chance;
		this.itemRepresentation = itemRepresentation;
		this.info = info;
		this.name = name;
	}

	/**
	 * Takes the chance set for this reward into account and randomly decides
	 * whether a reward should be given out
	 * 
	 * @return True if a reward should be given out, false if not
	 */
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

	/**
	 * @return General information or a description about this reward, which is
	 *         sent to the player, if he selects this reward in the info gui
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @return Which itemstack should represent this reward in a GUI
	 */
	public ItemStack getItemRepresentation() {
		return itemRepresentation;
	}

	/**
	 * @return The name of this reward
	 */
	public String getName() {
		return name;
	}

	/**
	 * Each reward must implement this method to apply it's effect to the
	 * player.
	 * 
	 * @param data
	 *            Whatever kind of data is needed to apply the effect of this
	 *            reward
	 */
	public abstract void applyEffect(Object... data);

}
