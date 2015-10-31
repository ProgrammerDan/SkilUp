package com.github.maxopoly.SkilUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.maxopoly.SkilUp.listeners.effects.EffectBlockBreakListener;
import com.github.maxopoly.SkilUp.listeners.effects.EffectBlockPlaceListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.BlockBreakListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.BlockPlaceListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.FishingListener;
import com.github.maxopoly.SkilUp.rewards.AbstractReward;
import com.github.maxopoly.SkilUp.rewards.BuffReward;
import com.github.maxopoly.SkilUp.rewards.DropReward;
import com.github.maxopoly.SkilUp.skills.Skill;

import static com.github.maxopoly.SkilUp.SkilUp.sendConsoleMessage;

public class ConfigParser {
	SkilUp plugin;

	ConfigParser(SkilUp plugin) {
		this.plugin = plugin;
	}

	public SkilUpManager parseConfig() {
		sendConsoleMessage("Initializing config");
		SkilUpManager manager = new SkilUpManager();
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		ConfigurationSection skills = config.getConfigurationSection("skills");
		for (String key : skills.getKeys(false)) {
			Skill skill = parseSkill(skills.getConfigurationSection(key));
			manager.addSkill(skill);
		}
		return manager;
	}

	public Skill parseSkill(ConfigurationSection config) {
		String name = config.getString("name");
		String lvlUpMsg = config.getString("level_up_message");
		ArrayList<AbstractReward> rewards = parseRewards(config
				.getConfigurationSection("rewards"));
		Skill skill = new Skill(name, rewards, lvlUpMsg);
		for (AbstractReward reward : rewards) {
			reward.setSkill(skill);
		}
		parseXPListeners(skill, config.getConfigurationSection("xpgains"));
		return skill;
	}

	public void parseXPListeners(Skill skill, ConfigurationSection cs) {
		for(String key: cs.getKeys(false)) {
			ConfigurationSection current = cs.getConfigurationSection(key);
			String type = current.getString("type");
			switch (type) {
			case "BLOCKPLACE":
				String lore = cs.getString("lore");
				Material mat = Material.getMaterial(current.getString("material"));
				Integer durability = integerNullCheck(current, "durability");
				int xpbp = current.getInt("xp");
				BlockPlaceListener bpl = new BlockPlaceListener(skill, xpbp, mat, durability, lore);
				registerListener(bpl);
				break;
			case "BLOCKBREAK":
				Material mate = Material.getMaterial(current.getString("material"));
				Integer blockType = integerNullCheck(current, "durability");
				int xpbb = current.getInt("xp");
				BlockBreakListener bbl = new BlockBreakListener(skill, xpbb, mate, blockType);
				registerListener(bbl);
				break;
			case "FISHING":
				HashMap <PlayerFishEvent.State,Integer> xpfish = new HashMap<PlayerFishEvent.State, Integer>();
				Integer caughtEntity = integerNullCheck(current, "CAUGHT_ENTITY");
				Integer caughtFish = integerNullCheck(current, "CAUGHT_FISH");
				Integer failedAttempt = integerNullCheck(current, "FAILED_ATTEMPT");
				Integer fishing = integerNullCheck(current, "FISHING");
				Integer inGround = integerNullCheck(current, "IN_GROUND");
				xpfish.put(PlayerFishEvent.State.CAUGHT_ENTITY,caughtEntity);
				xpfish.put(PlayerFishEvent.State.CAUGHT_FISH,caughtFish);
				xpfish.put(PlayerFishEvent.State.FAILED_ATTEMPT, failedAttempt);
				xpfish.put(PlayerFishEvent.State.FISHING,fishing);
				xpfish.put(PlayerFishEvent.State.IN_GROUND, inGround);
				FishingListener fl = new FishingListener(skill,xpfish);
				registerListener(fl);
				break;
			}
		}
		

	}

	public ArrayList<AbstractReward> parseRewards(ConfigurationSection config) {
		ArrayList<AbstractReward> rewards = new ArrayList<AbstractReward>();
		int index = 0;
		for (String key : config.getKeys(false)) {
			AbstractReward ar = null;
			ConfigurationSection current = config.getConfigurationSection(key);
			int reqLvl = current.getInt("required_level");
			double chance = current.getDouble("chance");
			String type = current.getString("type");
			switch (type) {
			case "DROP":
				ItemStack is = parseItemStack(current
						.getConfigurationSection("item"));
				ar = new DropReward(null, reqLvl, index, chance, is);
				break;
			case "BUFF":
				PotionEffect pe = parsePotionEffect(current
						.getConfigurationSection("potion_effect"));
				ar = new BuffReward(null, reqLvl, index, chance, pe);
				break;

			}
			rewards.add(ar);
			parseCause(current.getConfigurationSection("cause"), ar);
			index++;
		}
		return rewards;
	}

