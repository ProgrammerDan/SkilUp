package com.github.maxopoly.Emray.skills;

public class AbstractSkill {
	private int level;
	private String skillName;
	private int currentXP;
	private int totalXPForLvlUp;
	private String playerName;

	public AbstractSkill(String skillName, int level, int currentXP, String playerName) {
		this.skillName = skillName;
		this.level = level;
		this.currentXP = currentXP;
		this.playerName = playerName;
	}

	/**
	 * Increases the players level by one and adjusts the XP needed for the next
	 * level and his current XP
	 */
	public void lvlUp() {
		shinyParticles();
		level++;
		currentXP = currentXP - totalXPForLvlUp;
		checkForReward();
		totalXPForLvlUp = recalculateXPNeeded(level);
		checkForLvlUp();
	}

	/**
	 * Checks whether the player has more xp than the amount needed to level up
	 * and if he does, the method to actually level up is called
	 */
	public void checkForLvlUp() {
		if (totalXPForLvlUp <= currentXP) {
			lvlUp();
		}
	}

	/**
	 * @return The current level of this skill
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the level of this skill, only use this for testing/op, for the
	 * actual code use the levelUp method
	 * 
	 * @param level
	 *            The new level for this skill
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return The name of the player this instance is associated with
	 */
	public String getPlayer() {
		return playerName;
	}

	/**
	 * @return The amount of XP the player has on the current level
	 */
	public int getCurrentXP() {
		return currentXP;
	}

	/**
	 * @return The total amount of XP needed to reach the next level, not taking
	 *         into account the progress the player already made on this level
	 */
	public int getTotalXPForLvlUp() {
		return totalXPForLvlUp;
	}

	/**
	 * @return The amount of XP the player needs to earn to reach the next level
	 */
	public int getXPForLvlUp() {
		return totalXPForLvlUp - currentXP;
	}

	/**
	 * @return The name of this skill
	 */
	public String getName() {
		return skillName;
	}

	/**
	 * Recalculates the amount of XP needed to reach the next the level
	 * 
	 * @param level
	 *            The players current level
	 * @return The total amount of XP needed for the next level
	 */
	public static int recalculateXPNeeded(int level) {
		// TODO
		// Hardcode a function here

		return 0;
	}

	/**
	 * Gives the player a specific amount of XP for this skill and checks
	 * whether he lvled up
	 * 
	 * @param xp
	 *            Amount of xp the player should gain
	 */
	public void giveXP(int xp) {
		currentXP += xp;
		checkForLvlUp();
	}

	/**
	 * Plays some cool particle effects
	 */
	public void shinyParticles() {
		// TODO
	}

	public void checkForReward() {
		// TODO
		// check whether the player possibly gets something for the level he
		// reached
	}

}
