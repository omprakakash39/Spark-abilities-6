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
import org.bukkit.entity.Snowball;
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class SparkAbilities extends JavaPlugin implements CommandExecutor, Listener {

    private static SparkAbilities instance;
    private final HashMap<UUID, Integer> hitCounters = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        if (getCommand("sparkgive") != null) {
            getCommand("sparkgive").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("SparkMC Abilities Loaded successfully with all Crate items!");
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
            sender.sendMessage(ChatColor.GRAY + "Abilities: berserk, itemcounter, stickyfingers, clogger, stungun, focusmode, deathtouch, elixir, hulk, web");
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
                meta.setLore(List.of(ChatColor.GRAY + "Right click for Strength & Speed III"));
                key = new NamespacedKey(this, "berserk_item");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "itemcounter":
                item = new ItemStack(Material.PAINTING);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lItem Counter"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit player 3 times to check inventory stats"));
                key = new NamespacedKey(this, "item_counter");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "stickyfingers":
                item = new ItemStack(Material.COBWEB);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lSticky Fingers"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit player to disable item pickup/drop"));
                key = new NamespacedKey(this, "sticky_fingers");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "clogger":
                item = new ItemStack(Material.WOODEN_SHOVEL);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lClogger"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit player 3 times to clog inventory"));
                key = new NamespacedKey(this, "clogger");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "stungun":
                item = new ItemStack(Material.DIAMOND_HOE);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lStun Gun"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click to fire freezing snowball"));
                key = new NamespacedKey(this, "stun_gun");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "focusmode":
                item = new ItemStack(Material.SPYGLASS);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lFocus Mode"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click for 10% more damage boost"));
                key = new NamespacedKey(this, "focus_mode");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "deathtouch":
                item = new ItemStack(Material.SPLASH_POTION);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&lDeath's Touch Potion"));
                meta.setLore(List.of(ChatColor.GRAY + "Instant Damage III Splash Potion"));
                key = new NamespacedKey(this, "death_touch");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "elixir":
                item = new ItemStack(Material.SPLASH_POTION);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lElixir of Life Potion"));
                meta.setLore(List.of(ChatColor.GRAY + "Instant Health IX Potion"));
                key = new NamespacedKey(this, "elixir_life");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "hulk":
                item = new ItemStack(Material.SPLASH_POTION);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2&lHulk Potion"));
                meta.setLore(List.of(ChatColor.GRAY + "Strength III for 15 seconds"));
                key = new NamespacedKey(this, "hulk_potion");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "web":
                item = new ItemStack(Material.COBWEB);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lThrowable Web"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click to throw web"));
                key = new NamespacedKey(this, "throwable_web");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown ability!");
                return true;
        }

        if (meta != null && item != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            target.getInventory().addItem(item);
            sender.sendMessage(ChatColor.GREEN + "Given " + type + " to " + target.getName());
        }
        return true;
    }

    // --- INTERACT ABILITIES (RIGHT CLICK) ---
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            // Berserk
            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "berserk_item"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                player.sendMessage(ChatColor.RED + "⚡ Berserk activated!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
            }
            // Stun Gun
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "stun_gun"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setCustomName("StunGunBall");
                player.sendMessage(ChatColor.AQUA + "❄️ Stun Gun fired!");
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
            }
            // Focus Mode
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "focus_mode"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "🎯 Focus Mode enabled for 60 seconds!");
                player.playSound(player.getLocation(), Sound.ITEM_SPYGLASS_USE, 1.0f, 1.0f);
            }
            // Death's Touch Potion
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "death_touch"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.damage(6.0);
                player.sendMessage(ChatColor.DARK_RED + "☠️ Death's Touch consumed!");
            }
            // Elixir of Life Potion
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "elixir_life"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 8));
                player.sendMessage(ChatColor.GREEN + "❤️ Elixir of Life used!");
            }
            // Hulk Potion
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "hulk_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2));
                player.sendMessage(ChatColor.DARK_GREEN + "💪 Hulk Potion activated!");
            }
            // Throwable Web
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "throwable_web"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.launchProjectile(Snowball.class);
                player.sendMessage(ChatColor.AQUA + "🕸️ Web thrown!");
            }
        }
    }

    // --- HIT ABILITIES ---
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player target = (Player) event.getEntity();
            ItemStack item = attacker.getInventory().getItemInMainHand();

            if (!item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();

            // Item Counter (Hits 3 times)
            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "item_counter"), PersistentDataType.BYTE)) {
                int hits = hitCounters.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    hitCounters.put(attacker.getUniqueId(), 0);
                    int gapples = 0;
                    for (ItemStack invItem : target.getInventory().getContents()) {
                        if (invItem != null && (invItem.getType() == Material.GOLDEN_APPLE || invItem.getType() == Material.ENCHANTED_GOLDEN_APPLE)) {
                            gapples += invItem.getAmount();
                        }
                    }
                    attacker.sendMessage(ChatColor.GOLD + target.getName() + " has " + gapples + " Gapples and " + target.getLevel() + " XP levels!");
                } else {
                    hitCounters.put(attacker.getUniqueId(), hits);
                    attacker.sendMessage(ChatColor.YELLOW + "Hit counter: " + hits + "/3");
                }
            }

            // Sticky Fingers (Using SLOWNESS instead of SLOW)
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "sticky_fingers"), PersistentDataType.BYTE)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 1));
                target.sendMessage(ChatColor.RED + "🛡️ Sticky Fingers applied by " + attacker.getName() + "!");
            }

            // Clogger (Fill inventory with shovels)
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "clogger"), PersistentDataType.BYTE)) {
                int hits = hitCounters.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    hitCounters.put(attacker.getUniqueId(), 0);
                    for (int i = 0; i < target.getInventory().getSize(); i++) {
                        if (target.getInventory().getItem(i) == null) {
                            target.getInventory().setItem(i, new ItemStack(Material.WOODEN_SHOVEL));
                        }
                    }
                    target.sendMessage(ChatColor.RED + "⚠️ Your inventory was clogged!");
                } else {
                    hitCounters.put(attacker.getUniqueId(), hits);
                }
            }
        }
    }
}
