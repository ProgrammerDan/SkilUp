package com.github.maxopoly.SkilUp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseItemMap;
import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parsePotionEffects;
import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

import com.github.maxopoly.SkilUp.database.DataBaseManager;
import com.github.maxopoly.SkilUp.essences.EssenceTracker;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.BlockBreakListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.BlockPlaceListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.ConsumptionListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.RangedDuraBlockBreakListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.CropPlantListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.EntityKillListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.EntityTameListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.FishingListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.ItemBreakListener;
import com.github.maxopoly.SkilUp.listeners.abstractListeners.ShearListener;
import com.github.maxopoly.SkilUp.misc.RandomModule;
import com.github.maxopoly.SkilUp.rewards.AbstractReward;
import com.github.maxopoly.SkilUp.rewards.BuffReward;
import com.github.maxopoly.SkilUp.rewards.DropReward;
import com.github.maxopoly.SkilUp.skills.Skill;
import com.github.maxopoly.SkilUp.skills.XPDistributer;
import com.github.maxopoly.SkilUp.tracking.AmountTrackable;
import com.github.maxopoly.SkilUp.tracking.LocationTrackable;
import com.github.maxopoly.SkilUp.tracking.Trackable;
import com.github.maxopoly.SkilUp.tracking.Tracker;

public class ConfigParser {
	private SkilUp plugin;
	private String lvlUpMsg;
	private DataBaseManager dbm;
	private EssenceTracker et;
	private Tracker tracker;

	ConfigParser(SkilUp plugin) {
		this.plugin = plugin;
	}

	public SkilUpManager parseConfig() {
		plugin.info("Initializing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		boolean useBar = config.getBoolean("use_xp_bar", true);
		SkilUpManager manager = new SkilUpManager(useBar);
		lvlUpMsg = config.getString("level_up_message");
		ConfigurationSection skills = config.getConfigurationSection("skills");
		for (String key : skills.getKeys(false)) {
			Skill skill = parseSkill(skills.getConfigurationSection(key));
			manager.addSkill(skill);
		}

		// blocktracking
		parseTracking(config.getConfigurationSection("tracking"));

		// db stuff
		ConfigurationSection dbStuff = config
				.getConfigurationSection("database");
		String host = dbStuff.getString("host");
		int port = dbStuff.getInt("port");
		String db = dbStuff.getString("database_name");
		String user = dbStuff.getString("user");
		String password = dbStuff.getString("password");
		dbm = new DataBaseManager(manager, tracker, host, port, db, user, password,
				plugin.getLogger());

		// essence stuff
		ConfigurationSection essenceSection = config
				.getConfigurationSection("essence");
		if (essenceSection != null) {
			long checkIntervall = parseTime(essenceSection.getString(
					"check_intervall", "30m"));
			long rewardIntervall = 50 * parseTime(essenceSection.getString(
					"reward_intervall", "23h"));
			String msg = essenceSection.getString("reward_msg", ChatColor.BLUE
					+ "You got your daily reward");
			ItemMap reward = parseItemMap(essenceSection
					.getConfigurationSection("item"));
			et = new EssenceTracker(checkIntervall, reward, rewardIntervall,
					msg, dbm);
		}

		plugin.info("Finished parsing config and setup manager");
		return manager;
	}

	public Skill parseSkill(ConfigurationSection config) {
		String name = config.getString("name");
		List<AbstractReward> rewards = new ArrayList<AbstractReward>();
		if (config.contains("rewards")) {
			parseRewards(config.getConfigurationSection("rewards"));
		}
		ItemStack representation = parseItemMap(
				config.getConfigurationSection("item_representation"))
				.getItemStackRepresentation().get(0);
		int hourMultiplier = config.getInt("hour_multiplier");
		Skill skill = new Skill(name, rewards, lvlUpMsg, representation,
				hourMultiplier);
		for (AbstractReward reward : rewards) {
			reward.setSkill(skill);
		}
		parseXPListeners(skill, config.getConfigurationSection("xp_gains"));
		return skill;
	}

	public void parseTracking(ConfigurationSection config) {
		tracker = new Tracker();
		for(String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.severe("Found the key " + key + " in tracking section where only config section identifers are allowed, could not parse this tracker");
				continue;
			}
			String type = current.getString("type");
			if (type == null) {
				plugin.severe("No type was specified for tracker at " + current.getCurrentPath()+ ", could not parse tracker");
				continue;
			}
			String matString = current.getString("material");
			if (matString == null) {
				plugin.severe("No material was specified for tracker at " + current.getCurrentPath()+ ", could not parse tracker");
				continue;
			}
			Material mat = Material.matchMaterial(matString);
			if (mat == null) {
				plugin.severe("Could not recognize material " + matString + " at " + current.getCurrentPath()+ ", could not parse tracker");
			}
			Trackable t = null;
			switch(type) {
			case "AMOUNT":
				t = new AmountTrackable(mat, (short) 0, true);
				plugin.info("Parsed AmountTracker for material:" + mat.toString());
				break;
			case "LOCATION":
				t = new LocationTrackable(mat, new LinkedList <Short>(), true);
				plugin.info("Parsed LocationTracker for material:" + mat.toString());
				break;
			default:
				plugin.severe("Could not identify the tracker type " + type + " at " + current.getCurrentPath());
				continue;
			}
			if (t != null) {
				tracker.registerTrackable(t);
			}
		}
	}

