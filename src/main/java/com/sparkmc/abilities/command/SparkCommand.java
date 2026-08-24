package com.omprakash39.spark;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SparkGuiCommand implements CommandExecutor {

    private final SparkPlugin plugin;
    private final String GUI_TITLE = ChatColor.DARK_GRAY + "Spark Ability GUI";

    public SparkGuiCommand(SparkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("spark.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f&lScrambler"));
        meta.setLore(List.of(ChatColor.GRAY + "Click to get Scrambler ability item"));
        item.setItemMeta(meta);

        gui.setItem(0, item);

        player.openInventory(gui);
        return true;
    }
}
