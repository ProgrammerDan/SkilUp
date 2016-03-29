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
	private Map<Trackable, PreparedStatement> removeBlockDataStatements;
	private Map<Trackable, PreparedStatement> insertBlockDataStatements;
	private PreparedStatement getChunkAmountData;
	private PreparedStatement getChunkLocationData;
	private PreparedStatement insertEssenceData;
	private PreparedStatement updateEssenceData;
	private PreparedStatement getEssenceData;

	public DataBaseManager(SkilUpManager manager, Tracker tracker, String host, int port,
			String db, String user, String password, Logger logger) {
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
				+ " (chunkid bigint not null,world varchar(255),position int not null,material varchar(255), primary key(chunkid, position));");

		// init table for essence tracking
		db.execute("create table if not exists essenceTracking (uuid varchar(255) not null, timestamp bigint not null, primary key(uuid));");
	}

	public void loadPreparedStatements() {
		updatePlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		loadPlayerDataStatements = new HashMap<Skill, PreparedStatement>();
		removeBlockDataStatements = new HashMap<Trackable, PreparedStatement>();
		insertBlockDataStatements = new HashMap<Trackable, PreparedStatement>();
		for (Skill skill : manager.getSkills()) {
			PreparedStatement save = db.prepareStatement("update skilup"
					+ skill.getName()
					+ " set level = ?, xp = ? where uuid = ?;");
			updatePlayerDataStatements.put(skill, save);
			PreparedStatement load = db.prepareStatement("select * from skilup"
					+ skill.getName() + " where uuid = ?;");
			loadPlayerDataStatements.put(skill, load);
		}

		for (Trackable t : tracker.getTrackables()) {
			PreparedStatement del = null;
			PreparedStatement insert = null;
			if (t instanceof AmountTrackable) {
				del = db.prepareStatement("update blockTracking set amount = ? where chunkid = ?, y = ?, world = ?, material = "
						+ t.getMaterial() + ";");
				insert = db
						.prepareStatement("insert into blockTracking (chunkid,material,y,amount,world) values(?,"
								+ t.getMaterial() + ",?,?,?);");
			} else if (t instanceof LocationTrackable) {
				del = db.prepareStatement("remove from locationTracking"
						+ "where chunkid = ?, position = ?, world = ?;");
				insert = db
						.prepareStatement("insert into locationTracking"
								+ t.getMaterial()
								+ " (chunkid,position,material,world) values(?,?,?,?);");
			}
			removeBlockDataStatements.put(t, del);
			insertBlockDataStatements.put(t, insert);
		}
		insertEssenceData = db
				.prepareStatement("insert into essenceTracking (uuid,timestamp) values(?,?);");
		updateEssenceData = db
				.prepareStatement("update essenceTracking set timestamp = ? where uuid = ?;");
		getEssenceData = db
				.prepareStatement("select * from essenceTracking where uuid = ?;");

		getChunkAmountData = db
				.prepareStatement("select * from blockTracking where chunkid = ?,world = ?;");
		getChunkLocationData = db
				.prepareStatement("select * from locationTracking where chunkid = ?,world = ?;");
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
			} finally {
				try {
					ps.close();
				} catch (Exception ex) {
				}
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
			} finally {
				try {
					ps.close();
				} catch (Exception ex) {
				}
			}
		}
		return true;
	}

	public void saveChunkData(String world, long id, Trackable[] data, short y) {
		for (Trackable t : data) {
			if (t.isDirty()) {
				if (t instanceof AmountTrackable) {
					if (!t.savedBefore()) {
						PreparedStatement ins = insertBlockDataStatements
								.get(t);
						synchronized (ins) {
							try {
								ins.setLong(1, id);
								ins.setShort(2, y);
								ins.setShort(3,
										((AmountTrackable) t).getAmount());
								ins.setString(4, world);
							} catch (SQLException e) {
								e.printStackTrace();
							} finally {
								try {
									ins.close();
								} catch (Exception ex) {
								}
							}
						}
					} else {
						PreparedStatement update = removeBlockDataStatements
								.get(t);
						synchronized (update) {
							try {
								update.setShort(1,
										((AmountTrackable) t).getAmount());
								update.setLong(2, id);
								update.setShort(3, y);
								update.setString(4, world);
							} catch (SQLException e) {
								e.printStackTrace();
							} finally {
								try {
									update.close();
								} catch (Exception ex) {
								}
							}
						}
					}
				} else {
					// LocationTrackable
					if (!t.savedBefore()) {
						PreparedStatement ins = insertBlockDataStatements
								.get(t);
						synchronized (ins) {
							int shiftedY = y << 16;
							for (Short s : ((LocationTrackable) t)
									.getPositions()) {
								try {
									ins.setLong(1, id);
									ins.setInt(2, shiftedY + s);
									ins.setString(3, t.getMaterial().toString());
									ins.setString(4, world);
								} catch (SQLException e) {
									e.printStackTrace();
								} finally {
									try {
										ins.close();
									} catch (Exception ex) {
									}
								}
							}
						}
					} else {
						PreparedStatement del = removeBlockDataStatements
								.get(t);
						synchronized (del) {
							int shiftedY = y << 16;
							for (Short s : ((LocationTrackable) t)
									.getRemovedPositions()) {
								try {
									del.setLong(1, id);
									del.setInt(2, shiftedY + s);
									del.setString(3, world);
								} catch (SQLException e) {
									e.printStackTrace();
								} finally {
									try {
										del.close();
									} catch (Exception ex) {
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public Trackable[][] loadChunkData(String world, long id) {
		Trackable[][] trackables = new Trackable[255][tracker.getTrackables().length];
		ResultSet set = null;
		synchronized (getChunkAmountData) {
			try {
				getChunkAmountData.setLong(1, id);
				getChunkAmountData.setString(2, world);
				set = getChunkAmountData.executeQuery();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					getChunkAmountData.close();
				} catch (Exception ex) {
				}
			}
		}
		try {
			while (set.next()) {
				Material mat = Material.getMaterial(set.getString("material"));
				short y = set.getShort("y");
				short amount = set.getShort("amount");
				short s = tracker.getDataIndex(mat);
				trackables[y][s] = new AmountTrackable(mat, amount, true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				set.close();
			} catch (Exception ex) {
			}
		}
		synchronized (getChunkLocationData) {
			try {
				getChunkLocationData.setLong(1, id);
				getChunkLocationData.setString(2, world);
				set = getChunkAmountData.executeQuery();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					getChunkAmountData.close();
				} catch (Exception ex) {
				}
			}
		}
		try {
			while (set.next()) {
				Material material = Material.getMaterial(set
						.getString("material"));
				int pos = set.getInt("position");
				short relPos = (short) pos;
				short y = (short) (pos >> 16);
				short s = tracker.getDataIndex(material);
				if (trackables[y][s] == null) {
					trackables[y][s] = new LocationTrackable(material,
							new LinkedList<Short>(), true);
					if (relPos != -1) {
						((LocationTrackable) trackables[y][s]).add(relPos);
					}
				} else {
					if (relPos != -1) {
						((LocationTrackable) trackables[y][s]).add(relPos);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				set.close();
			} catch (Exception ex) {
			}
		}
		return trackables;
	}

	public synchronized void initEssenceData(UUID uuid) {
		try {
			insertEssenceData.setString(1, uuid.toString());
			insertEssenceData.setLong(2, 0L);
			insertEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				insertEssenceData.close();
			} catch (Exception ex) {
			}
		}
	}

	public synchronized long getEssenceData(UUID uuid) {
		ResultSet set = null;
		long res = 0;
		try {
			getEssenceData.setString(1, uuid.toString());
			set = getEssenceData.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				getEssenceData.close();
			} catch (Exception ex) {
			}
		}
		try {
			if (set.next()) {
				res = set.getLong("timestamp");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				set.close();
			} catch (Exception ex) {
			}
		}
		return res;
	}

	public synchronized void updateEssenceData(UUID uuid, long time) {
		try {
			updateEssenceData.setLong(1, time);
			updateEssenceData.setString(2, uuid.toString());
			updateEssenceData.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				updateEssenceData.close();
			} catch (Exception ex) {
			}
		}
	}
}
