package com.omprepakash39.spark;

import org.bukkit.plugin.java.JavaPlugin;

public final class SparkAbilityPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Yeh line tumhari saari abilities aur events ko activate kar degi
        getServer().getPluginManager().registerEvents(new SparkAbilities(this), this);
        getLogger().info("SparkAbilities successfully loaded, Akki bhai!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SparkAbilities disabled.");
    }
}
