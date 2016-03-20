package com.github.maxopoly.SkilUp.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a tracked block is broken
 * 
 * @author Maxopoly
 *
 */
public class TrackedBlockBreak extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private Block b;
	private Player p;
	
	public TrackedBlockBreak(Block b, Player p) {
		this.b = b;
		this.p = p;
	}
	
	public Block getBlock() {
		return b;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
		}
	
}
