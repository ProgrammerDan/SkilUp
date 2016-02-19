package com.github.maxopoly.SkilUp.rewards;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.ListenerUser;
import com.github.maxopoly.SkilUp.skills.Skill;

public abstract class AbstractReward implements ListenerUser{
	protected int requiredLevel;
	protected Skill skill;
	protected double chance;
	protected static Random rng = new Random();
	protected String info;
	protected String name;
	protected ItemStack itemRepresentation;

	public AbstractReward(Skill skill, int requiredLevel,
			double chance, String info,
			ItemStack itemRepresentation, String name) {
		this.skill = skill;
		this.requiredLevel = requiredLevel;
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
	protected boolean rollForApplying() {
		return rng.nextDouble() <= chance;
	}

	protected boolean deservesReward(Player p) {
		return p != null && skill.getStatus(p).getLevel() >= requiredLevel;
	}
	
	protected boolean shouldBeGivenOut(Player p) {
		return deservesReward(p) && rollForApplying();
	}

	/**
	 * @return The level required for this skill
	 */
	public int getRequiredLevel() {
		return requiredLevel;
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
