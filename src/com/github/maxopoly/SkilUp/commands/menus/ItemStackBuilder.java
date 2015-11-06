package com.github.maxopoly.SkilUp.commands.menus;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility class to easily create named lored itemstacks
 * 
 * @author Maxopoly
 *
 */
public class ItemStackBuilder {

	/**
	 * Utility to create ItemStacks with names and lores for menus easier
	 * 
	 * @param material
	 *            Material of the itemstack
	 * @param amount
	 *            How many items are on the stack, this can be more than 64 if
	 *            needed
	 * @param dura
	 *            Durability of the itemstack, 0 means default
	 * @param name
	 *            Name of the itemstack, null means no name
	 * @param lore
	 *            Lore of the itemstack, null means no lore
	 * @return The complete ItemStack created accordingly to the given parameter
	 */
	public static ItemStack createItemStack(Material material, int amount,
			short dura, String name, String lore) {
		ItemStack is = new ItemStack(material, amount, dura);
		ItemMeta im = is.getItemMeta();
		if (name != null) {
			im.setDisplayName(name);
		}
		if (lore != null) {
			LinkedList<String> loreList = new LinkedList<String>();
			loreList.add(lore);
			im.setLore(loreList);
		}
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Utility to create ItemStacks with names and lores for menus easier. This
	 * method is for stacks with 2 lines of lore, use the other method for less
	 * lore
	 * 
	 * @param material
	 *            Material of the itemstack
	 * @param amount
	 *            How many items are on the stack, this can be more than 64 if
	 *            needed
	 * @param dura
	 *            Durability of the itemstack, 0 means default
	 * @param name
	 *            Name of the itemstack, null means no name
	 * @param lore1
	 *            First line of lore
	 * @param lore2
	 *            Second line of lore
	 * @return The complete ItemStack created accordingly to the given parameter
	 */
	public static ItemStack createItemStack(Material material, int amount,
			short dura, String name, String lore1, String lore2) {
		ItemStack is = new ItemStack(material, amount, dura);
		ItemMeta im = is.getItemMeta();
		if (name != null) {
			im.setDisplayName(name);
		}
		LinkedList<String> loreList = new LinkedList<String>();
		loreList.add(lore1);
		loreList.add(lore2);
		im.setLore(loreList);
		is.setItemMeta(im);
		return is;
	}

}
