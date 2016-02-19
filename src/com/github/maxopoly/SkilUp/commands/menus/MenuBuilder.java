package com.github.maxopoly.SkilUp.commands.menus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.maxopoly.SkilUp.SkilUp;
import com.github.maxopoly.SkilUp.SkilUpManager;
import com.github.maxopoly.SkilUp.rewards.AbstractReward;
import com.github.maxopoly.SkilUp.skills.PlayerXPStatus;
import com.github.maxopoly.SkilUp.skills.Skill;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;

/**
 * Creates Item GUIs/menus to easily give visual feedback on the current
 * progress and to easily explain how to gain XP and what the individual rewards
 * do. This uses CivModCores ItemGUI.
 * 
 * @author Maxopoly
 *
 */
public class MenuBuilder {
	private SkilUpManager sm = SkilUp.getPlugin().getManager();
	private Clickable exitIcon;
	private Clickable mainMenu;

	public MenuBuilder() {
		new Clickable(ItemStackBuilder.createItemStack(Material.BARRIER, 1,
				(short) 0, "Click to exit the menu", "Enough of those numbers")) {

			@Override
			public void clicked(Player p) {
				ClickableInventory.forceCloseInventory(p);
			}
		};
		mainMenu = new Clickable(ItemStackBuilder.createItemStack(
				Material.WOODEN_DOOR, 1, (short) 0, "Back",
				"Click to return to the main menu")) {

			@Override
			public void clicked(Player p) {
				showSkillOverview(p);

			}
		};
	}

	public void showSkillOverview(Player p) {
		ClickableInventory ci = new ClickableInventory(
				InventoryType.ENDER_CHEST, "Click a skill for more information");
		ci.setSlot(exitIcon, 8);
		int slot = 10;
		for (Skill s : sm.getSkills()) {
			final PlayerXPStatus pxps = s.getStatus(p);
			ItemStack skillStack = ItemStackBuilder.createItemStack(s
					.getItemRepresentation().getType(), pxps.getLevel(), s
					.getItemRepresentation().getDurability(), s.getName(),
					"Level " + pxps.getLevel() + ", " + pxps.getCurrentXP()
							+ " / " + pxps.getTotalXPForLvlUp() + " XP",
					"Click for more information");
			ci.setSlot(new Clickable(skillStack) {

				@Override
				public void clicked(Player p) {
					showSkillInfo(p, pxps);

				}
			}, slot++);
		}
		ci.showInventory(p);
	}

	public void showSkillInfo(Player p, PlayerXPStatus pxps) {
		final Skill s = pxps.getSkill();
		ClickableInventory ci = new ClickableInventory(
				InventoryType.ENDER_CHEST, s.getName() + "  Level "
						+ pxps.getLevel());
		ci.setSlot(mainMenu, 0);
		ci.setSlot(exitIcon, 8);
		ci.setSlot(
				new Clickable(ItemStackBuilder.createItemStack(s
						.getItemRepresentation().getType(), pxps.getLevel(), s
						.getItemRepresentation().getDurability(), s.getName(),
						pxps.getCurrentXP() + " / " + pxps.getTotalXPForLvlUp()
								+ " XP")) {

					@Override
					public void clicked(Player p) {
						// This is just to display information, we are not
						// actually gonna do anything if someone clicks this
					}
				}, 4);
		int slot = 11;
		for (final AbstractReward r : s.getRewards()) {
			Clickable c = new Clickable(ItemStackBuilder.createItemStack(r
					.getItemRepresentation().getType(), 1, r
					.getItemRepresentation().getDurability(), r.getName(),
					"Click for more information")) {

				@Override
				public void clicked(Player p) {
					ClickableInventory.forceCloseInventory(p);
					p.sendMessage(r.getInfo());
					TextComponent message = new TextComponent(
							"Click here to return to the menu");
					message.setColor(ChatColor.GREEN);
					message.setClickEvent(new ClickEvent(
							ClickEvent.Action.RUN_COMMAND, "/sum "
									+ s.getName()));
					p.spigot().sendMessage(message);
				}
			};
			ci.setSlot(c, slot);
			slot++;
			if (slot == 16) {
				slot = 20;
			}
		}

	}
}
