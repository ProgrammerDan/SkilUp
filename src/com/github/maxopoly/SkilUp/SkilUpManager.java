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

	public SkilUpManager() {
		skills = new HashMap<String, Skill>();
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
		//TODO
		for (Skill skill : skills.values()) { //for every skill
			String skillname = skill.getName(); // save this
			UUID uuid = p.getUniqueId(); // save this
			PlayerXPStatus pxps = skill.getStatus(p);
			int level = pxps.getLevel(); // save this
			int currrentXP = pxps.getCurrentXP(); // save this
		}

	}

	/**
	 * Loads the data of a specific player from the database into memory
	 * 
	 * @param playerName
	 *            The players name whos data should be loaded
	 * @return true if the data was found and loaded, false if no data was found
	 */
	public boolean loadPlayerDataFromDataBase(Player p) {
		// TODO
		// for( ) //iterate over skills pulled from db
		String skillName = null; // TODO
		Skill skill = getSkillByName(skillName);
		UUID playerUUID = null; // TODO
		int level = 0; // TODO
		int currentXP = 0; // TODO
		PlayerXPStatus pxps = new PlayerXPStatus(skill, playerUUID, level,
				currentXP);
		skill.addXPStatus(pxps);
		return true; // return false if data was not found
	}

	/**
	 * This is called when a player logs in for the first, it initializes all
	 * skills for the player at level 0 and puts the data into memory
	 * 
	 * @param p
	 *            The name of the player who joined for the first time
	 */
	public void playerFirstLogin(Player p) {
		HashMap<String, Skill> temp = new HashMap<String, Skill>();
		for (Map.Entry<String, Skill> skillEntry : skills.entrySet()) {
			PlayerXPStatus pxps = new PlayerXPStatus(skillEntry.getValue(), p,
					0, 0);
			skillEntry.getValue().addXPStatus(pxps);
		}
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

}
