package com.github.maxopoly.SkilUp.essences;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.database.DataBaseManager;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class EssenceTracker {
	private ItemMap reward;
	// measured in milli seconds, because internally unix time stamps are used
	// to keep track of the essences given out
	private long rewardIntervall;
	private String rewardMsg;
	private DataBaseManager db;

	public EssenceTracker(long checkIntervall, ItemMap reward, long rewardIntervall, String rewardMsg, DataBaseManager dbm) {
		db = dbm;
		this.rewardMsg = rewardMsg;
		this.reward = reward;
		this.rewardIntervall = rewardIntervall;
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(SkilUp.getPlugin(), new Runnable() {
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					checkForGiveOut(p);
				}
			}
		},0L, checkIntervall);
	}

	public void checkForGiveOut(Player p) {
		long lastGiven = db.getEssenceData(p.getUniqueId());
		if ((System.currentTimeMillis() - lastGiven) > rewardIntervall) {
			giveEssence(p);
		}
	}

	public void handleFirstLogin(Player p) {
		SkilUp.getDataBaseManager().initEssenceData(p.getUniqueId());
	}

	public void giveEssence(Player p) {
		db.updateEssenceData(p.getUniqueId(), System.currentTimeMillis());
		Inventory i = p.getInventory();
		p.sendMessage(rewardMsg);
		if (reward.fitsIn(i)) {
			for (ItemStack is : reward.getItemStackRepresentation()) {
				i.addItem(is);
			}
		} else {
			p.sendMessage(ChatColor.YELLOW + "Your inventory was full, so the items were dropped");
			for (ItemStack is : reward.getItemStackRepresentation()) {
				p.getLocation().getWorld()
						.dropItemNaturally(p.getLocation(), is);
			}
		}
	}
}
