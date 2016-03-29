package com.github.maxopoly.SkilUp.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.listeners.ListenerUser;
import com.github.maxopoly.SkilUp.misc.RandomModule;
import com.github.maxopoly.SkilUp.skills.Skill;

public abstract class AbstractReward implements ListenerUser {
	protected int requiredLevel;
	protected int maximumLevel;
	protected Skill skill;
	protected RandomModule rng;
	protected String info;
	protected String name;
	protected ItemStack itemRepresentation;

	public AbstractReward(Skill skill, int requiredLevel, int maximumLevel,
			String info, ItemStack itemRepresentation,
			String name, RandomModule rng) {
		this.skill = skill;
		this.maximumLevel = maximumLevel;
		this.requiredLevel = requiredLevel;
		this.itemRepresentation = itemRepresentation;
		this.info = info;
		this.name = name;
		this.rng = rng;
	}

	/**
	 * Checks whether a player fulfills the level requirements for this skill
	 * 
	 * @param p
	 *            Player to check for
	 * @return True if the player fulfills the level requirements, false if not
	 */
	protected boolean deservesReward(Player p) {
		if (p != null) {
			int level = skill.getStatus(p).getLevel();
			return level >= requiredLevel && level <= maximumLevel;
		}
		return false;
	}

	/**
	 * Convenience method that takes both level requirements and random chances
	 * into account to determine whether a player should get a reward
	 * 
	 * @param p
	 *            Player to check for
	 * @return true if the player should get the reward, false if not
	 */
	protected boolean shouldBeGivenOut(Player p) {
		if (p != null) {
			return rng.roll(skill.getStatus(p).getLevel());
		}
		return false;
	}

	/**
	 * @return The level required for this skill
	 */
	public int getRequiredLevel() {
		return requiredLevel;
	}

	/**
	 * @return The maximum level to be eligible for this reward
	 */
	public int getMaximumLevel() {
		return maximumLevel;
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

}
