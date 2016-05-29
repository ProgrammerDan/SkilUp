package com.github.maxopoly.SkilUp.tracking;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.maxopoly.SkilUp.database.DataBaseManager;

public class Tracker {

	private DataBaseManager db;
	private ChunkGarbageCollector gc;

	private Map<UUID, Map<Long, Trackable[]>> amounts;
	private Map<Material, Integer> materialIndex;
	private Trackable[] exampleTrackables;

	public Tracker(long savingTime, long checkIntervall) {
		amounts = new TreeMap<UUID, Map<Long, Trackable[]>>();
		materialIndex = new TreeMap<Material, Integer>();
		exampleTrackables = new Trackable[0];
		for (World w : Bukkit.getWorlds()) {
			// init all worlds
			amounts.put(w.getUID(), new TreeMap<Long, Trackable[]>());
		}
		gc = new ChunkGarbageCollector(this, savingTime, checkIntervall);
	}

	public void handleLoad(Chunk c) {
		long id = generateChunkID(c);
		if (amounts.get(c.getWorld().getUID()).get(id) == null) {
			loadData(id, c.getWorld().getUID());
		} else {
			// chunk is cached and was loaded again, update garbage collector
			gc.removeChunk(id, c.getWorld().getUID());
		}
	}

	public void handleUnload(Chunk c) {
		// chunk was unloaded, we keep it cached, but add it to the garbage
		// collector
		gc.addChunk(generateChunkID(c), c.getWorld().getUID());
	}

	public void handleBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Trackable track = getTrackable(b);
		if (track != null) {
			track.handleBreak(e);
		}
	}

	public void handlePlace(BlockPlaceEvent e) {
		Block b = e.getBlock();
		Trackable track = getTrackable(b);
		if (track != null) {
			track.handlePlace(e);
		}
	}

	private static long generateChunkID(Chunk c) {
		long res = c.getX();
		res = res << 32;
		res += (long) c.getZ();
		return res;
	}

	public Integer getDataIndex(Material m) {
		return materialIndex.get(m);
	}

	public Trackable[] getChunkData(UUID world, long chunkID) {
		Map<Long, Trackable[]> worldMap = amounts.get(world);
		Trackable[] track = worldMap.get(chunkID);
		if (track == null) {
			track = new Trackable[exampleTrackables.length];
			worldMap.put(chunkID, track);
		}
		if (track[0] == null) {
			// not initialized
			initChunk(world, chunkID, track);
		}
		return track;
	}

	public Trackable getTrackable(Block b) {
		Integer dataIndex = getDataIndex(b.getType());
		if (dataIndex == null) {
			return null;
		}
		return getChunkData(b.getWorld().getUID(),
				generateChunkID(b.getChunk()))[dataIndex];
	}

	public void initChunk(UUID world, long chunkID, Trackable[] trackables) {
		World w = Bukkit.getWorld(world);
		for (int i = 0; i < exampleTrackables.length; i++) {
			trackables[i] = exampleTrackables[i].clone();
		}
		Chunk c = w.getChunkAt((int) (chunkID >> 32), (int) chunkID);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					Block b = c.getBlock(x, y, z);
					Integer slot = getDataIndex(b.getType());
					if (slot != null) {
						trackables[slot].addLocation(b.getLocation());
					}
				}
			}
		}
	}

	public void loadData(long id, UUID world) {
		Map<Long, Trackable[]> worldMap = amounts.get(world);
		worldMap.put(id, db.loadChunkData(world, id));
	}

	public void saveDataAndRemoveFromCache(long id, UUID world) {
		Map<Long, Trackable[]> worldMap = amounts.get(world);
		Trackable[] track = worldMap.get(id);
		if (track == null) {
			// chunk not initialized
			return;
		}
		db.saveChunkData(world, id, track);
		worldMap.remove(id);
	}

	public void saveAll() {
		for (Entry<UUID, Map<Long, Trackable[]>> entry : amounts.entrySet()) {
			for (Entry<Long, Trackable[]> deepEntry : entry.getValue()
					.entrySet()) {
				db.saveChunkData(entry.getKey(), deepEntry.getKey(),
						deepEntry.getValue());
			}
		}
	}

	public Trackable[] getTrackables() {
		return exampleTrackables;
	}

	public void registerTrackable(Trackable t) {
		Trackable[] exampleReplacement = new Trackable[exampleTrackables.length + 1];
		// copy old ones over
		for (int i = 0; i < exampleTrackables.length; i++) {
			exampleReplacement[i] = exampleTrackables[i];
		}
		exampleReplacement[exampleTrackables.length] = t;
		materialIndex.put(t.getMaterial(), exampleTrackables.length);
		exampleTrackables = exampleReplacement;
	}

	public void setDataBase(DataBaseManager dbm) {
		this.db = dbm;
	}
}
