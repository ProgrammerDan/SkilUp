package com.github.maxopoly.SkilUp.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.rewards.AbstractReward;

public class PlayerXPStatus {

	private int level;
	private int currentXP;
	private int totalXPForLvlUp;
	private UUID playerUUID;
	private Skill skill;
	private static final Map<UUID, PlayerXPStatus> onXPBar = new HashMap<UUID, PlayerXPStatus>();

	private static double overallMultiplier;
	private static double logarithmicMultiplier;
	private static double exponentialMultiplier;
	private static double gradientFirstStage;
	private static int equationChangeLevel;

	public static void setXPMultipliers(double overallMultiplier,
			double logarithmicMultiplier, double exponentialMultiplier,
			double gradientFirstStage, int equationChangeLevel) {
		PlayerXPStatus.overallMultiplier = overallMultiplier;
		PlayerXPStatus.logarithmicMultiplier = logarithmicMultiplier;
		PlayerXPStatus.gradientFirstStage = gradientFirstStage;
		PlayerXPStatus.exponentialMultiplier = exponentialMultiplier;
		PlayerXPStatus.equationChangeLevel = equationChangeLevel;
	}

	public PlayerXPStatus(Skill skill, UUID playerUUID, int level, int currentXP) {
		this.skill = skill;
		this.playerUUID = playerUUID;
		this.level = level;
		this.currentXP = currentXP;
		totalXPForLvlUp = recalculateXPNeeded(level);
	}

	public PlayerXPStatus(Skill skill, Player player, int level, int currentXP) {
		this(skill, player.getUniqueId(), level, currentXP);
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
	 * @return The UUID of the player this instance is associated with
	 */
	public UUID getPlayerUUID() {
		return playerUUID;
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
	public int getXPMissingForLvlUp() {
		return totalXPForLvlUp - currentXP;
	}

	/**
	 * @return The skill this XP status is modelling
	 */
	public Skill getSkill() {
		return skill;
	}

	/**
	 * @return Whether the progress of this skill is shown on the XP bar right
	 *         now
	 */
	public boolean isOnXPBar() {
		return onXPBar.get(playerUUID) == this;
	}

	/**
	 * Which xp status is currently shown on the given players bar
	 * 
	 * @param uuid
	 *            of the player
	 * @return The XP status currently shown or null if no data on the player
	 *         exists currently
	 */
	public PlayerXPStatus getOnXPBar(UUID uuid) {
		return onXPBar.get(uuid);
	}

	/**
	 * Sets whether this status is shown on the bar right now. If it is set to
	 * true the bar will also get updated
	 * 
	 * @param onXPBar
	 *            Whether this status is now shown on the players XP bar
	 */
	public static void setOnXPBar(PlayerXPStatus pxps) {
		if (onXPBar.get(pxps.getPlayerUUID()) != pxps) {
			onXPBar.put(pxps.getPlayerUUID(), pxps);
			pxps.updateXPBar();
		}
	}

	/**
	 * Updates the players the players XP bar and level to show the progress of
	 * this skill
	 */
	public void updateXPBar() {
		float progress = (float) currentXP / (float) totalXPForLvlUp;
		Player p = Bukkit.getPlayer(playerUUID);
		p.setLevel(level);
		p.setExp(progress);

	}

	/**
	 * Recalculates the amount of XP needed to reach the next the level
	 * 
	 * @param level
	 *            The players current level
	 * @return The total amount of XP needed for the next level
	 */
	public int recalculateXPNeeded(int level) {
		double result;
		if (level < equationChangeLevel) {
			result = level * gradientFirstStage;
		} else {
			result = overallMultiplier
					* (exponentialMultiplier * Math.pow(level, 1 / 3) * Math
							.log(logarithmicMultiplier * level));
		}
		return level;
	}

	/**
	 * Gives the player a specific amount of XP for this skill and checks
	 * whether he lvled up
	 * 
	 * @param xp
	 *            Amount of xp the player should gain
	 */
	public void giveXP(int xp) {
		System.out.println("Giving "+ xp);
		currentXP += xp;
		checkForLvlUp();
		updateXPBar();
	}

	/**
	 * Increases the players level by one, adjusts the XP needed for the next
	 * level and his current XP
	 */
	public void lvlUp() {
		level++;
		currentXP = currentXP - totalXPForLvlUp;
		skill.checkForReward();
		skill.fancyStuff(playerUUID, level);
		totalXPForLvlUp = recalculateXPNeeded(level);
		checkForLvlUp();
	}
}
