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
import com.github.maxopoly.SkilUp.tracking.TrackableConfig;
import com.github.maxopoly.SkilUp.tracking.Tracker;

public class ConfigParser {
	private SkilUp plugin;
	private String lvlUpMsg;
	private DataBaseManager dbm;
	private EssenceTracker et;
	private Tracker tracker;
	private SkilUpManager manager;

	ConfigParser(SkilUp plugin) {
		this.plugin = plugin;
	}

	public SkilUpManager parseConfig() {
		plugin.info("Initializing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		boolean useBar = config.getBoolean("use_xp_bar", true);
		manager = new SkilUpManager(useBar);
		lvlUpMsg = config.getString("level_up_message");
		ConfigurationSection skills = config.getConfigurationSection("skills");
		if (skills != null) {
			for (String key : skills.getKeys(false)) {
				Skill skill = parseSkill(skills.getConfigurationSection(key));
				manager.addSkill(skill);
			}
		} else {
			plugin.warning("No skills found specified in the config. While the plugin will still work, this makes it completly pointless");
		}

		// blocktracking
		parseTracking(config.getConfigurationSection("tracking"));

		// db stuff
		parseDatabase(config.getConfigurationSection("database"));

		// essence stuff
		parseEssences(config.getConfigurationSection("essence"));

		plugin.info("Finished parsing config and setup manager");
		return manager;
	}

	public void parseEssences(ConfigurationSection essenceSection) {
		if (essenceSection != null) {
			long checkIntervall = parseTime(essenceSection.getString(
					"check_intervall", "30m"));
			long rewardIntervall = 50 * parseTime(essenceSection.getString(
					"reward_intervall", "23h"));
			String msg = essenceSection.getString("reward_msg", ChatColor.BLUE
					+ "You got your daily reward");
			ItemMap reward = parseItemMap(essenceSection
					.getConfigurationSection("item"));
			if (reward.getTotalItemAmount() == 0) {
				plugin.warning("Essence data was provided, but no items to give were specified");
			}
			et = new EssenceTracker(checkIntervall, reward, rewardIntervall,
					msg, dbm);
		} else {
			plugin.info("Essence section nonexistent, skipping parsing it");
		}

	}

	public void parseDatabase(ConfigurationSection dbStuff) {
		if (dbStuff == null) {
			plugin.severe("No database credentials specified. This plugin requires a database to run!");
			return;
		}
		String host = dbStuff.getString("host");
		if (host == null) {
			plugin.severe("No host for database specified. Could not load database credentials");
			return;
		}
		int port = dbStuff.getInt("port", -1);
		if (port == -1) {
			plugin.severe("No port for database specified. Could not load database credentials");
			return;
		}
		String db = dbStuff.getString("database_name");
		if (db == null) {
			plugin.severe("No name for database specified. Could not load database credentials");
			return;
		}
		String user = dbStuff.getString("user");
		if (user == null) {
			plugin.severe("No user for database specified. Could not load database credentials");
			return;
		}
		String password = dbStuff.getString("password");
		if (password == null) {
			plugin.severe("No password for database specified. Could not load database credentials");
			return;
		}
		dbm = new DataBaseManager(manager, tracker, host, port, db, user,
				password, plugin.getLogger());

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
		boolean enabled = config.getBoolean("enabled", false);
		if (!enabled) {
			plugin.info("Block tracking is disabled");
			return;
		}
		long checkIntervall = parseTime(config.getString(
				"garbage_collection_intervall", "1m")) * 50; // ms
		long savingTime = parseTime(config.getString("cache_invalidation_time",
				"5m")) * 50; // ms
		plugin.info("Initializing block tracking, garbage_collection_intervall: "
				+ checkIntervall
				+ "ms, cache_invalidation_time: "
				+ savingTime
				+ "ms");
		tracker = new Tracker(savingTime, checkIntervall);
		ConfigurationSection tracked = config
				.getConfigurationSection("tracked");
		if (tracked != null) {
			for (String key : config.getKeys(false)) {
				ConfigurationSection current = config
						.getConfigurationSection(key);
				if (current == null) {
					plugin.severe("Found the key "
							+ key
							+ " in tracking section where only config section identifers are allowed, could not parse this tracker");
					continue;
				}
				String type = current.getString("type");
				if (type == null) {
					plugin.severe("No type was specified for tracker at "
							+ current.getCurrentPath()
							+ ", could not parse tracker");
					continue;
				}
				String matString = current.getString("material");
				if (matString == null) {
					plugin.severe("No material was specified for tracker at "
							+ current.getCurrentPath()
							+ ", could not parse tracker");
					continue;
				}
				Material mat = Material.matchMaterial(matString);
				if (mat == null) {
					plugin.severe("Could not recognize material " + matString
							+ " at " + current.getCurrentPath()
							+ ", could not parse tracker");
				}
				boolean eventOnlyForRegistered = current.getBoolean(
						"event_only_for_registered_block", true);
				boolean registerPlaced = current.getBoolean(
						"register_placed_blocks", false);
				boolean registerGrown = current.getBoolean(
						"register_grown_blocks", false);
				boolean registerPushedIn = current.getBoolean(
						"register_pushed_in", false);
				boolean unregisterPushedOut = current.getBoolean(
						"unregister_pushed_out", true);
				boolean unregisterExploded = current.getBoolean(
						"unregister_exploded", true);
				TrackableConfig tConfig = new TrackableConfig(
						eventOnlyForRegistered, registerPlaced, registerGrown,
						registerPushedIn, unregisterPushedOut,
						unregisterExploded);
				Trackable t = null;
				switch (type) {
				case "AMOUNT":
					t = new AmountTrackable(mat, (short) 0, true, tConfig);
					plugin.info("Parsed AmountTracker for material:"
							+ mat.toString());
					break;
				case "LOCATION":
					t = new LocationTrackable(mat, new LinkedList<Short>(),
							true, tConfig);
					plugin.info("Parsed LocationTracker for material:"
							+ mat.toString());
					break;
				default:
					plugin.severe("Could not identify the tracker type " + type
							+ " at " + current.getCurrentPath());
					continue;
				}
				if (t != null) {
					plugin.info("Tracker configuration: event_only_for_registered_block:"
							+ eventOnlyForRegistered
							+ ", register_placed_blocks:"
							+ registerPlaced
							+ ", register_grown_blocks:"
							+ registerGrown
							+ ", register_pushed_in:"
							+ registerPushedIn
							+ ", unregister_pushed_out:"
							+ unregisterPushedOut
							+ ", unregister_exploded:" + unregisterExploded);
					tracker.registerTrackable(t);
				}
			}
		} else {
			plugin.info("No tracked materials specified in config. If you don't want to use the tracking you should explicitly disable it, as that will improve performance");
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

	public Tracker getTracker() {
		return tracker;
	}
}
