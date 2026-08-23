package com.sparkmc.abilities;

import com.sparkmc.abilities.command.SparkCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SparkAbilities extends JavaPlugin {

    private static SparkAbilities instance;

    @Override
    public void onEnable() {
        instance = this;

        // Register Command
        if (getCommand("sparkgive") != null) {
            getCommand("sparkgive").setExecutor(new SparkCommand(this));
        }

        getLogger().info("SparkMC Abilities Loaded successfully for 1.21.3!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SparkMC Abilities Disabled.");
    }

    public static SparkAbilities getInstance() {
        return instance;
    }
}
