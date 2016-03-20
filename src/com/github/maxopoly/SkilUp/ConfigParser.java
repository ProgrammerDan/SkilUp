package com.github.maxopoly.SkilUp;

import java.util.ArrayList;
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
import com.github.maxopoly.SkilUp.rewards.AbstractReward;
import com.github.maxopoly.SkilUp.rewards.DropReward;
import com.github.maxopoly.SkilUp.skills.Skill;
import com.github.maxopoly.SkilUp.skills.XPDistributer;

public class ConfigParser {
	private SkilUp plugin;
	private String lvlUpMsg;
	private DataBaseManager dbm;
	private EssenceTracker et;

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

		// db stuff
		ConfigurationSection dbStuff = config
				.getConfigurationSection("database");
		String host = dbStuff.getString("host");
		int port = dbStuff.getInt("port");
		String db = dbStuff.getString("database_name");
		String user = dbStuff.getString("user");
		String password = dbStuff.getString("password");
		dbm = new DataBaseManager(manager, host, port, db, user, password,
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
		int index = 0;
		for (String key : config.getKeys(false)) {
			AbstractReward ar = null;
			ConfigurationSection current = config.getConfigurationSection(key);
			int reqLvl = current.getInt("required_level");
			double chance = current.getDouble("chance");
			String type = current.getString("type");
			String info = current.getString("info");
			String name = current.getString("name");
			ItemStack representation = parseItemMap(
					current.getConfigurationSection("item_representation"))
					.getItemStackRepresentation().get(0);
			switch (type) {
			case "DROP":
				ItemMap is = parseItemMap(current
						.getConfigurationSection("item"));
				ar = new DropReward(null, reqLvl, index, chance, info,
						representation, name, is);
				break;
			case "BUFF":
				List<PotionEffect> pe = parsePotionEffects(current
						.getConfigurationSection("potion_effect"));
				break;

			}
			rewards.add(ar);
			parseCause(current.getConfigurationSection("cause"), ar);
			index++;
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
			break;
		case "BLOCKBREAK":
			Material mate = Material.getMaterial(cs.getString("material"));
			Integer blockType = integerNullCheck(cs, "durability");
			break;
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
