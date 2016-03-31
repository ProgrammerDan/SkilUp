package com.github.maxopoly.SkilUp.tracking;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.maxopoly.SkilUp.database.DataBaseManager;

public class Tracker {

	private DataBaseManager db;
	private ChunkGarbageCollector gc;

	private Map<String, Map<Long, Trackable[][]>> amounts;
	private Map<Material, Short> materialIndex;
	private Trackable[] exampleTrackables;

	public Tracker(long savingTime, long checkIntervall) {
		amounts = new TreeMap<String, Map<Long, Trackable[][]>>();
		materialIndex = new TreeMap<Material, Short>();
		exampleTrackables = new Trackable[0];
		for (World w : Bukkit.getWorlds()) {
			// init all worlds
			amounts.put(w.getName(), new TreeMap<Long, Trackable[][]>());
		}
		gc = new ChunkGarbageCollector(this, savingTime, checkIntervall);
	}

	public void handleLoad(Chunk c) {
		long id = generateChunkID(c);
		if (amounts.get(c.getWorld().getName()).get(id) == null) {
			loadData(id, c.getWorld().getName());
		} else {
			// chunk is cached and was loaded again, update garbage collector
			gc.removeChunk(id, c.getWorld().getName());
		}
	}

	public void handleUnload(Chunk c) {
		// chunk was unloaded, we keep it cached, but add it to the garbage
		// collector
		gc.addChunk(generateChunkID(c), c.getWorld().getName());
	}

	public void handleBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Trackable[] sliceData = getDataSlice(e.getBlock().getWorld().getName(),
				generateChunkID(b.getChunk()), (short) b.getLocation()
						.getBlockY());
		sliceData[materialIndex.get(b.getType())].handleBreak(e);
	}

	public void handlePlace(BlockPlaceEvent e) {
		Block b = e.getBlock();
		Trackable[] sliceData = getDataSlice(e.getBlock().getWorld().getName(),
				generateChunkID(b.getChunk()), (short) b.getLocation()
						.getBlockY());
		sliceData[materialIndex.get(b.getType())].handlePlace(e);
	}

	private long generateChunkID(Chunk c) {
		long res = c.getX();
		res <<= 32;
		res += c.getZ();
		return res;
	}

	public Short getDataIndex(Material m) {
		return materialIndex.get(m);
	}

	public Trackable[] getDataSlice(String world, long chunkID, short y) {
		Map<Long, Trackable[][]> worldMap = amounts.get(world);
		Trackable[][] track = worldMap.get(chunkID);
		if (track == null) {
			track = new Trackable[256][exampleTrackables.length];
			worldMap.put(chunkID, track);
		}
		Trackable[] t = track[y];
		if (t[0] == null) {
			// not initialized
			initLayer(world, chunkID, y, t);
		}
		return t;
	}

	public void initLayer(String world, long chunkID, short y,
			Trackable[] trackables) {
		World w = Bukkit.getWorld(world);
		for (int i = 0; i < exampleTrackables.length; i++) {
			trackables[i] = exampleTrackables[i].clone();
		}
		Chunk c = w.getChunkAt((int) (chunkID >> 32), (int) chunkID);
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Block b = c.getBlock(x, y, z);
				Short slot = getDataIndex(b.getType());
				if (slot != null) {
					trackables[slot].addLocation(b.getLocation());
				}
			}
		}
	}

	public void loadData(long id, String world) {
		Map<Long, Trackable[][]> worldMap = amounts.get(world);
		worldMap.put(id, db.loadChunkData(world, id));
	}

	public void saveDataAndRemoveFromCache(long id, String world) {
		Map<Long, Trackable[][]> worldMap = amounts.get(world);
		Trackable[][] track = worldMap.get(id);
		if (track == null) {
			// chunk not initialized
			return;
		}
		for (short y = 0; y <= 255; y++) {
			if (track[y][0] == null) {
				// layer not initialized
				continue;
			}
			db.saveChunkData(world, id, track[y], y);
		}
		worldMap.remove(id);
	}

	public void saveAll() {
		for (Entry<String, Map<Long, Trackable[][]>> entry : amounts.entrySet()) {
			for (Entry<Long, Trackable[][]> deepEntry : entry.getValue()
					.entrySet()) {
				for (short y = 0; y <= 255; y++) {
					if (deepEntry.getValue()[y][0] == null) {
						// layer not initialized
						continue;
					}
					db.saveChunkData(entry.getKey(), deepEntry.getKey(), deepEntry.getValue()[y], y);
				}
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
		materialIndex.put(t.getMaterial(), (short) exampleTrackables.length);
		exampleTrackables = exampleReplacement;
	}

	public void setDataBase(DataBaseManager dbm) {
		this.db = dbm;
	}
}
