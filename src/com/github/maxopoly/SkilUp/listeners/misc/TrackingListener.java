package com.github.maxopoly.SkilUp.listeners.misc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.maxopoly.SkilUp.tracking.Tracker;

public class TrackingListener implements Listener {
	
	private Tracker tracker;
	
	public TrackingListener(Tracker tracker) {
		this.tracker = tracker;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent e) {
		tracker.handleBreak(e);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockBreak(BlockPlaceEvent e) {
		tracker.handlePlace(e);
	}

}
