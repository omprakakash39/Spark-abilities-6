package com.omprepakash39.spark;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class SparkAbilityPlugin extends JavaPlugin implements CommandExecutor {

    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Ability Crate Preview";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new SparkAbilities(this), this);
        
        if (getCommand("sparkgui") != null) {
            getCommand("sparkgui").setExecutor(this);
        }
        
        getLogger().info("SparkAbilities successfully loaded, Akki bhai!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SparkAbilities disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("sparkgui")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("spark.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            SparkAbilities abilities = new SparkAbilities(this);
            Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

            // Adding all items & potions to the GUI preview
            gui.addItem(abilities.createAbilityItem(Material.STICK, "Stun Gun", Arrays.asList("Hit a player three times", "Stuns the opponent and item is destroyed after use."), "stun_gun"));
            gui.addItem(abilities.createAbilityItem(Material.NETHERITE_SWORD, "Focus Mode", Arrays.asList("Hit a player three times", "Grants strength and focus in combat."), "focus_mode"));
            gui.addItem(abilities.createAbilityItem(Material.DIAMOND, "KOTH Starter", Arrays.asList("Hit a player three times", "Starts a random King of the Hill event."), "koth_starter"));
            gui.addItem(abilities.createAbilityItem(Material.ENDER_PEARL, "Anti-Ability Ball", Arrays.asList("Hit a player three times", "Deploys an anti-ability shield field."), "anti_ability_ball"));
            gui.addItem(abilities.createAbilityItem(Material.EXPERIENCE_BOTTLE, "XP Jammer", Arrays.asList("Hit a player three times", "Jams enemy experience gains."), "xp_jammer"));
            gui.addItem(abilities.createAbilityItem(Material.IRON_SWORD, "Effect Disabler", Arrays.asList("Hit a player three times", "Removes all active potion effects from target."), "effect_disabler"));
            gui.addItem(abilities.createAbilityItem(Material.STRING, "Sticky Fingers", Arrays.asList("Hit a player three times", "Jams enemy inventory usage for 15 seconds."), "sticky_fingers"));
            gui.addItem(abilities.createAbilityItem(Material.COMPASS, "Item Counter", Arrays.asList("Hit a player three times", "Scans target inventory items after 3 hits."), "item_counter"));
            gui.addItem(abilities.createAbilityItem(Material.SHIELD, "Neutralizer", Arrays.asList("Hit a player three times", "Neutralizes enemy buffs on 3rd hit."), "neutralizer"));
            gui.addItem(abilities.createAbilityItem(Material.WOODEN_SHOVEL, "Clogger", Arrays.asList("Hit a player three times", "Clogs enemy inventory with wooden shovels and destroys after use."), "clogger"));
            gui.addItem(abilities.createAbilityItem(Material.PACKED_ICE, "Igloo", Arrays.asList("Hit a player three times", "Traps the target inside packed ice for a short duration."), "igloo"));
            gui.addItem(abilities.createAbilityItem(Material.CHAINMAIL_HELMET, "Lock In", Arrays.asList("Hit a player three times", "Locks you and target into a 1v1 duel."), "lock_in"));
            gui.addItem(abilities.createAbilityItem(Material.CRAFTING_TABLE, "Crafting Chaos", Arrays.asList("Hit a player three times", "Forces open a crafting workbench for the target."), "crafting_chaos"));
            gui.addItem(abilities.createAbilityItem(Material.FEATHER, "Scrambler", Arrays.asList("Hit a player three times", "Randomly shuffles target storage inventory contents."), "scrambler"));
            gui.addItem(abilities.createAbilityItem(Material.GOLDEN_HELMET, "Top Hat", Arrays.asList("Hit a player three times", "Forces a golden helmet onto target temporarily."), "top_hat"));
            
            // Custom Splash Potions in GUI
            gui.addItem(abilities.createPotionItem("Hulk Potion", Arrays.asList("Splash potion", "Grants Strength III to targets in splash radius."), "hulk_potion"));
            gui.addItem(abilities.createPotionItem("Death Touch", Arrays.asList("Splash potion", "Deals Instant Damage III to players caught in splash."), "death_touch"));
            gui.addItem(abilities.createPotionItem("Elixir of Life", Arrays.asList("Splash potion", "Heals players with Instant Health IX instantly."), "elixir_of_life"));
            gui.addItem(abilities.createPotionItem("Escape Potion", Arrays.asList("Splash potion", "Grants Absorption IV, Resistance IV, Weakness IV, Fire Resistance & Speed IV."), "escape_potion"));

            player.openInventory(gui);
            player.sendMessage(ChatColor.GREEN + "Opened Spark Abilities GUI!");
            return true;
        }
        return false;
    }
}
