package com.github.maxopoly.SkilUp;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.nio.file.Files;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import com.github.maxopoly.SkilUp.database.DataBaseManager;

public class SkilUpBungee extends Plugin implements Listener {

	private Configuration config;
	private DataBaseManager dbm;

	@Override
	public void onEnable() {
		getLogger().info("Getting SkilUpBungee configuration");
		this.config = loadConfig();

		if (this.config != null) {
			getLogger().info("Setting up Database for SkilUpBungee");
			this.dbm = configDatabase(config.getSection("database"));

			getLogger().info("Setting up SkilUpBungee listeners");
			getProxy().getPluginManager().registerListener(this, this);
		} else {
			getLogger().severe("Config not found, SkilUpBungee going dark.");
		}
	}

	private Configuration loadConfig() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");

		if (!file.exists()) {
			getLogger().info("Setting up SkilUpBungee default configuration");
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, file.toPath());
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Failed to save SkilUpBungee default config", e);
			}
		}

		try {
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException ioe) {
			getLogger().log(Level.SEVERE, "Failed to load SkilUpBungee config", ioe);
			return null;
		}
	}

	public DataBaseManager configDatabase(Configuration dbStuff) {
		if (dbStuff == null) {
			getLogger().severe("No database credentials specified. This plugin requires a database to run!");
			return null;
		}
		String host = dbStuff.getString("host");
		if (host == null) {
			getLogger().severe("No host for database specified. Could not load database credentials");
			return null;
		}
		int port = dbStuff.getInt("port", -1);
		if (port == -1) {
			getLogger().severe("No port for database specified. Could not load database credentials");
			return null;
		}
		String db = dbStuff.getString("database_name");
		if (db == null) {
			getLogger().severe("No name for database specified. Could not load database credentials");
			return null;
		}
		String user = dbStuff.getString("user");
		if (user == null) {
			getLogger().severe("No user for database specified. Could not load database credentials");
			return null;
		}
		String password = dbStuff.getString("password");
		if (password == null) {
			getLogger().warning("No password for database specified.");
		}
		return new DataBaseManager(host, port, db, user, password, getLogger());
	}

	@EventHandler
	public void afterLogin(PostLoginEvent event) {
		if (dbm == null) {
			getLogger().severe("Login occurred but no database configured. Skipping.");
			return;
		}

		ProxiedPlayer player = event.getPlayer();
		if (dbm.getEssenceData(player.getUniqueId())[0] == 0) {
			handleFirstLogin(player);
		} else {
			handleLogin(player);
		}
	}

	public void handleFirstLogin(ProxiedPlayer p) {
		dbm.initEssenceData(p.getUniqueId());
		handleLogin(p);
	}
	
	public void handleLogin(ProxiedPlayer p) {
		dbm.updateEssenceLogin(p.getUniqueId(), System.currentTimeMillis());
	}
}
