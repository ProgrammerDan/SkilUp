package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.maxopoly.SkilUp.ListenerUser;

public class BlockBreakListener extends AbstractListener {

	private Material material;
	private Integer data;

	public BlockBreakListener(ListenerUser listenerUser, Material material,
			Integer data) {
		super(listenerUser);
		this.material = material;
		this.data = data;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void catchEvent(BlockBreakEvent e) {
		if (material == null || material == e.getBlock().getType()) {
			if (data == null || data == e.getBlock().getData()) {
				tellUser(e, e.getPlayer());
			}
		}
	}

}
