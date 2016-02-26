package com.github.maxopoly.SkilUp.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.SkilUpManager;
import com.github.maxopoly.SkilUp.skills.PlayerXPStatus;
import com.github.maxopoly.SkilUp.skills.Skill;

public class DataBaseManager {
	private SkilUp plugin;
	private SkilUpManager manager;
	private DataBase db;

	private Map<Skill, PreparedStatement> savePlayerDataStatements;
	private Map<Skill, PreparedStatement> loadPlayerDataStatements;

	public DataBaseManager(String host, int port, String db, String user,
			String password, Logger logger) {
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
		for (Skill skill : manager.getSkills()) {
			db.execute("create table if not exists skilup" + skill.getName()
					+ "(uuid varchar(255) not null,level int not null,"
					+ "xp int not null,primary key(uuid));");
		}
	}

	public void loadPreparedStatements() {
		savePlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		loadPlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		for (Skill skill : manager.getSkills()) {
			PreparedStatement save = db.prepareStatement("insert into skilup"
					+ skill.getName() + "(uuid, level, xp) values(?,?,?);");
			savePlayerDataStatements.put(skill, save);
			PreparedStatement load = db.prepareStatement("select * from skilup"
					+ skill.getName() + " where id = ?;");
			loadPlayerDataStatements.put(skill, load);
		}
	}

	public boolean isConnected() {
		if (!db.isConnected())
			db.connect();
		if (db.isConnected()) {
			loadPreparedStatements();
		}
		return db.isConnected();
	}

	public void savePlayerData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not save data for "
					+ uuid.toString());
		}
		for (Entry<Skill, PreparedStatement> entry : savePlayerDataStatements
				.entrySet()) {
			Skill s = entry.getKey();
			PreparedStatement ps = entry.getValue();
			PlayerXPStatus pxps = s.getStatus(uuid);
			try {
				ps.setString(1, uuid.toString());
				ps.setInt(2, pxps.getLevel());
				ps.setInt(3, pxps.getCurrentXP());
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					ps.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	public boolean loadPlayerData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not load data for "
					+ uuid.toString());
			return false;
		}
		for (Entry<Skill, PreparedStatement> entry : savePlayerDataStatements
				.entrySet()) {
			Skill s = entry.getKey();
			PreparedStatement ps = entry.getValue();
			try {
				ps.setString(1, uuid.toString());
				ResultSet set = ps.executeQuery();
				if (!set.next()) {
					//no data on player, he is new
					return false;
				}
				int level = set.getInt("level");
				int xp = set.getInt("xp");
				PlayerXPStatus pxps = new PlayerXPStatus(s, uuid, level, xp);
				s.addXPStatus(pxps);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					ps.close();
				} catch (Exception ex) {
				}
			}
		}
		return true;
	}

}
