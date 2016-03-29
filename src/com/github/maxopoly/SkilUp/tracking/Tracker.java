package com.github.maxopoly.SkilUp.tracking;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.database.DataBaseManager;

public class Tracker {

	private DataBaseManager db;

	private Map <String, Map<Long, Trackable[][]>> amounts;
	private Map<Material, Short> materialIndex;

	private Trackable[] exampleTrackables;
	
	public Tracker() {
		amounts = new TreeMap<String, Map<Long,Trackable[][]>>();
		materialIndex = new TreeMap<Material, Short>();
		exampleTrackables = new Trackable [0];
		for(World w : Bukkit.getWorlds()) {
			amounts.put(w.getName(), new TreeMap<Long, Trackable[][]>());
		}
		db = SkilUp.getDataBaseManager();
	}

	public void handleBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Trackable[] sliceData = getDataSlice(e.getBlock().getWorld().getName(), generateChunkID(b.getChunk()),
				(short) b.getLocation().getBlockY());
		sliceData[materialIndex.get(b.getType())].handleBreak(e);
	}

	public void handlePlace(BlockBreakEvent e) {
		Block b = e.getBlock();
		Trackable[] sliceData = getDataSlice(e.getBlock().getWorld().getName(), generateChunkID(b.getChunk()),
				(short) b.getLocation().getBlockY());
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

	public Trackable[] getDataSlice(String world, long chunkID, short y) {
		Map <Long, Trackable[][]> worldMap = amounts.get(world);
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
	
	public void initLayer(String world, long chunkID, short y, Trackable [] trackables) {
		World w = Bukkit.getWorld(world);
		for(int i = 0; i < exampleTrackables.length ; i++) {
			trackables[i] = exampleTrackables[i].clone();
		}
		Chunk c = w.getChunkAt((int)(chunkID >> 32), (int) chunkID);
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				Block b = c.getBlock(x, y, z);
				Short slot = getDataIndex(b.getType());
				if (slot != null) {
					trackables[slot].addLocation(b.getLocation());
				}
			}
		}
	}

	private Trackable[] getDataSliceForSaving(String world, long chunkID, short y) {
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
		for (short y = 0; y <= 255; y++) {
			Trackable[] slice = getDataSliceForSaving(c.getWorld().getName(), id, (short) y);
			if (slice != null) {
				db.saveChunkData(c.getWorld().getName(), id, slice, y);
			}
		}
	}

	public Trackable[] getTrackables() {
		return exampleTrackables;
	}
	
	public void registerTrackable(Trackable t) {
		Trackable [] exampleReplacement = new Trackable [exampleTrackables.length + 1];
		//copy old ones over
		for(int i = 0; i < exampleTrackables.length; i++) {
			exampleReplacement [i] = exampleTrackables [i];
		}
		exampleReplacement[exampleTrackables.length] = t;
		materialIndex.put(t.getMaterial(), (short) exampleTrackables.length);
		exampleTrackables = exampleReplacement;
	}
}
