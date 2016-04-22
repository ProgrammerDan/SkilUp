package com.github.maxopoly.SkilUp.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

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
	private PreparedStatement insertInitialBlockAmount;
	private PreparedStatement updateBlockAmount;
	private PreparedStatement addBlockLocation;
	private PreparedStatement removeBlockLocation;
	private PreparedStatement getChunkAmountData;
	private PreparedStatement getChunkLocationData;
	private PreparedStatement insertEssenceData;
	private PreparedStatement updateEssenceData;
	private PreparedStatement getEssenceData;

	public DataBaseManager(SkilUpManager manager, Tracker tracker, String host,
			int port, String db, String user, String password, Logger logger) {
		plugin = SkilUp.getPlugin();
		this.manager = manager;
		this.tracker = tracker;
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
		// init tables for player xp tracking
		for (Skill skill : manager.getSkills()) {
			db.execute("create table if not exists skilup" + skill.getName()
					+ "(uuid varchar(255) not null,level int not null,"
					+ "xp int not null,primary key(uuid));");
		}

		// init tables for block tracking
		db.execute("create table if not exists blockTracking "
				+ "(chunkid bigint not null, world varchar(255), material varchar(255) not null,"
				+ " y smallint not null, amount smallint not null, "
				+ " primary key(world, chunkid, y, material));");
		db.execute("create table if not exists locationTracking"
				+ " (chunkid bigint not null,world varchar(255),position int not null,material varchar(255), primary key(world, chunkid, position, material));");
		// one might argue that the material doesn't have to be included in the
		// primary key, but when saving newly added blocks to the db, a block
		// might be at a location
		// where previously another tracked block was. Because no particular
		// saving order can be guaranteed, multiple entries in the same location
		// with different materials may exist during runtime. This should always
		// get resolved when saving the whole cache though.

		// init table for essence tracking
		db.execute("create table if not exists essenceTracking (uuid varchar(255) not null, timestamp bigint not null, primary key(uuid));");
	}

	public void loadPreparedStatements() {
		updatePlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		loadPlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		for (Skill skill : manager.getSkills()) {
			PreparedStatement save = db.prepareStatement("update skilup"
					+ skill.getName()
					+ " set level = ?, xp = ? where uuid = ?;");
			updatePlayerDataStatements.put(skill, save);
			PreparedStatement load = db.prepareStatement("select * from skilup"
					+ skill.getName() + " where uuid = ?;");
			loadPlayerDataStatements.put(skill, load);
		}
		updateBlockAmount = db
				.prepareStatement("update blockTracking set amount = ? where chunkid = ?, y = ?, world = ?, material = ?;");
		insertInitialBlockAmount = db
				.prepareStatement("insert into blockTracking (chunkid,material,y,amount,world) values(?,?,?,?,?);");
		removeBlockLocation = db
				.prepareStatement("remove from locationTracking"
						+ "where chunkid = ?, position = ?, world = ?;");
		addBlockLocation = db.prepareStatement("insert into locationTracking"
				+ " (chunkid,position,material,world) values(?,?,?,?);");
		insertEssenceData = db
				.prepareStatement("insert into essenceTracking (uuid,timestamp) values(?,?);");
		updateEssenceData = db
				.prepareStatement("update essenceTracking set timestamp = ? where uuid = ?;");
		getEssenceData = db
				.prepareStatement("select * from essenceTracking where uuid = ?;");

		getChunkAmountData = db
				.prepareStatement("select * from blockTracking where chunkid = ? and world = ?;");
		getChunkLocationData = db
				.prepareStatement("select * from locationTracking where chunkid = ? and world = ?;");
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
			plugin.severe("Could not connect to database, could not save data for "
					+ uuid.toString());
			return;
		}
		for (Entry<Skill, PreparedStatement> entry : updatePlayerDataStatements
				.entrySet()) {
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
			plugin.severe("Could not connect to database, could not initialize data for "
					+ uuid.toString());
			return;
		}
		db.execute("insert into skilup" + skill.getName()
				+ " (uuid,level,xp) VALUES ('" + uuid.toString() + "',0,0);");
	}

	public synchronized boolean loadPlayerData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not load data for "
					+ uuid.toString());
			return false;
		}
		for (Entry<Skill, PreparedStatement> entry : loadPlayerDataStatements
				.entrySet()) {
			Skill s = entry.getKey();
			PreparedStatement ps = entry.getValue();
			try {
				ps.setString(1, uuid.toString());
				ResultSet set = ps.executeQuery();
				PlayerXPStatus pxps;
				if (!set.next()) {
					createInitialPlayerData(s, uuid);
					pxps = new PlayerXPStatus(s, uuid, 0, 0);
					plugin.info("Could not find data for skill " + s.getName()
							+ " for " + uuid.toString() + ", creating new data");
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

	public void saveChunkData(String world, long id, Trackable[] data) {
		plugin.info("Called db saving for " + id + "   " + y);
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not save data for chunk"
					+ id + " in " + world);
			return;
		}
		for (Trackable t : data) {
			if (t.isDirty()) {
				plugin.info("Dirty db saving for " + id + "   " + y + "   "
						+ t.getMaterial().toString());
				if (t instanceof AmountTrackable) {
					if (!t.savedBefore()) {
						synchronized (insertInitialBlockAmount) {
							try {
								insertInitialBlockAmount.setLong(1, id);
								insertInitialBlockAmount.setString(2, t
										.getMaterial().toString());
								insertInitialBlockAmount.setShort(3, y);
								insertInitialBlockAmount.setShort(4,
										((AmountTrackable) t).getAmount());
								insertInitialBlockAmount.setString(5, world);
								insertInitialBlockAmount.execute();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					} else {
						synchronized (updateBlockAmount) {
							try {
								updateBlockAmount.setShort(1,
										((AmountTrackable) t).getAmount());
								updateBlockAmount.setLong(2, id);
								updateBlockAmount.setShort(3, y);
								updateBlockAmount.setString(4, world);
								updateBlockAmount.setString(5, t.getMaterial()
										.toString());
								updateBlockAmount.execute();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				} else {
					// LocationTrackable
					if (!t.savedBefore()) {
						synchronized (addBlockLocation) {
							int shiftedY = 256 * y;
							for (Short s : ((LocationTrackable) t)
									.getPositions()) {
								try {
									addBlockLocation.setLong(1, id);
									addBlockLocation.setInt(2, shiftedY + s);
									addBlockLocation.setString(3, t
											.getMaterial().toString());
									addBlockLocation.setString(4, world);
									addBlockLocation.execute();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					} else {
						synchronized (removeBlockLocation) {
							int shiftedY = 256 * y;
							for (Short s : ((LocationTrackable) t)
									.getRemovedPositions()) {
								try {
									removeBlockLocation.setLong(1, id);
									removeBlockLocation.setInt(2, shiftedY + s);
									removeBlockLocation.setString(3, world);
									removeBlockLocation.execute();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}

	public Trackable[] loadChunkData(String world, long id) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not load data for chunk"
					+ id + " in " + world);
			return null;
		}
		Trackable[][] trackables = new Trackable[256][tracker.getTrackables().length];
		ResultSet amountSet = null;
		synchronized (getChunkAmountData) {
			try {
				getChunkAmountData.setLong(1, id);
				getChunkAmountData.setString(2, world);
				amountSet = getChunkAmountData.executeQuery();
				while (amountSet.next()) {
					Material mat = Material.getMaterial(amountSet
							.getString("material"));
					short y = amountSet.getShort("y");
					short amount = amountSet.getShort("amount");
					short s = tracker.getDataIndex(mat);
					trackables[y][s] = new AmountTrackable(mat, amount, true,
							tracker.getTrackables()[s].getConfig());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		ResultSet locationSet = null;
		synchronized (getChunkLocationData) {
			try {
				getChunkLocationData.setLong(1, id);
				getChunkLocationData.setString(2, world);
				locationSet = getChunkLocationData.executeQuery();

				while (locationSet.next()) {
					Material material = Material.getMaterial(locationSet
							.getString("material"));
					int pos = locationSet.getInt("position");
					int y = pos / 256;
					int relPos = pos % 256;
					short s = tracker.getDataIndex(material);
					if (trackables[y][s] == null) {
						trackables[y][s] = new LocationTrackable(material,
								new LinkedList<Short>(), true,
								tracker.getTrackables()[s].getConfig());
						if (relPos != -1) {
							((LocationTrackable) trackables[y][s])
									.add((short) relPos);
						}
					} else {
						if (relPos != -1) {
							((LocationTrackable) trackables[y][s])
									.add((short) relPos);
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return trackables;
	}

	public synchronized void initEssenceData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not initialize essence data for "
					+ uuid.toString());
			return;
		}
		try {
			insertEssenceData.setString(1, uuid.toString());
			insertEssenceData.setLong(2, 0L);
			insertEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized long getEssenceData(UUID uuid) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not retrieve essence data for "
					+ uuid.toString());
			// deny all essences while db is dead
			return Long.MAX_VALUE;
		}
		ResultSet set = null;
		long res = 0;
		try {
			getEssenceData.setString(1, uuid.toString());
			set = getEssenceData.executeQuery();
			if (set.next()) {
				res = set.getLong("timestamp");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	public synchronized void updateEssenceData(UUID uuid, long time) {
		if (!isConnected()) {
			plugin.severe("Could not connect to database, could not update essence data for "
					+ uuid.toString() + " to " + time);
			return;
		}
		try {
			updateEssenceData.setLong(1, time);
			updateEssenceData.setString(2, uuid.toString());
			updateEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
