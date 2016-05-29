package com.github.maxopoly.SkilUp.tracking;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.events.TrackedBlockBreak;

public class LocationTrackable extends Trackable {
	// Even if we hold 1,000,000 objects that will only take 4MB of ram.
	// Using a little bit more ram makes the coding way easier.
	private List <Integer> positions;
	private List <Integer> removedPositions;
	
	public LocationTrackable(Material m, List <Integer> positions, boolean savedBefore, TrackableConfig config) {
		super(m, savedBefore, config);
		if (!savedBefore) {
			//if it hasnt been saved before, we will save the position -1 to mark that this layer is tracked
			positions.add(-1);
		}
		this.positions = positions;
		removedPositions = new LinkedList<Integer>();
	}
	
	public boolean isTracked(int s) {
		return positions.contains(s);
	}
	
	public void add(int s) {
		positions.add(s);
	}
	
	public void remove(int s) {
		positions.remove(s);
		removedPositions.add(s);
		setDirty(true);
	}
	
	public List<Integer> getPositions() {
		return positions;
	}
	
	public List<Integer> getRemovedPositions() {
		return removedPositions;
	}
	
	public void handleBreak(BlockBreakEvent e) {
		int b = translateLocation(e.getBlock().getLocation());
		if (!positions.contains(b))
			return;
		positions.remove(b);
		removedPositions.add(b);
		setDirty(true);
		Bukkit.getPluginManager().callEvent(new TrackedBlockBreak(e.getBlock(), e.getPlayer()));
	}
	
	public void handlePlace(BlockPlaceEvent e) {
		int b = translateLocation(e.getBlock().getLocation());
		if (positions.contains(b))
			return;
		positions.add(b);
		removedPositions.remove(b);
		setDirty(true);
	}
	
	public int translateLocation(Location loc) {
		System.out.println("Translating location" + loc.toString());
		int x = loc.getBlockX() % 16;
		System.out.println("Relative x:" + x);
		int z = loc.getBlockZ() % 16;
		System.out.println("Relative z:" + z);
		int y = loc.getBlockY();
		SkilUp.getPlugin().info("Relative y: " + y);
		int pos = ((y*256) + (z * 16) + x);
		System.out.println("Calculated unique location: " + pos);
		return pos;
	}
	
	public Trackable clone() {
		return new LocationTrackable(getMaterial(), new LinkedList<Integer>(), false, config);
	}
	
	public void addLocation(Location loc) {
		add(translateLocation(loc));
	}
}
