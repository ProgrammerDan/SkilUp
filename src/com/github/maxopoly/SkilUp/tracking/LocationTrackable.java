package com.github.maxopoly.SkilUp.tracking;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.maxopoly.SkilUp.events.TrackedBlockBreak;

public class LocationTrackable extends Trackable {
	private List <Short> positions;
	private List <Short> removedPositions;
	
	public LocationTrackable(Material m, List <Short> positions, boolean savedBefore) {
		super(m, savedBefore);
		if (!savedBefore) {
			//if it hasnt been saved before, we will save the position -1 to mark that this layer is tracked
			positions.add((short) -1);
		}
		this.positions = positions;
		removedPositions = new LinkedList <Short> ();
	}
	
	public boolean isTracked(short s) {
		return positions.contains(s);
	}
	
	public void add(short s) {
		positions.add(s);
	}
	
	public void remove(short s) {
		positions.remove(s);
		removedPositions.add(s);
		setDirty(true);
	}
	
	public List<Short> getPositions() {
		return positions;
	}
	
	public List <Short> getRemovedPositions() {
		return removedPositions;
	}
	
	public void handleBreak(BlockBreakEvent e) {
		short b = translateLocation(e.getBlock().getLocation());
		if (positions.contains(b)) {
			positions.remove(b);
			removedPositions.add(b);
			setDirty(true);
			Bukkit.getPluginManager().callEvent(new TrackedBlockBreak(e.getBlock(), e.getPlayer()));
		}
	}
	
	public void handlePlace(BlockPlaceEvent e) {
		
	}
	
	public short translateLocation(Location loc) {
		int x = loc.getBlockX() % 16;
		int z = loc.getBlockX() % 16;
		return (short) ((z * 16) + x);
	}
	
	public Trackable clone() {
		return new LocationTrackable(getMaterial(), new LinkedList <Short> (), false);
	}
	
	public void addLocation(Location loc) {
		add(translateLocation(loc));
	}
}
