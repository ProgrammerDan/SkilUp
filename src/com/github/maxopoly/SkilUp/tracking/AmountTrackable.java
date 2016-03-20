package com.github.maxopoly.SkilUp.tracking;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.maxopoly.SkilUp.events.TrackedBlockBreak;

public class AmountTrackable extends Trackable {
	
	private byte amount;
	
	public AmountTrackable(Material mat, byte amount, boolean savedBefore) {
		super(mat, savedBefore);
		this.amount = amount;
	}
	
	public byte getAmount() {
		return amount;
	}
	
	public void setAmount(byte amount) {
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
}
