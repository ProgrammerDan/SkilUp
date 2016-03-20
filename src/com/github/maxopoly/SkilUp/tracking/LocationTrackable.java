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
	private List <Byte> positions;
	private List <Byte> removedPositions;
	
	public LocationTrackable(Material m, List <Byte> positions, boolean savedBefore) {
		super(m, savedBefore);
		if (!savedBefore) {
			//if it hasnt been saved before, we will save the position -1 to mark that this layer is tracked
			positions.add((byte) -1);
		}
		this.positions = positions;
		removedPositions = new LinkedList <Byte> ();
	}
	
	public boolean isTracked(byte s) {
		return positions.contains(s);
	}
	
	public void add(byte s) {
		positions.add(s);
	}
	
	public void remove(byte s) {
		positions.remove(s);
		removedPositions.add(s);
		setDirty(true);
	}
	
	public List<Byte> getPositions() {
		return positions;
	}
	
	public List <Byte> getRemovedPositions() {
		return removedPositions;
	}
	
	public void handleBreak(BlockBreakEvent e) {
		byte b = translateLocation(e.getBlock().getLocation());
		if (positions.contains(b)) {
			positions.remove(b);
			removedPositions.add(b);
			setDirty(true);
			Bukkit.getPluginManager().callEvent(new TrackedBlockBreak(e.getBlock(), e.getPlayer()));
		}
	}
	
	public void handlePlace(BlockPlaceEvent e) {
		
	}
	
	public byte translateLocation(Location loc) {
		int x = loc.getBlockX() % 16;
		int z = loc.getBlockX() % 16;
		return (byte) ((z * 16) + x);
	}
}