	public void parseXPListeners(Skill skill, ConfigurationSection cs) {
		plugin.info("Parsing XP listeners for " + skill.getName());
		for (String key : cs.getKeys(false)) {
			ConfigurationSection current = cs.getConfigurationSection(key);
			String type = current.getString("type");
			int xp = current.getInt("xp");
			XPDistributer handler = new XPDistributer(skill, xp);
			List<String> lore = current.getStringList("lore");
			Material material = Material.getMaterial(current
					.getString("material"));
			Integer durability = integerNullCheck(current, "durability");
			EntityType entity = EntityType
					.fromName(current.getString("entity"));

			switch (type) {
			case "BLOCKPLACE":
				BlockPlaceListener bpl = new BlockPlaceListener(handler,
						material, durability, lore);
				plugin.info("Parsed blockplace listener, material:"
						+ (material != null ? material.name() : "")
						+ ",durability:"
						+ (durability != null ? durability.toString() : "")
						+ ",lore:" + lore + ",xp:" + String.valueOf(xp));
				break;
			case "BLOCKBREAK":
				BlockBreakListener bbl = new BlockBreakListener(handler,
						material, durability);
				plugin.info("Parsed blockbreak listener, material:"
						+ (material != null ? material.name() : "")
						+ ",blockType:"
						+ (durability != null ? durability.toString() : "")
						+ ",xp" + String.valueOf(xp));
				break;
			case "FISHING":
				FishingListener fl = new FishingListener(handler, material,
						durability, lore);
				plugin.info("Parsed fishing listener, material:"
						+ (material != null ? material.name() : "")
						+ ",blockType:"
						+ (durability != null ? durability.toString() : "")
						+ ",xp" + String.valueOf(xp));
				break;
			case "ENTITYKILL":
				EntityKillListener ekl = new EntityKillListener(handler, entity);
				plugin.info("Parsed entity kill listener, type:"
						+ entity.toString() + ",xp:" + String.valueOf(xp));
				break;
			case "TAME":
				EntityTameListener etl = new EntityTameListener(handler, entity);
				plugin.info("Parsed entity tame listener, type:"
						+ entity.toString() + ",xp:" + String.valueOf(xp));
				break;
			case "ITEMBREAK":
				ItemBreakListener ibl = new ItemBreakListener(handler,
						material, lore);
				plugin.info("Parsed item break listener, material:"
						+ (material != null ? material.toString() : "")
						+ ",lore:" + lore + ",xp:" + String.valueOf(xp));
				break;
			case "SHEAR":
				ShearListener sl = new ShearListener(handler, entity);
				plugin.info("Parsed entity tame listener, type:"
						+ entity.toString() + ",xp:" + String.valueOf(xp));
				break;
			case "CONSUME":
				ConsumptionListener cl = new ConsumptionListener(handler,
						material, durability, lore);
				plugin.info("Parsed consumption listener, material:"
						+ (material != null ? material.name() : "")
						+ ",durability:"
						+ (durability != null ? durability.toString() : "")
						+ ",lore:" + lore + ",xp:" + String.valueOf(xp));
				break;
			case "PLANT":
				CropPlantListener cpl = new CropPlantListener(handler, material);
				plugin.info("Parsed crop plant listener, material:"
						+ (material != null ? material.name() : "") + ",xp:"
						+ String.valueOf(xp));
				break;
			case "RANGEDBLOCKBREAK":
				int lowerBound = current.getInt("lowerDurabilityBound", 0);
				int upperBound = current.getInt("upperDurabilityBound", 7);
				RangedDuraBlockBreakListener cbl = new RangedDuraBlockBreakListener(
						handler, material, lowerBound, upperBound);
				plugin.info("Parsed crop plant listener, material:"
						+ (material != null ? material.name() : "")
						+ ", lower durability bound: " + lowerBound
						+ ", upper durability bound: " + upperBound + ",xp:"
						+ String.valueOf(xp));
				break;
			default:
				plugin.severe(type
						+ " is not a valid listener type, invalid config");
			}
		}
	}

