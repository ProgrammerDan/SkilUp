package com.github.maxopoly.SkilUp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class SkilUpManager {
	private boolean behindBungee;
	private SkilUp plugin;

	public SkilUpManager(boolean behindBungee) {
		plugin = SkilUp.getPlugin();
		this.behindBungee = behindBungee;
	}

	public boolean isBehindBungee() {
		return behindBungee;
	}
}
