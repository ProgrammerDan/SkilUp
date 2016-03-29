package com.github.maxopoly.SkilUp.misc;

import java.util.Random;

public class RandomModule {
	private static Random rng;
	private int startingLvl;
	private int maximumLvl;
	private double startingChance;
	private double endChance;
	private double perLevelMultiplier;
	
	public RandomModule(int startingLvl,int maximumLvl, double startingChance, double endChance) {
		if (rng == null) {
			rng = new Random();
		}
		this.startingChance = startingChance;
		this.startingLvl = startingLvl;
		this.maximumLvl = maximumLvl;
		this.endChance = endChance;
		perLevelMultiplier = (endChance - startingChance) / (maximumLvl - startingLvl);
		}
	
	public boolean roll(int currentLevel) {
		if (currentLevel > maximumLvl || currentLevel < startingLvl) {
			return false;
		}
		return rng.nextDouble() < (((currentLevel - startingLvl) * perLevelMultiplier) + startingChance);
	}
}
