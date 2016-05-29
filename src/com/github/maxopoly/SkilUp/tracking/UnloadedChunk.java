package com.github.maxopoly.SkilUp.tracking;

import java.util.UUID;

public class UnloadedChunk {
	private long id;
	private long unloadTime;
	private UUID world;
	
	public UnloadedChunk(long id, UUID world, long unloadTime) {
		this.id = id;
		this.world = world;
		this.unloadTime = unloadTime;
	}
	
	public long getID() {
		return id;
	}
	
	public UUID getWorld() {
		return world;
	}
	
	public long getUnloadTime() {
		return unloadTime;
	}
	
	public int hashCode() {
		return (int) id + world.hashCode() + (int) (id >> 32);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof UnloadedChunk)) {
			return false;
		}
		UnloadedChunk uc = (UnloadedChunk) o;
		return uc.getID() == this.id && uc.getWorld().equals(this.world);
	}
	

}
