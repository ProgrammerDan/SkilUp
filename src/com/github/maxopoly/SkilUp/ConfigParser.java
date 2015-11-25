package com.github.maxopoly.SkilUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
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
import com.github.maxopoly.SkilUp.listeners.xpgains.ConsumptionListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.EntityKillListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.EntityTameListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.FishingListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.ItemBreakListener;
import com.github.maxopoly.SkilUp.listeners.xpgains.ShearListener;
import com.github.maxopoly.SkilUp.rewards.AbstractReward;
import com.github.maxopoly.SkilUp.rewards.BuffReward;
import com.github.maxopoly.SkilUp.rewards.DropReward;
import com.github.maxopoly.SkilUp.skills.Skill;

public class ConfigParser {
	SkilUp plugin;

	ConfigParser(SkilUp plugin) {
		this.plugin = plugin;
	}

	public SkilUpManager parseConfig() {
		plugin.info("Initializing config");
		SkilUpManager manager = new SkilUpManager();
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		ConfigurationSection skills = config.getConfigurationSection("skills");
		for (String key : skills.getKeys(false)) {
			Skill skill = parseSkill(skills.getConfigurationSection(key));
			manager.addSkill(skill);
		}
		plugin.info("Finished parsing config and setup manager");
		return manager;
	}

	public Skill parseSkill(ConfigurationSection config) {
		String name = config.getString("name");
		String lvlUpMsg = config.getString("level_up_message");
		ArrayList<AbstractReward> rewards = parseRewards(config
				.getConfigurationSection("rewards"));
		ItemStack representation = parseItemStack(config
				.getConfigurationSection("item_representation"));
		Skill skill = new Skill(name, rewards, lvlUpMsg, representation);
		for (AbstractReward reward : rewards) {
			reward.setSkill(skill);
		}
		parseXPListeners(skill, config.getConfigurationSection("xpgains"));
		return skill;
	}

