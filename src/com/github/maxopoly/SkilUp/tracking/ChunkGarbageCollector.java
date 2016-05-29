package com.github.maxopoly.SkilUp.tracking;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import com.github.maxopoly.SkilUp.SkilUp;

public class ChunkGarbageCollector {

	// after which time chunks are saved to the db and removed from the cache
	// after being unloaded (in ms)
	private long savingTime;

	private Tracker tracker;

	private Set<UnloadedChunk> chunks;

	public ChunkGarbageCollector(Tracker tracker, long savingTime, long checkIntervall) {
		chunks = Collections
				.newSetFromMap(new ConcurrentHashMap<UnloadedChunk, Boolean>());
		this.tracker = tracker;
		this.savingTime = savingTime;
		System.out.println(Bukkit.getScheduler().scheduleAsyncRepeatingTask(SkilUp.getPlugin(), new Runnable() {
			@Override
			public void run() {
				checkChunks();				
			}
		}, checkIntervall, checkIntervall));
	}

	public void checkChunks() {
		long current = System.currentTimeMillis();
		SkilUp.getPlugin().info("Collecting garbage chunks at " + current);
		for (UnloadedChunk uc : chunks) {
			if ((current - uc.getUnloadTime()) > savingTime) {
				SkilUp.getPlugin().info("Removing chunk " + uc.getID() + "," + uc.getWorld() + " from cache and garbage collection, because of timeout");
				tracker.saveDataAndRemoveFromCache(uc.getID(), uc.getWorld());
				chunks.remove(uc);
			}
		}
	}

	public void removeChunk(long id, UUID world) {
		SkilUp.getPlugin().info("Removing chunk " + id + "," + world + " from garbage collection, because it was loaded again");
		chunks.remove(new UnloadedChunk(id, world, 0L));
	}

	public void addChunk(long id, UUID world) {
		SkilUp.getPlugin().info("Adding chunk " + id + "," + world + " to garbage collection, because it was unloaded");
		chunks.add(new UnloadedChunk(id, world, System.currentTimeMillis()));
	}

}
