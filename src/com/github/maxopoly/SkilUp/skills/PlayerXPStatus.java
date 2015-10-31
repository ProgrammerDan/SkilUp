package com.github.maxopoly.SkilUp.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.rewards.AbstractReward;

public class PlayerXPStatus {

	private int level;
	private int currentXP;
	private int totalXPForLvlUp;
	private UUID playerUUID;
	private Skill skill;
	private Boolean[] rewards;
	private static final Map<UUID, PlayerXPStatus> onXPBar = new HashMap<UUID, PlayerXPStatus>();
	private Player player;

	public PlayerXPStatus(Skill skill, UUID playerUUID, int level, int currentXP) {
		this.skill = skill;
		this.playerUUID = playerUUID;
		this.player = SkilUp.getPlugin().getServer().getPlayer(playerUUID);
		this.level = level;
		this.currentXP = currentXP;
		totalXPForLvlUp = recalculateXPNeeded(level);
		rewards = calculateRewards();
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
	 * @return The player this instance is associated with
	 */
	public Player getPlayer() {
		return player;
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
		player.setLevel(level);
		player.setExp(progress);

	}

	/**
	 * Recalculates the amount of XP needed to reach the next the level
	 * 
	 * @param level
	 *            The players current level
	 * @return The total amount of XP needed for the next level
	 */
	public int recalculateXPNeeded(int level) {
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
	 * Recalculates which rewards the player deserves
	 * 
	 * @return Whether the player deserves each reward or not, the order is the
	 *         same as in the skill's reward order, so this is consistent with
	 *         the indices used
	 * 
	 * 
	 */

	public Boolean[] calculateRewards() {
		Boolean[] reward = new Boolean[skill.getRewards().length];
		int i = 0;
		for (AbstractReward rew : skill.getRewards()) {
			if (rew.getRequiredLevel() <= level) {
				reward[i] = true;
			} else {
				reward[i] = false;
			}
		}
		return reward;
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

	public boolean deservesReward(int i) {
		return rewards[i];
	}

}
