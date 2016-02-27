package com.github.maxopoly.SkilUp.skills;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.rewards.AbstractReward;

public class Skill {
	private List<AbstractReward> rewards;
	private String skillName;
	private String lvlUpMsg;
	private int hourMultiplier;
	private HashMap<UUID, PlayerXPStatus> playerXP;
	private ItemStack itemRepresentation;

	public Skill(String skillName, List<AbstractReward> rewards,
			String lvlUpMsg, ItemStack itemRepresentation, int hourMultiplier) {
		this.skillName = skillName;
		this.rewards = rewards;
		this.lvlUpMsg = lvlUpMsg.replaceAll("%SKILL%", skillName);
		playerXP = new HashMap<UUID, PlayerXPStatus>();
		this.itemRepresentation = itemRepresentation;
		this.hourMultiplier = hourMultiplier;
	}

	/**
	 * Adds the given XP status to the tracking of players XP in memory
	 * 
	 * @param pxps
	 *            Status to add
	 */
	public void addXPStatus(PlayerXPStatus pxps) {
		playerXP.put(pxps.getPlayerUUID(), pxps);
	}

	/**
	 * Gets the XP status of a specific player for this skill
	 * 
	 * @param uuid
	 *            UUID of the player whos XP status should be gotten
	 * @return The XPStatus for the given UUID or null if no status for this
	 *         uuid exists
	 */
	public PlayerXPStatus getStatus(UUID uuid) {
		return playerXP.get(uuid);
	}

	/**
	 * Gets the XP status of a specific player for this skill
	 * 
	 * @param p
	 *            The player whos status should be retrieved
	 * @return The status of the player if it exists, null if it doesnt
	 */
	public PlayerXPStatus getStatus(Player p) {
		return getStatus(p.getUniqueId());
	}

	/**
	 * @return The name of this skill
	 */
	public String getName() {
		return skillName;
	}

	/**
	 * Gives a reward with the given index. Rewards are sorted by level,
	 * starting with the smallest. This is not safe against wrong input and will
	 * throw exceptions if you use invalid indices
	 * 
	 * @param i
	 *            index of the reward
	 * @return The reward of this skill with the given index
	 */
	public AbstractReward getReward(int i) {
		return rewards.get(i);
	}

	/**
	 * @return All the rewards for this skill
	 */
	public List<AbstractReward> getRewards() {
		return rewards;
	}

	/**
	 * Plays some cool particle effects and sends a message to the player
	 */
	public void fancyStuff(UUID uuid, int level) {
		Player p = SkilUp.getPlugin().getServer().getPlayer(uuid);
		p.sendMessage(lvlUpMsg.replace("%LEVEL%", String.valueOf(level)));
		// TODO Add particles
	}

	/**
	 * @return Which item should be used in the GUI to represent this skill
	 */
	public ItemStack getItemRepresentation() {
		return itemRepresentation;
	}

	/**
	 * Each skill may have an individual multiplier to calculate the experience
	 * needed to adjust the skills individuals balance. This value should
	 * represent how much experience is gained on average in an hour
	 * 
	 * @return hourly multiplier for this skill
	 */
	public int getHourMultiplier() {
		return hourMultiplier;
	}

	public void checkForReward() {
		// TODO
		// check whether the player possibly gets something for the level he
		// reached
	}

}
