package com.github.maxopoly.SkilUp.tracking;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.maxopoly.SkilUp.database.DataBaseManager;

public class Tracker {

	private DataBaseManager db;

	private Map <String, Map<Long, Trackable[][]>> amounts;
	private Map<Material, Short> materialIndex;

	private Trackable[] exampleTrackables;
	
	public Tracker() {
		for(World w : Bukkit.getWorlds()) {
			amounts.put(w.getName(), new TreeMap<Long, Trackable[][]>());
		}
	}

	public void handleBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Trackable[] sliceData = getDataSlice(e.getBlock().getWorld().getName(), generateChunkID(b.getChunk()),
				(byte) b.getLocation().getBlockY());
		sliceData[materialIndex.get(b.getType())].handleBreak(e);
	}

	public void handlePlace(BlockBreakEvent e) {
		Block b = e.getBlock();
		Trackable[] sliceData = getDataSlice(e.getBlock().getWorld().getName(), generateChunkID(b.getChunk()),
				(byte) b.getLocation().getBlockY());
		sliceData[materialIndex.get(b.getType())].handleBreak(e);
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

	public Trackable[] getDataSlice(String world, long chunkID, byte y) {
		Map <Long, Trackable[][]> worldMap = amounts.get(world);
		Trackable[][] track = worldMap.get(chunkID);
		if (track == null) {
			track = new Trackable[256][exampleTrackables.length];
			worldMap.put(chunkID, track);
		}
		Trackable[] t = track[y];
		if (t[0] == null) {
			// not initialized
			initLayer(world, chunkID, y);
		}
		return t;
	}
	
	public void initLayer(String world, long chunkID, byte y) {
		for(int x = 0; x < 16; x++) {
			
		}
	}

	private Trackable[] getDataSliceForSaving(String world, long chunkID, byte y) {
		Map <Long, Trackable[][]> worldMap = amounts.get(world);
		Trackable[][] track = worldMap.get(chunkID);
		if (track == null) {
			// chunk not initialized
			return null;
		}
		if (track[y][0] == null) {
			// layer not initialized
			return null;
		}
		return track[y];
	}

	public void loadData(Chunk c) {
		long id = generateChunkID(c);
		Map <Long, Trackable[][]> worldMap = amounts.get(c.getWorld().getName());
		worldMap.put(id, db.loadChunkData(c.getWorld().getName(), id));
	}

	public void saveData(Chunk c) {
		long id = generateChunkID(c);
		for (byte y = 0; y <= 255; y++) {
			Trackable[] slice = getDataSliceForSaving(c.getWorld().getName(), id, (byte) y);
			if (slice != null) {
				db.saveChunkData(c.getWorld().getName(), id, slice, y);
			}
		}
	}

	public Trackable[] getTrackables() {
		return exampleTrackables;
	}
}
