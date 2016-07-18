package com.github.maxopoly.SkilUp.essences;

import java.util.Map;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.database.DataBaseManager;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class EssenceTracker {
	private ItemMap reward;
	// measured in milli seconds, because internally unix time stamps are used
	// to keep track of the essences given out
	private long checkDelay;
	private long rewardDelay;
	private long minimumRest;
	private String bonusMsg;
	private Map<String, Object> bonusFactors;
	private String rewardMsg;
	private String dropMsg;

	public EssenceTracker(long checkDelay, long rewardDelay, long minimumRest, ItemMap reward, String rewardMsg, String bonusMsg, String dropMsg, Map<String, Object> bonusFactors) {
		this.checkDelay = checkDelay;
		this.rewardMsg = rewardMsg;
		this.reward = reward;
		this.rewardDelay = rewardDelay;
		this.minimumRest = minimumRest;
		this.bonusFactors = bonusFactors;
		this.bonusMsg = bonusMsg;
		this.dropMsg = dropMsg;
		new BukkitRunnable(){
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					checkForGiveOut(p);
				}
			}
		}.runTaskTimer(SkilUp.getPlugin(), this.checkDelay, this.checkDelay);
	}

	public void checkForGiveOut(Player p) {
		long[] lastTimes = SkilUp.getDataBaseManager().getEssenceData(p.getUniqueId());
		long lastLogin = lastTimes[0];
		long lastGiven = lastTimes[1];
		// TODO: Here check last login time vs. rewarddelay; use Mercury events.
		// TODO: Much later, add in bonus time checks.
		if ((System.currentTimeMillis() - lastGiven) > minimumRest && 
				(System.currentTimeMillis() - lastLogin) > rewardDelay) {
			System.out.println("Giving out Essences: "+p.getUniqueId() + " (last gift @ " + lastGiven + 
					" vs. now: " + System.currentTimeMillis());
			giveEssence(p);
		}
	}

	public void handleFirstLogin(Player p) {
		SkilUp.getDataBaseManager().initEssenceData(p.getUniqueId());
		handleLogin(p);
	}
	
	public void handleLogin(Player p) {
		SkilUp.getDataBaseManager().updateEssenceLogin(p.getUniqueId(), System.currentTimeMillis());
	}

	public void giveEssence(Player p) {
		SkilUp.getDataBaseManager().updateEssenceGiven(p.getUniqueId(), System.currentTimeMillis());
		Inventory i = p.getInventory();
		p.sendMessage(this.rewardMsg);
		if (this.reward.fitsIn(i)) {
			for (ItemStack is : this.reward.getItemStackRepresentation()) {
				i.addItem(is);
			}
		} else {
			p.sendMessage(this.dropMsg);
			final List<ItemStack> items = this.reward.getItemStackRepresentation();
			final Location l = p.getLocation();
			new BukkitRunnable() {
				@Override
				public void run() {
					for (ItemStack item: items) {
						l.getWorld().dropItem(l.add(0.5, 0.5, 0.5), item).setVelocity(new Vector(0, 0.05, 0));
					}
				}
			}.runTask(SkilUp.getPlugin());
		}
	}
}
