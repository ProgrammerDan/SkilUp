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
import com.github.maxopoly.SkilUp.skills.PlayerXPStatus;
import com.github.maxopoly.SkilUp.skills.Skill;
import com.github.maxopoly.SkilUp.tracking.AmountTrackable;
import com.github.maxopoly.SkilUp.tracking.LocationTrackable;
import com.github.maxopoly.SkilUp.tracking.Trackable;
import com.github.maxopoly.SkilUp.tracking.Tracker;

public class DataBaseManager {
	private SkilUp plugin;
	private SkilUpManager manager;
	private DataBase db;
	private Tracker tracker;

	private Map<Skill, PreparedStatement> updatePlayerDataStatements;
	private Map<Skill, PreparedStatement> loadPlayerDataStatements;
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
		updatePlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		loadPlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		for (Skill skill : manager.getSkills()) {
			PreparedStatement save = db
					.prepareStatement("update skilup" + skill.getName() + " set level = ?, xp = ? where uuid = ?;");
			updatePlayerDataStatements.put(skill, save);
			PreparedStatement load = db.prepareStatement("select * from skilup" + skill.getName() + " where uuid = ?;");
			loadPlayerDataStatements.put(skill, load);
		}
		insertEssenceData = "insert into essenceTracking (uuid,timestamp, last_login) values(?,?);";
		updateEssenceData = "update essenceTracking set timestamp = ? where uuid = ?;";
		getEssenceData = "select * from essenceTracking where uuid = ?;";
		getBlockChunkData = "select * from blockTracking where chunkid = ? and world = ?;";
	}

	public boolean isConnected() {
		if (!db.isConnected())
			db.connect();
		if (db.isConnected()) {
			loadPreparedStatements();
		}
		return db.isConnected();
	}

	public synchronized void savePlayerData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not save data for " + uuid.toString());
			return;
		}
		for (Entry<Skill, PreparedStatement> entry : updatePlayerDataStatements.entrySet()) {
			Skill s = entry.getKey();
			PreparedStatement ps = entry.getValue();
			PlayerXPStatus pxps = s.getStatus(uuid);
			try {
				ps.setInt(1, pxps.getLevel());
				ps.setInt(2, pxps.getCurrentXP());
				ps.setString(3, uuid.toString());
				ps.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void createInitialPlayerData(Skill skill, UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not initialize data for " + uuid.toString());
			return;
		}
		db.execute("insert into skilup" + skill.getName() + " (uuid,level,xp) VALUES ('" + uuid.toString() + "',0,0);");
	}

	public synchronized boolean loadPlayerData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not load data for " + uuid.toString());
			return false;
		}
		for (Entry<Skill, PreparedStatement> entry : loadPlayerDataStatements.entrySet()) {
			Skill s = entry.getKey();
			PreparedStatement ps = entry.getValue();
			try {
				ps.setString(1, uuid.toString());
				ResultSet set = ps.executeQuery();
				PlayerXPStatus pxps;
				if (!set.next()) {
					createInitialPlayerData(s, uuid);
					pxps = new PlayerXPStatus(s, uuid, 0, 0);
					plugin.info("Could not find data for skill " + s.getName() + " for " + uuid.toString()
							+ ", creating new data");
				} else {
					int level = set.getInt("level");
					int xp = set.getInt("xp");
					pxps = new PlayerXPStatus(s, uuid, level, xp);
				}
				s.addXPStatus(pxps);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public void saveChunkData(UUID world, long id, Trackable[] data) {
		plugin.info("Called db saving for " + id);
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not save data for chunk" + id + " in " + world);
		}
		for (Trackable t : data) {
			if (t.isDirty()) {
				plugin.info("Dirty db saving for " + id + "   " + t.getMaterial().toString());
				if (t instanceof LocationTrackable) {
					// Location based tracking.
					try {
						PreparedStatement addBlockLocation = db.prepareStatement(this.addBlockLocation);
						for (int s : ((LocationTrackable) t).getPositions()) {
							addBlockLocation.setInt(1, s);
							addBlockLocation.setLong(2, id);
							addBlockLocation.setString(3, world.toString());
							addBlockLocation.addBatch();
						}
						addBlockLocation.executeBatch();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						PreparedStatement removeBlockLocation = db.prepareStatement(this.removeBlockLocation);
						for (int s : ((LocationTrackable) t).getRemovedPositions()) {
							removeBlockLocation.setLong(1, id);
							removeBlockLocation.setInt(2, s);
							removeBlockLocation.setString(3, world.toString());
							removeBlockLocation.addBatch();
						}
						removeBlockLocation.executeBatch();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public Trackable[] loadChunkData(UUID world, long id) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not load data for chunk" + id + " in " + world);
			return null;
		}
		Trackable[] trackables = new Trackable[tracker.getTrackables().length];
		ResultSet locationSet = null;

		try {
			PreparedStatement getBlockChunkData = db.prepareStatement(this.getBlockChunkData);
			getBlockChunkData.setLong(1, id);
			getBlockChunkData.setString(2, world.toString());
			locationSet = getBlockChunkData.executeQuery();

			while (locationSet.next()) {
				int pos = locationSet.getInt("position");
				int y = pos / 256;
				int relPos = pos % 256;
				int z = relPos / 16;
				int x = relPos % 16;
				Location loc = new Location(Bukkit.getWorld(world), x, y, z);
				Material material = loc.getBlock().getType();
				int s = tracker.getDataIndex(material);
				if (trackables[s] == null) {
					trackables[s] = new LocationTrackable(material, new LinkedList<Integer>(), true,
							tracker.getTrackables()[s].getConfig());
					if (relPos != -1) {
						((LocationTrackable) trackables[s]).add(pos);
					}
				} else {
					if (relPos != -1) {
						((LocationTrackable) trackables[s]).add(pos);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return trackables;
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
		long res = 0;
		try(PreparedStatement getEssenceData = db.prepareStatement(this.getEssenceData)) {
			getEssenceData.setString(1, uuid.toString());
			set = getEssenceData.executeQuery();
			if (set.next()) {
				res = set.getLong("timestamp");
			}
		} catch (SQLException e) {
			SkilUp.getPlugin().getLogger().log(Level.SEVERE, "Failed communicating with database", e);
		}
		return res;
	}

	public void updateEssenceData(UUID uuid, long time) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not update essence data for " + uuid.toString() + " to "
					+ time);
			return;
		}
		try {
			PreparedStatement updateEssenceData = db.prepareStatement(this.updateEssenceData);
			updateEssenceData.setLong(1, time);
			updateEssenceData.setString(2, uuid.toString());
			updateEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
