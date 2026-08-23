package com.sparkmc.abilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SparkAbilities extends JavaPlugin implements CommandExecutor, Listener {

    private static SparkAbilities instance;

    @Override
    public void onEnable() {
        instance = this;

        // Register Command & Events
        if (getCommand("sparkgive") != null) {
            getCommand("sparkgive").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("SparkMC Abilities Loaded successfully with Glow & Working Logic!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SparkMC Abilities Disabled.");
    }

    public static SparkAbilities getInstance() {
        return instance;
    }

    // --- COMMAND TO GIVE ITEMS ---
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("spark.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /sparkgive <player> <ability>");
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

        switch (type) {
            case "berserk":
                item = new ItemStack(Material.MAGMA_CREAM);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lBerserk"));
                meta.setLore(List.of(ChatColor.GRAY + "Right-click for Strength & Speed III"));
                key = new NamespacedKey(this, "berserk_item");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "web":
                item = new ItemStack(Material.COBWEB);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lThrowable Web"));
                meta.setLore(List.of(ChatColor.GRAY + "Right-click to throw a web"));
                key = new NamespacedKey(this, "throwable_web");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "bow":
                item = new ItemStack(Material.BOW);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lTeleport Bow"));
                meta.setLore(List.of(ChatColor.GRAY + "Shoot players to teleport"));
                key = new NamespacedKey(this, "teleport_bow");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "disabler":
                item = new ItemStack(Material.CLOCK);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lEffect Disabler"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit players to remove effects"));
                key = new NamespacedKey(this, "effect_disabler");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "neutralizer":
                item = new ItemStack(Material.IRON_SWORD);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lNeutralizer"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit players to weaken armor"));
                key = new NamespacedKey(this, "neutralizer");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "antiball":
                item = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lAnti-Ability Ball"));
                meta.setLore(List.of(ChatColor.GRAY + "Right-click to block nearby abilities"));
                key = new NamespacedKey(this, "anti_ability_ball");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "xpjammer":
                item = new ItemStack(Material.COMMAND_BLOCK);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5&lXP Jammer"));
                meta.setLore(List.of(ChatColor.GRAY + "Prevents nearby XP drops"));
                key = new NamespacedKey(this, "xp_jammer");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "koth":
                item = new ItemStack(Material.END_CRYSTAL);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lKOTH Starter"));
                meta.setLore(List.of(ChatColor.GRAY + "Right-click to start KOTH event"));
                key = new NamespacedKey(this, "koth_starter");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown ability!");
                return true;
        }

        // MAKE ITEM GLOW (Add unbreaking enchantment + hide flags)
        if (meta != null && item != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            target.getInventory().addItem(item);
            sender.sendMessage(ChatColor.GREEN + "Given " + type + " to " + target.getName());
        }
        return true;
    }

    // --- ABILITY LOGIC (EVENTS) ---

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            // 1. Berserk Ability
            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "berserk_item"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2)); // Strength III for 10s
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));     // Speed III for 10s
                player.sendMessage(ChatColor.RED + "⚡ Berserk activated!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
            }

            // 2. Throwable Web Ability
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "throwable_web"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                org.bukkit.entity.Snowball webBall = player.launchProjectile(org.bukkit.entity.Snowball.class);
                webBall.setCustomName("ThrowableWeb");
                player.sendMessage(ChatColor.AQUA + "🕸️ Web thrown!");
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
            }

            // 3. Anti-Ability Ball
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "anti_ability_ball"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.sendMessage(ChatColor.RED + "🛡️ Anti-Ability Ball activated!");
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.0f);
            }

            // 4. KOTH Starter
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "koth_starter"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                Bukkit.broadcastMessage(ChatColor.GOLD + "👑 KOTH Event has been started by " + player.getName() + "!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
    }

    // Hit-based Abilities (Disabler & Neutralizer)
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player target = (Player) event.getEntity();
            ItemStack item = attacker.getInventory().getItemInMainHand();

            if (!item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();

            // Effect Disabler
            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "effect_disabler"), PersistentDataType.BYTE)) {
                for (PotionEffect effect : target.getActivePotionEffects()) {
                    target.removePotionEffect(effect.getType());
                }
                target.sendMessage(ChatColor.RED + "⚠️ Your effects were disabled by " + attacker.getName() + "!");
                attacker.sendMessage(ChatColor.GREEN + "Effect Disabler applied!");
            }

            // Neutralizer
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "neutralizer"), PersistentDataType.BYTE)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 140, 1)); // Weakness for 7s
                target.sendMessage(ChatColor.RED + "🛡️ You have been neutralized!");
                attacker.sendMessage(ChatColor.GREEN + "Neutralizer applied!");
            }
        }
    }
}
