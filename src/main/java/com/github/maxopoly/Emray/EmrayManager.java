package com.github.maxopoly.Emray;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.entity.Player;

import com.github.maxopoly.Emray.skills.AbstractSkill;

public class EmrayManager {
	private HashMap<String, HashMap<String, AbstractSkill>> skills;
	private Emray plugin;
	private LinkedList<String> skillNames;

	public EmrayManager(Emray plugin, LinkedList<String> skillNames) {
		this.plugin = plugin;
		skills = new HashMap<String, HashMap<String, AbstractSkill>>();
		this.skillNames = skillNames;
	}

	/**
	 * Saves the data of all players
	 */
	public void saveAllToDataBase() {
		// TODO
	}

	/**
	 * Saves the data of a specific player to the data base
	 * 
	 * @param playerName
	 *            The player whos data is saved
	 */
	public void savePlayerDataToDataBase(String playerName) {

	}

	/**
	 * Loads the data of a specific player from the database into memory
	 * 
	 * @param playerName
	 *            The players name whos data should be loaded
	 * @return true if the data was found and loaded, false if no data was found
	 */
	public boolean loadPlayerDataFromDataBase(String playerName) {
		// TODO
		return true;
	}

	/**
	 * This is called when a player logs in for the first, it initializes all
	 * skills for the player at level 0 and puts the data into memory
	 * 
	 * @param p
	 *            The name of the player who joined for the first time
	 */
	public void playerFirstLogin(Player p) {
		HashMap<String, AbstractSkill> temp = new HashMap<String, AbstractSkill>();
		for (String skillName : skillNames) {
			AbstractSkill as = new AbstractSkill(skillName, 0, 0, p.getName());
			temp.put(skillName, as);
		}
		skills.put(p.getName(), temp);
	}

	/**
	 * Gets a map of all skills of player
	 * 
	 * @param player
	 *            The name of the player
	 * @return HashMap containing all the skills of the player
	 */
	public HashMap<String, AbstractSkill> getSkills(String player) {
		return skills.get(player);
	}

	/**
	 * Gets a specific skill of a player
	 * 
	 * @param player
	 *            The name of the player
	 * @param skillName
	 *            The name of the wanted skill
	 * @return The skill or null if no data on the player exists or no data for
	 *         the given skill name
	 */
	public AbstractSkill getSkillByName(Player player, String skillName) {
		HashMap<String, AbstractSkill> playersSkills = skills.get(player);
		if (playersSkills == null) {
			return null;
		}
		return playersSkills.get(skillName);
	}
}
