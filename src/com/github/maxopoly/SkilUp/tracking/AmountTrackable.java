package com.github.maxopoly.SkilUp.tracking;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.maxopoly.SkilUp.events.TrackedBlockBreak;

public class AmountTrackable extends Trackable {
	
	private short amount;
	
	public AmountTrackable(Material mat, short amount, boolean savedBefore) {
		super(mat, savedBefore);
		this.amount = amount;
	}
	
	public short getAmount() {
		return amount;
	}
	
	public void setAmount(short amount) {
		this.amount = amount;
		setDirty(true);
	}
	
	public void handleBreak(BlockBreakEvent e) {
		if (amount > 0) {
			amount--;
			setDirty(true);
			Bukkit.getPluginManager().callEvent(new TrackedBlockBreak(e.getBlock(), e.getPlayer()));
		}
	}
	
	public void handlePlace(BlockPlaceEvent e) {
		
	}
	
	public Trackable clone() {
		return new AmountTrackable(getMaterial(), (short) 0, false);
	}
	
	public void addLocation(Location loc) {
		setAmount((short) (amount+1));
	}
}