	public void parseXPListeners(Skill skill, ConfigurationSection cs) {
		plugin.info("Parsing XP listeners for " + skill.getName());
		for (String key : cs.getKeys(false)) {
			ConfigurationSection current = cs.getConfigurationSection(key);
			String type = current.getString("type");
			switch (type) {
			case "BLOCKPLACE":
				String lore = cs.getString("lore");
				Material mat = Material.getMaterial(current
						.getString("material"));
				Integer durability = integerNullCheck(current, "durability");
				int xpbp = current.getInt("xp");
				BlockPlaceListener bpl = new BlockPlaceListener(skill, xpbp,
						mat, durability, lore);
				plugin.info("Parsed blockplace listener, material:" + mat != null ? mat
						.name()
						: "" + ",durability:" + durability != null ? durability
								.toString() : "" + ",lore:" + lore + ",xp:"
								+ String.valueOf(xpbp));
				break;
			case "BLOCKBREAK":
				Material mate = Material.getMaterial(current
						.getString("material"));
				Integer blockType = integerNullCheck(current, "durability");
				int xpbb = current.getInt("xp");
				BlockBreakListener bbl = new BlockBreakListener(skill, xpbb,
						mate, blockType);
				plugin.info("Parsed blockbreak listener, material:" + mate != null ? mate
						.name()
						: "" + ",blockType:" + blockType != null ? blockType
								.toString() : "" + ",xp" + String.valueOf(xpbb));
				break;
			case "FISHING":
				HashMap<PlayerFishEvent.State, Integer> xpfish = new HashMap<PlayerFishEvent.State, Integer>();
				for (String fishKey : current.getKeys(false)) {
					if (!fishKey.equals("type")) {
						ConfigurationSection currentFishSection = current
								.getConfigurationSection(fishKey);
						PlayerFishEvent.State currentState = PlayerFishEvent.State
								.valueOf(currentFishSection.getString("state"));
						xpfish.put(currentState,
								currentFishSection.getInt("xp"));
						plugin.info("Parsed fishing listener, state:"
								+ currentState.toString() + ",xp:"
								+ currentFishSection.getString("xp"));
					}
				}
				FishingListener fl = new FishingListener(skill, xpfish);
				break;
			case "ENTITYKILL":
				HashMap<EntityType, Integer> killXP = new HashMap<EntityType, Integer>();
				for (String killKey : current.getKeys(false)) {
					if (!killKey.equals("type")) {
						ConfigurationSection currentKillSection = current
								.getConfigurationSection(killKey);
						EntityType killType = EntityType
								.valueOf(currentKillSection.getString("type"));
						killXP.put(killType, currentKillSection.getInt("xp"));
						plugin.info("Parsed entity kill listener, type:"
								+ killType.toString() + ",xp:"
								+ currentKillSection.getString("xp"));
					}
				}
				EntityKillListener ekl = new EntityKillListener(skill, killXP);
				break;
			case "TAME":
				HashMap<EntityType, Integer> tameXP = new HashMap<EntityType, Integer>();
				for (String tameKey : current.getKeys(false)) {
					ConfigurationSection currentKillSection = current
							.getConfigurationSection(tameKey);
					EntityType tameType = EntityType.valueOf(currentKillSection
							.getString("type"));
					tameXP.put(tameType, currentKillSection.getInt("xp"));
					plugin.info("Parsed entity tame listener, type:"
							+ tameType.toString() + ",xp:"
							+ currentKillSection.getString("xp"));
				}
				EntityTameListener etl = new EntityTameListener(skill, tameXP);
				break;
			case "ITEMBREAK":
				Material brokenMat = Material.getMaterial(current
						.getString("material"));
				String breakLore = current.getString("lore");
				int itemBreakXP = current.getInt("xp");
				plugin.info("Parsed item break listener, material:" + brokenMat != null ? brokenMat
						.toString() : "" + ",lore:" + breakLore + ",xp:"
						+ String.valueOf(itemBreakXP));
				ItemBreakListener ibl = new ItemBreakListener(skill,
						itemBreakXP, brokenMat, breakLore);
				break;
			case "SHEAR":
				HashMap<EntityType, Integer> shearXP = new HashMap<EntityType, Integer>();
				for (String shearKey : current.getKeys(false)) {
					ConfigurationSection currentKillSection = current
							.getConfigurationSection(shearKey);
					EntityType shearType = EntityType
							.valueOf(currentKillSection.getString("type"));
					shearXP.put(shearType, currentKillSection.getInt("xp"));
					plugin.info("Parsed entity tame listener, type:"
							+ shearType.toString() + ",xp:"
							+ currentKillSection.getString("xp"));
				}
				ShearListener sl = new ShearListener(skill, shearXP);
				break;
			case "CONSUME":
				Material eatMat = Material.getMaterial(current
						.getString("material"));
				Integer durabilityEaten = integerNullCheck(current,
						"durability");
				String loreEaten = current.getString("lore");
				int xpconsume = current.getInt("xp");
				ConsumptionListener cl = new ConsumptionListener(skill, xpconsume,
						eatMat, durabilityEaten, loreEaten);
				plugin.info("Parsed consumption listener, material:" + eatMat != null ? eatMat
						.name()
						: "" + ",durability:" + durabilityEaten != null ? durabilityEaten
								.toString() : "" + ",lore:" + loreEaten
								+ ",xp:" + String.valueOf(xpconsume));
				break;
			default:
				plugin.severe(type
						+ " is not a valid listener type, invalid config");

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
			String info = current.getString("info");
			String name = current.getString("name");
			ItemStack representation = parseItemStack(current
					.getConfigurationSection("item_representation"));
			switch (type) {
			case "DROP":
				ItemStack is = parseItemStack(current
						.getConfigurationSection("item"));
				ar = new DropReward(null, reqLvl, index, chance, info,
						representation, name, is);
				break;
			case "BUFF":
				PotionEffect pe = parsePotionEffect(current
						.getConfigurationSection("potion_effect"));
				ar = new BuffReward(null, reqLvl, index, chance, info,
						representation, name, pe);
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
				ConfigurationException e = new ConfigurationException(
						arg.charAt(arg.length() - 1)
								+ " is not a valid time character");
				e.printStackTrace();
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
