package com.github.maxopoly.SkilUp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

import com.github.maxopoly.SkilUp.database.DataBaseManager;
import com.github.maxopoly.SkilUp.essences.EssenceTracker;

public class ConfigParser {
	private SkilUp plugin;
	private DataBaseManager dbm;
	private EssenceTracker et;
	private SkilUpManager manager;

	ConfigParser(SkilUp plugin) {
		this.plugin = plugin;
	}

	public SkilUpManager parseConfig() {
		plugin.info("Initializing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		boolean behindBungee = config.getBoolean("behind_bungee", false);
		manager = new SkilUpManager(behindBungee);
		
		// db stuff
		parseDatabase(config.getConfigurationSection("database"));
		
		// essence stuff
		parseEssences(config.getConfigurationSection("essence"));

		plugin.info("Finished parsing config and setup manager");
		return manager;
	}

	public void parseEssences(ConfigurationSection essenceSection) {
		if (essenceSection != null) {
			long checkDelay = parseTime(essenceSection.getString("scanInterval", "30s"));
			// parsetime returns # of ticks, not milliseconds. * 50 for milliseconds.
			long rewardDelay = parseTime(essenceSection.getString("delay", "30m")) * 50l;
			long minimumRest = parseTime(essenceSection.getString("restInterval", "18h")) * 50l;
			String msg = ChatColor.translateAlternateColorCodes('~', essenceSection.getString("message", 
					ChatColor.BLUE + "You got your daily reward"));
			String reward = essenceSection.getString("command");
			if (reward == null) {
				plugin.warning("Essence data was provided, but no commands given");
			}
			ConfigurationSection bonus = essenceSection.getConfigurationSection("bonus");
			String bonusMsg = ChatColor.translateAlternateColorCodes('~', bonus.getString("message", 
					ChatColor.DARK_BLUE + "Thanks for coming back! Here's a bigger daily reward."));
			Map<String, Object> bonusBuffs = bonus.getConfigurationSection("factors").getValues(false);
			
			String dropMsg = ChatColor.translateAlternateColorCodes('~', essenceSection.getString("dropMessage",
					ChatColor.YELLOW + "Your inventory was full, so the items were dropped"));
			et = new EssenceTracker(checkDelay, rewardDelay, minimumRest, reward, msg,
					bonusMsg, dropMsg, bonusBuffs);
		} else {
			plugin.info("Essence section nonexistent, skipping parsing it");
		}

	}

	public void parseDatabase(ConfigurationSection dbStuff) {
		if (dbStuff == null) {
			plugin.severe("No database credentials specified. This plugin requires a database to run!");
			return;
		}
		String host = dbStuff.getString("host");
		if (host == null) {
			plugin.severe("No host for database specified. Could not load database credentials");
			return;
		}
		int port = dbStuff.getInt("port", -1);
		if (port == -1) {
			plugin.severe("No port for database specified. Could not load database credentials");
			return;
		}
		String db = dbStuff.getString("database_name");
		if (db == null) {
			plugin.severe("No name for database specified. Could not load database credentials");
			return;
		}
		String user = dbStuff.getString("user");
		if (user == null) {
			plugin.severe("No user for database specified. Could not load database credentials");
			return;
		}
		String password = dbStuff.getString("password");
		if (password == null) {
			plugin.severe("No password for database specified. Could not load database credentials");
			return;
		}
		dbm = new DataBaseManager(host, port, db, user, password, plugin.getLogger());

	}

	private Integer integerNullCheck(ConfigurationSection cs, String option) {
		if (cs.contains(option)) {
			return cs.getInt(option);
		}
		return null;
	}

	public DataBaseManager getDBManager() {
		return dbm;
	}

	public EssenceTracker getEssenceTracker() {
		return et;
	}
}
