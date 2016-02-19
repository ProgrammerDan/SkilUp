package com.github.maxopoly.SkilUp.listeners.abstractListeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.ListenerUser;

public class CropPlantListener extends AbstractListener {
	private Material material;

	public CropPlantListener (ListenerUser listenerUser, Material material) {
		super(listenerUser);
		this.material = material;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void catchEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.SOIL) {
			return;
		}
		ItemStack is = e.getPlayer().getItemInHand();
		if (is == null) {
			return;
		}
		if (is.getType() == material) {
				tellUser(e, e.getPlayer());
			}
		}
	}