	public void parseCause(ConfigurationSection cs, AbstractReward ar) {
		String type = cs.getString("type");
		switch (type) {
		case "BLOCKPLACE":
			String lore = cs.getString("lore");
			Material mat = Material.getMaterial(cs.getString("material"));
			Integer durability = integerNullCheck(cs, "durability");
			EffectBlockPlaceListener ebpl = new EffectBlockPlaceListener(ar,
					mat, durability, lore);
			registerListener(ebpl);
			break;
		case "BLOCKBREAK":
			Material mate = Material.getMaterial(cs.getString("material"));
			Integer blockType = integerNullCheck(cs, "durability");
			EffectBlockBreakListener ebbl = new EffectBlockBreakListener(ar,
					mate, blockType);
			registerListener(ebbl);
			break;
		}
	}

	public Integer integerNullCheck(ConfigurationSection cs, String option) {
		if (cs.contains(option)) {
			return cs.getInt(option);
		}
		return null;
	}

	public void registerListener(Listener lis) {
		plugin.getServer().getPluginManager().registerEvents(lis, plugin);
	}

	public ItemStack parseItemStack(ConfigurationSection cs) {
		Material mat = Material.getMaterial(cs.getString("material"));
		int durability = cs.getInt("durability", 0);
		ItemStack is = new ItemStack(mat, durability);
		ItemMeta meta = is.getItemMeta();
		String displayName = cs.getString("display_name");
		if (displayName != null) {
			meta.setDisplayName(displayName);
		}
		String lore = cs.getString("lore");
		if (lore != null) {
			List<String> lorelist = new LinkedList<String>();
			lorelist.add(lore);
			meta.setLore(lorelist);
		}
		ConfigurationSection enchants = cs.getConfigurationSection("enchants");
		if (enchants != null) {
			for (String enchantKey : enchants.getKeys(false)) {
				ConfigurationSection enchantSection = enchants
						.getConfigurationSection(enchantKey);
				Enchantment enchant = Enchantment.getByName(enchantSection
						.getString("enchant"));
				int level = enchantSection.getInt("level");
				meta.addEnchant(enchant, level, true);
			}
		}
		is.setItemMeta(meta);
		return is;
	}

	public PotionEffect parsePotionEffect(ConfigurationSection cs) {
		PotionEffectType pet = PotionEffectType.getByName(cs.getString("type"));
		int level = cs.getInt("level", 1);
		long duration = parseTime(cs.getString("duration", "5s"));
		double chance = cs.getDouble("chance", 1.0);
		return new PotionEffect(pet, (int) duration, level - 1); // -1 because
																	// its an
																	// amplifier
																	// internally
	}

	private long parseTime(String arg) {
		long result = 0;
		boolean set = true;
		try {
			result += Long.parseLong(arg);
		} catch (NumberFormatException e) {
			set = false;
		}
		if (set) {
			return result;
		}
		while (!arg.equals("")) {
			int length = 0;
			switch (arg.charAt(arg.length() - 1)) {
			case 't': // ticks
				long ticks = getLastNumber(arg);
				result += ticks;
				length = String.valueOf(ticks).length() + 1;
				break;
			case 's': // seconds
				long seconds = getLastNumber(arg);
				result += 20 * seconds; // 20 ticks in a second
				length = String.valueOf(seconds).length() + 1;
				break;
			case 'm': // minutes
				long minutes = getLastNumber(arg);
				result += 20 * 60 * minutes;
				length = String.valueOf(minutes).length() + 1;
				break;
			case 'h': // hours
				long hours = getLastNumber(arg);
				result += 20 * 3600 * hours;
				length = String.valueOf(hours).length() + 1;
				break;
			case 'd': // days, mostly here to easily define a 'never'
				long days = getLastNumber(arg);
				result += 20 * 3600 * 24 * days;
				length = String.valueOf(days).length() + 1;
			default:
				throw new EventException(arg.charAt(arg.length() - 1)
						+ " is not a valid time character");
			}
			arg = arg.substring(0, arg.length() - length);
		}
		return result;
	}

	private long getLastNumber(String arg) {
		StringBuilder number = new StringBuilder();
		for (int i = arg.length() - 2; i >= 0; i--) {
			if (Character.isDigit(arg.charAt(i))) {
				number.insert(0, arg.substring(i, i + 1));
			} else {
				break;
			}
		}
		long result = Long.parseLong(number.toString());
		return result;
	}
}
