package com.sparkmc.abilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SparkAbilities extends JavaPlugin implements CommandExecutor, Listener {

    private static SparkAbilities instance;

    @Override
    public void onEnable() {
        instance = this;

        // Register Command
        if (getCommand("sparkgive") != null) {
            getCommand("sparkgive").setExecutor(this);
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("spark.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /sparkgive <player> <ability>");
            sender.sendMessage(ChatColor.GRAY + "Abilities: berserk, web");
            return true;
        }

        Player target = getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        String type = args[1].toLowerCase();
        ItemStack item = null;
        NamespacedKey key;
        ItemMeta meta;

        if (type.equals("berserk")) {
            item = new ItemStack(Material.MAGMA_CREAM);
            meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lBerserk"));
            meta.setLore(List.of(
                ChatColor.GRAY + "Unbreaking I",
                ChatColor.translateAlternateColorCodes('&', "Right click to receive &eStrength 3"),
                ChatColor.translateAlternateColorCodes('&', "and &eSpeed 3 &ffor &e10 seconds&f."),
                ChatColor.RED + "You cannot eat golden apples while active!",
                "",
                ChatColor.GOLD + "PURCHASE ADDITIONAL KEYS",
                ChatColor.GOLD + "AT: " + ChatColor.GREEN + "STORE.SPARKMC.ORG"
            ));
            key = new NamespacedKey(this, "berserk_item");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        } else if (type.equals("web")) {
            item = new ItemStack(Material.COBWEB);
            meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lThrowable Web"));
            meta.setLore(List.of(
                ChatColor.GRAY + "Unbreaking I",
                ChatColor.translateAlternateColorCodes('&', "Right click to throw a cobweb"),
                ChatColor.translateAlternateColorCodes('&', "in your direction!"),
                "",
                ChatColor.GOLD + "PURCHASE ADDITIONAL KEYS",
                ChatColor.GOLD + "AT: " + ChatColor.GREEN + "STORE.SPARKMC.ORG"
            ));
            key = new NamespacedKey(this, "throwable_web");
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown ability! Use: berserk, web");
            return true;
        }

        target.getInventory().addItem(item);
        sender.sendMessage(ChatColor.GREEN + "Successfully gave " + type + " to " + target.getName() + "!");
        return true;
    }
}
