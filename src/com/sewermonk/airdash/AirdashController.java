package com.sewermonk.airdash;

import de.slikey.effectlib.EffectManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class AirdashController extends JavaPlugin {
	private EffectManager effectManager;

	@Override
	public void onEnable() {
		effectManager = new EffectManager(this);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Airdash Plugin Loaded v" + getDescription().getVersion());
		getServer().getPluginManager().registerEvents(new AirdashEvents(this, effectManager), this);
	}

	@Override
	public void onDisable() {
		effectManager.dispose();
	}
}
