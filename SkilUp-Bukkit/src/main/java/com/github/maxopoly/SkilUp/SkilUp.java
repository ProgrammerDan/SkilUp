package com.github.maxopoly.SkilUp;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import vg.civcraft.mc.civmodcore.ACivMod;

import com.github.maxopoly.SkilUp.database.DataBaseManager;
import com.github.maxopoly.SkilUp.essences.EssenceTracker;
import com.github.maxopoly.SkilUp.listeners.misc.LoginLogoutListener;

public class SkilUp extends ACivMod {
	private static SkilUp plugin;
	private static SkilUpManager manager;
	private static DataBaseManager dbm;
	private static EssenceTracker essenceTracker;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		ConfigParser cp = new ConfigParser(this);
		manager = cp.parseConfig();
		dbm = cp.getDBManager();
		essenceTracker = cp.getEssenceTracker();
		registerListener();
	}

	@Override
	public void onDisable() {
	}

	protected String getPluginName() {
		return "SkilUp-light";
	}
	
	public void registerListener() {
		Bukkit.getPluginManager().registerEvents(new LoginLogoutListener(), this);
	}

	public static SkilUpManager getManager() {
		return manager;
	}

	public static SkilUp getPlugin() {
		return plugin;
	}
	
	public static DataBaseManager getDataBaseManager() {
		return dbm;
	}
	
	public static EssenceTracker getEssenceTracker() {
		return essenceTracker;
	}
	
}
