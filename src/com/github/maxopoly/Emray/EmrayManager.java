package com.github.maxopoly.Emray;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.github.maxopoly.Emray.skills.PlayerXPStatus;
import com.github.maxopoly.Emray.skills.Skill;

public class EmrayManager {
	private HashMap<String, Skill> skills;

	public EmrayManager() {
		skills = new HashMap<String, Skill>();
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
	public void savePlayerDataToDataBase(Player p) {

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
		HashMap<String, Skill> temp = new HashMap<String, Skill>();
		for (Map.Entry <String,Skill> skillEntry : skills.entrySet()) {
			PlayerXPStatus pxps = new PlayerXPStatus(skillEntry.getValue(), p, 0, 0);
			skillEntry.getValue().addXPStatus(pxps);
		}
	}
	
	
	public Skill getSkillByName(String skillName) {
		return skills.get(skillName);
	}
	
	public void addSkill(Skill skill) {
		skills.put(skill.getName(),skill);
	}

}
