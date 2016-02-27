package com.github.maxopoly.SkilUp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.github.maxopoly.SkilUp.skills.PlayerXPStatus;
import com.github.maxopoly.SkilUp.skills.Skill;

public class SkilUpManager {
	private HashMap<String, Skill> skills;
	private boolean useXPBar;
	private SkilUp plugin;

	public SkilUpManager(boolean useXPBar) {
		skills = new HashMap<String, Skill>();
		plugin = SkilUp.getPlugin();
	}

	/**
	 * Saves the data of all players
	 */
	public void saveAllToDataBase() {
		for (Player p : SkilUp.getPlugin().getServer().getOnlinePlayers()) {
			savePlayerDataToDataBase(p);
		}
	}

	/**
	 * Saves the data of a specific player to the data base
	 * 
	 * @param playerName
	 *            The player whos data is saved
	 */
	public void savePlayerDataToDataBase(Player p) {
		savePlayerDataToDataBase(p.getUniqueId());
	}

	/**
	 * Saves the data of a specific player to the data base
	 * 
	 * @param playerName
	 *            UUID of the player whos data is saved
	 */
	public void savePlayerDataToDataBase(UUID uuid) {
		plugin.getDataBaseManager().savePlayerData(uuid);
	}

	/**
	 * Loads the data of a specific player from the database into memory
	 * 
	 * @param playerName
	 *            The players name whos data should be loaded
	 * @return true if the data was found and loaded, false if no data was found
	 */
	public boolean loadPlayerDataFromDataBase(Player p) {
		return loadPlayerDataFromDataBase(p.getUniqueId());
	}

	/**
	 * Loads the data of a specific player from the database into memory
	 * 
	 * @param uuid
	 *            The players uuid whos data should be loaded
	 * @return true if the data was found and loaded, false if no data was found
	 */
	public boolean loadPlayerDataFromDataBase(UUID uuid) {
		return plugin.getDataBaseManager().loadPlayerData(uuid);
	}

	/**
	 * This is called when a player logs in for the first, it initializes all
	 * skills for the player at level 0 and puts the data into memory
	 * 
	 * @param p
	 *            The name of the player who joined for the first time
	 */
	public void playerFirstLogin(Player p) {
	}

	public Skill getSkillByName(String skillName) {
		return skills.get(skillName.toLowerCase());
	}

	public void addSkill(Skill skill) {
		skills.put(skill.getName().toLowerCase(), skill);
	}

	public Collection<Skill> getSkills() {
		return skills.values();
	}

	public boolean useXPBar() {
		return useXPBar;
	}

}
