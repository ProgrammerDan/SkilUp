package com.github.maxopoly.SkilUp.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.SkilUpManager;

public class DataBaseManager {
	private SkilUp plugin;
	private SkilUpManager manager;
	private DataBase db;

	private String insertEssenceData;
	private String updateEssenceLogin;
	private String updateEssenceGiven;
	private String getEssenceData;

	public DataBaseManager(SkilUpManager manager, String host, int port, String db, String user,
			String password, Logger logger) {
		plugin = SkilUp.getPlugin();
		this.manager = manager;
		this.db = new DataBase(host, port, db, user, password, logger);
		if (!this.db.connect()) {
			logger.severe("Could not connect to database");
			return;
		}
		prepareTables();
		loadPreparedStatements();
	}

	/**
	 * Creates a table for each skill if it doesnt already exist
	 */
	public void prepareTables() {
		db.execute("create table if not exists essenceTracking (uuid varchar(255) not null, last_login bigint not null, last_gift bigint not null, primary key(uuid))");
	}

	public void loadPreparedStatements() {
		insertEssenceData = "insert into essenceTracking (uuid,timestamp, last_login, last_gift) values(?,?,?)";
		updateEssenceLogin = "update essenceTracking set last_login = ? where uuid = ?";
		updateEssenceGiven = "update essenceTracking set last_login = ? where uuid = ?";
		getEssenceData = "select * from essenceTracking where uuid = ?";
	}

	public boolean isConnected() {
		if (!db.isConnected())
			db.connect();
		if (db.isConnected()) {
			loadPreparedStatements();
		}
		return db.isConnected();
	}

	public void initEssenceData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not initialize essence data for " + uuid.toString());
			return;
		}
		try {
			PreparedStatement insertEssenceData = db.prepareStatement(this.insertEssenceData);
			insertEssenceData.setString(1, uuid.toString());
			insertEssenceData.setLong(2, 0L);
			insertEssenceData.setLong(3, 0L);
			insertEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public long[] getEssenceData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not retrieve essence data for " + uuid.toString());
			// deny all essences while db is dead
			return new long[]{Long.MAX_VALUE, Long.MAX_VALUE};
		}
		ResultSet set = null;
		long[] res = new long[2];
		try(PreparedStatement getEssenceData = db.prepareStatement(this.getEssenceData)) {
			getEssenceData.setString(1, uuid.toString());
			set = getEssenceData.executeQuery();
			if (set.next()) {
				res[0] = set.getLong("last_login");
				res[1] = set.getLong("last_gift");
			}
		} catch (SQLException e) {
			SkilUp.getPlugin().getLogger().log(Level.SEVERE, "Failed communicating with database", e);
		}
		return res;
	}

	public void updateEssenceLogin(UUID uuid, long time) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not update essence login data for " + uuid.toString() + " to "
					+ time);
			return;
		}
		try {
			PreparedStatement updateEssenceData = db.prepareStatement(this.updateEssenceLogin);
			updateEssenceData.setLong(1, time);
			updateEssenceData.setString(2, uuid.toString());
			updateEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateEssenceGiven(UUID uuid, long time) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not update essence give data for " + uuid.toString() + " to "
					+ time);
			return;
		}
		try {
			PreparedStatement updateEssenceData = db.prepareStatement(this.updateEssenceGiven);
			updateEssenceData.setLong(1, time);
			updateEssenceData.setString(2, uuid.toString());
			updateEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
