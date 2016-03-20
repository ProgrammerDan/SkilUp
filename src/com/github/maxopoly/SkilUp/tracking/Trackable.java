package com.github.maxopoly.SkilUp.tracking;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public abstract class Trackable {
	private Material material;
	private boolean dirty;
	private boolean savedBefore;
	
	public Trackable(Material m, boolean savedBefore) {
		this.material = m;
		dirty = false;
		this.savedBefore = savedBefore;
	}
	
	public abstract void handleBreak(BlockBreakEvent e);
	
	public abstract void handlePlace(BlockPlaceEvent e);
	
	public Material getMaterial() {
		return material;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public boolean savedBefore() {
		return savedBefore;
	}
	
	public int hashCode() {
		return material.hashCode();
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