	public ArrayList<AbstractReward> parseRewards(ConfigurationSection config) {
		ArrayList<AbstractReward> rewards = new ArrayList<AbstractReward>();
		for (String key : config.getKeys(false)) {
			AbstractReward ar = null;
			ConfigurationSection current = config.getConfigurationSection(key);
			int reqLvl = current.getInt("required_level", 0);
			int maxLvL = current.getInt("maximum_level", 1000);
			double startingChance = current.getDouble("starting_chance", 1.0);
			double endChance = current.getDouble("end_chance", startingChance);
			String type = current.getString("type");
			String info = current.getString("info");
			String name = current.getString("name");
			ItemStack representation = parseItemMap(
					current.getConfigurationSection("item_representation"))
					.getItemStackRepresentation().get(0);
			RandomModule rng = new RandomModule(reqLvl, maxLvL, startingChance,
					endChance);
			switch (type) {
			case "DROP":
				ItemMap is = parseItemMap(current
						.getConfigurationSection("item"));
				ar = new DropReward(null, reqLvl, maxLvL, info, representation,
						name, rng, is);
				break;
			case "BUFF":
				List<PotionEffect> pe = parsePotionEffects(current
						.getConfigurationSection("potion_effects"));
				ar = new BuffReward(null, reqLvl, maxLvL, info, representation,
						name, pe, rng);
				break;
			default:
				plugin.severe("Could not recognize reward type " + type);
				continue;
			}
			rewards.add(ar);
			parseCause(current.getConfigurationSection("cause"), ar);
		}
		return rewards;
	}

	private void parseCause(ConfigurationSection cs, AbstractReward ar) {
		String type = cs.getString("type");
		switch (type) {
		case "BLOCKPLACE":
			List<String> lore = cs.getStringList("lore");
			Material mat = Material.getMaterial(cs.getString("material"));
			Integer durability = integerNullCheck(cs, "durability");
			BlockPlaceListener bpl = new BlockPlaceListener(ar, mat,
					durability, lore);
			break;
		case "BLOCKBREAK":
			Material mate = Material.getMaterial(cs.getString("material"));
			Integer blockType = integerNullCheck(cs, "durability");
			BlockBreakListener bbl = new BlockBreakListener(ar, mate, blockType);
			break;
		default:
			plugin.severe("Could not recognize reward cause " + type);
		}
	}

	private Integer integerNullCheck(ConfigurationSection cs, String option) {
		if (cs.contains(option)) {
			return cs.getInt(option);
		}
		return null;
	}

	public DataBaseManager getDBManager() {
		return dbm;
	}

	public EssenceTracker getEssenceTracker() {
		return et;
	}
}
