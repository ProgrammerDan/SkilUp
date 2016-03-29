package com.github.maxopoly.SkilUp.listeners.misc;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.tracking.Tracker;

public class ChunkLoadListener implements Listener {
	private Tracker tracker;
	
	public ChunkLoadListener() {
		this.tracker = SkilUp.getTracker();
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void chunkLoad(ChunkLoadEvent e) {
		final Chunk c = e.getChunk();
		Bukkit.getScheduler().runTaskAsynchronously(SkilUp.getPlugin(), new Runnable() {
			@Override
			public void run() {
				tracker.loadData(c);				
			}
		});
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void chunkSave(ChunkUnloadEvent e) {
		final Chunk c = e.getChunk();
		Bukkit.getScheduler().runTaskAsynchronously(SkilUp.getPlugin(), new Runnable() {
			@Override
			public void run() {
				tracker.saveData(c);				
			}
		});
	}
	

}
