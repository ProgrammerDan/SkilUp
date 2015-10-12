package com.github.maxopoly.Emray.listeners.xpgains;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import com.github.maxopoly.Emray.skills.Skill;

public class BlockBreakListener extends AbstractXPListener {

	private Material material;
	private Integer data;

	public BlockBreakListener(Skill skill, int xp, Material material,
			Integer data) {
		super(skill, xp);
		this.material = material;
		this.data = data;
	}

	@EventHandler
	public void catchEvent(BlockBreakEvent e) {
		if (material == null || material == e.getBlock().getType()) {
			if (data == null || data == e.getBlock().getData()) {
				giveXP(e.getPlayer());
			}
		}
	}

}
