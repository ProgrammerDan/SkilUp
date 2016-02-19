package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.maxopoly.SkilUp.ListenerUser;

public class RangedDuraBlockBreakListener extends AbstractListener {
	private Material material;
	int lowerDuraRange;
	int upperDuraRange;

	public RangedDuraBlockBreakListener (ListenerUser listenerUser, Material material, int lowerDuraRange, int upperDuraRange) {
		super(listenerUser);
		this.material = material;
		this.lowerDuraRange = lowerDuraRange;
		this.upperDuraRange = upperDuraRange;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void catchEvent(BlockBreakEvent e) {
		Block b = e.getBlock();
		if (b.getType() == material && b.getData() <= upperDuraRange && b.getData() >= lowerDuraRange) {
				tellUser(e, e.getPlayer());
			}
		}
	}