package com.sparkmc.abilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class SparkAbilities extends JavaPlugin implements CommandExecutor, Listener {

    private static SparkAbilities instance;
    private final HashMap<UUID, Integer> hitCounters = new HashMap<>();
    private final HashMap<UUID, Integer> neutralizerHits = new HashMap<>();
    private final HashMap<UUID, Integer> stickyFingersHits = new HashMap<>();
    private final HashMap<UUID, Integer> antiAbilityHits = new HashMap<>();
    private final HashMap<UUID, Integer> effectDisablerHits = new HashMap<>();
    private final HashMap<UUID, Integer> lockInHits = new HashMap<>();
    private final HashMap<UUID, Integer> craftingChaosHits = new HashMap<>();
    private final HashMap<UUID, Integer> scramblerHits = new HashMap<>();
    private final HashMap<UUID, Integer> topHatHits = new HashMap<>();
    private final HashMap<UUID, Integer> iglooHits = new HashMap<>();

    private final HashMap<UUID, Long> stickyFingersJammed = new HashMap<>();
    private final HashMap<UUID, Long> xpJammedPlayers = new HashMap<>();
    private final HashMap<UUID, Long> antiAbilityJammed = new HashMap<>();
    
    private final HashMap<UUID, UUID> lockInTarget = new HashMap<>();
    private final HashMap<UUID, Long> lockInTime = new HashMap<>();
    
    private final String GUI_TITLE = ChatColor.DARK_PURPLE + "⚡ SparkMC Ability Menu";

    @Override
    public void onEnable() {
        instance = this;

        if (getCommand("sparkgive") != null) {
            getCommand("sparkgive").setExecutor(this);
        }
        if (getCommand("sparkgui") != null) {
            getCommand("sparkgui").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("SparkMC Abilities Loaded successfully!");
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("spark.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("sparkgui")) {
            openAbilityGui(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("sparkgive")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /sparkgive <player> <ability>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            ItemStack abilityItem = getAbilityItem(args[1].toLowerCase());
            if (abilityItem == null) {
                player.sendMessage(ChatColor.RED + "Unknown ability!");
                return true;
            }

            target.getInventory().addItem(abilityItem);
            player.sendMessage(ChatColor.GREEN + "Successfully gave ability to " + target.getName());
        }
        return true;
    }

    private void openAbilityGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, GUI_TITLE);

        gui.addItem(getGuiAbilityItem("berserk"));
        gui.addItem(getGuiAbilityItem("itemcounter"));
        gui.addItem(getGuiAbilityItem("stickyfingers"));
        gui.addItem(getGuiAbilityItem("clogger"));
        gui.addItem(getGuiAbilityItem("stungun"));
        gui.addItem(getGuiAbilityItem("focusmode"));
        gui.addItem(getGuiAbilityItem("deathtouch"));
        gui.addItem(getGuiAbilityItem("elixir"));
        gui.addItem(getGuiAbilityItem("hulk"));
        gui.addItem(getGuiAbilityItem("escapepotion"));
        gui.addItem(getGuiAbilityItem("web"));
        gui.addItem(getGuiAbilityItem("xpjammer"));
        gui.addItem(getGuiAbilityItem("neutralizer"));
        gui.addItem(getGuiAbilityItem("antiability"));
        gui.addItem(getGuiAbilityItem("effectdisabler"));
        gui.addItem(getGuiAbilityItem("lockin"));
        gui.addItem(getGuiAbilityItem("craftingchaos"));
        gui.addItem(getGuiAbilityItem("scrambler"));
        gui.addItem(getGuiAbilityItem("tophat"));
        gui.addItem(getGuiAbilityItem("igloo"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    private ItemStack getGuiAbilityItem(String type) {
        ItemStack item = getAbilityItem(type);
        if (item != null) {
            item.setAmount(1);
        }
        return item;
    }

    private ItemStack getAbilityItem(String type) {
        ItemStack item = null;
        NamespacedKey key;
        ItemMeta meta;

        switch (type) {
            case "berserk":
                item = new ItemStack(Material.MAGMA_CREAM, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lBerserk"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Active Buff",
                    ChatColor.GRAY + "Right click to unleash raw power, granting",
                    ChatColor.GRAY + "Strength & Speed III for a short duration."
                ));
                key = new NamespacedKey(this, "berserk_item");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "itemcounter":
                item = new ItemStack(Material.PAINTING, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lItem Counter"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Hit Counter",
                    ChatColor.GRAY + "Strike an enemy player 3 times to inspect",
                    ChatColor.GRAY + "and analyze their inventory stats."
                ));
                key = new NamespacedKey(this, "item_counter");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "stickyfingers":
                item = new ItemStack(Material.HONEYCOMB, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lSticky Fingers"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Debuff",
                    ChatColor.GRAY + "Hit your opponent 3 times to lock their hands,",
                    ChatColor.GRAY + "blocking item pickups and drops completely."
                ));
                key = new NamespacedKey(this, "sticky_fingers");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "clogger":
                item = new ItemStack(Material.WOODEN_SHOVEL, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lClogger"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Disruption",
                    ChatColor.GRAY + "Land 3 successful hits on a target player",
                    ChatColor.GRAY + "to instantly clog and jam their inventory."
                ));
                key = new NamespacedKey(this, "clogger");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "stungun":
                item = new ItemStack(Material.DIAMOND_HOE, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lStun Gun"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Ranged Weapon",
                    ChatColor.GRAY + "Right click to fire a high-impact shockwave",
                    ChatColor.GRAY + "fireball projectile at enemies."
                ));
                key = new NamespacedKey(this, "stun_gun");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "focusmode":
                item = new ItemStack(Material.SPYGLASS, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lFocus Mode"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Combat Buff",
                    ChatColor.GRAY + "Right click to lock your vision and gain",
                    ChatColor.GRAY + "enhanced combat strength and precision."
                ));
                key = new NamespacedKey(this, "focus_mode");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "deathtouch":
                item = new ItemStack(Material.SPLASH_POTION, 1);
                PotionMeta pMeta1 = (PotionMeta) item.getItemMeta();
                pMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&lDeath's Touch Potion"));
                pMeta1.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Splash Pot",
                    ChatColor.GRAY + "Hurt players and mobs instantly with",
                    ChatColor.GRAY + "devastating Instant Damage III effect."
                ));
                pMeta1.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 2), true);
                key = new NamespacedKey(this, "death_touch");
                pMeta1.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(pMeta1);
                return item;

            case "elixir":
                item = new ItemStack(Material.SPLASH_POTION, 1);
                PotionMeta pMeta2 = (PotionMeta) item.getItemMeta();
                pMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lElixir of Life Potion"));
                pMeta2.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Healing Pot",
                    ChatColor.GRAY + "Throw to restore massive health instantly",
                    ChatColor.GRAY + "using powerful Instant Health IX potency."
                ));
                pMeta2.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 8), true);
                key = new NamespacedKey(this, "elixir_life");
                pMeta2.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(pMeta2);
                return item;

            case "hulk":
                item = new ItemStack(Material.SPLASH_POTION, 1);
                PotionMeta pMeta3 = (PotionMeta) item.getItemMeta();
                pMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2&lHulk Potion"));
                pMeta3.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Buff Pot",
                    ChatColor.GRAY + "Gain immense physical strength III status",
                    ChatColor.GRAY + "boost for 15 seconds upon impact."
                ));
                pMeta3.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2), true);
                key = new NamespacedKey(this, "hulk_potion");
                pMeta3.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(pMeta3);
                return item;

            case "escapepotion":
                item = new ItemStack(Material.SPLASH_POTION, 1);
                PotionMeta pMetaEscape = (PotionMeta) item.getItemMeta();
                pMetaEscape.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lEscape Potion"));
                pMetaEscape.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Ultimate Utility",
                    ChatColor.GRAY + "Your ultimate life saver: grants Speed, Resistance,",
                    ChatColor.GRAY + "Absorption, and Fire Resistance for survival."
                ));
                pMetaEscape.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 3600, 3), true);
                pMetaEscape.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 3600, 3), true);
                pMetaEscape.addCustomEffect(new PotionEffect(PotionEffectType.ABSORPTION, 3600, 3), true);
                pMetaEscape.addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE, 3600, 3), true);
                pMetaEscape.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3600, 0), true);
                key = new NamespacedKey(this, "escape_potion");
                pMetaEscape.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(pMetaEscape);
                return item;

            case "web":
                item = new ItemStack(Material.COBWEB, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lThrowable Web"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Crowd Control",
                    ChatColor.GRAY + "Right click to deploy a 3x3 web trap",
                    ChatColor.GRAY + "that binds enemies for 15 seconds."
                ));
                key = new NamespacedKey(this, "throwable_web");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;
                
            case "xpjammer":
                item = new ItemStack(Material.REPEATING_COMMAND_BLOCK, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5&lXP Jammer"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Utility Jammer",
                    ChatColor.GRAY + "Right click to block opponents nearby",
                    ChatColor.GRAY + "from utilizing experience items."
                ));
                key = new NamespacedKey(this, "xp_jammer");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;
                
            case "neutralizer":
                item = new ItemStack(Material.BLAZE_ROD, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lNeutralizer"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Armor Breaker",
                    ChatColor.GRAY + "Strike an enemy player 3 times to strip",
                    ChatColor.GRAY + "and downgrade their active armor protection."
                ));
                key = new NamespacedKey(this, "neutralizer");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "antiability":
                item = new ItemStack(Material.ENDER_EYE, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5&lAnti Ability Ball"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Silence Tool",
                    ChatColor.GRAY + "Hit an enemy player 3 times in melee combat",
                    ChatColor.GRAY + "to disable their special items for 10 seconds."
                ));
                key = new NamespacedKey(this, "anti_ability");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "effectdisabler":
                item = new ItemStack(Material.CLOCK, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lEffect Disabler"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Purge Tool",
                    ChatColor.GRAY + "Hit your target player 3 consecutive times",
                    ChatColor.GRAY + "to purge and clear all active potion buffs."
                ));
                key = new NamespacedKey(this, "effect_disabler");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "lockin":
                item = new ItemStack(Material.REDSTONE, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lLock In"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: 1v1 Isolation",
                    ChatColor.GRAY + "Hit a target player 3 times to secure a",
                    ChatColor.GRAY + "strict 1v1 lockdown arena duel for 8s."
                ));
                key = new NamespacedKey(this, "lock_in");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "crafting_chaos":
                item = new ItemStack(Material.CRAFTING_TABLE, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lCrafting Chaos"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: UI Disturbance",
                    ChatColor.GRAY + "Hit an enemy player 3 times to forcefully",
                    ChatColor.GRAY + "pop open a crafting table interface."
                ));
                key = new NamespacedKey(this, "crafting_chaos");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "scrambler":
                item = new ItemStack(Material.PAPER, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&f&lScrambler"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Inventory Shuffle",
                    ChatColor.GRAY + "Strike an enemy player 3 times to completely",
                    ChatColor.GRAY + "scramble and shuffle their inventory slots."
                ));
                key = new NamespacedKey(this, "scrambler");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "tophat":
                item = new ItemStack(Material.GOLDEN_HELMET, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lTop Hat"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Helmet Swap",
                    ChatColor.GRAY + "Hit an enemy player 3 times to replace",
                    ChatColor.GRAY + "their helmet with gold for 10 seconds."
                ));
                key = new NamespacedKey(this, "top_hat");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "igloo":
                item = new ItemStack(Material.PACKED_ICE, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lIgloo"));
                meta.setLore(List.of(
                    ChatColor.DARK_GRAY + "▪ Ability Type: Ice Dome & Spear",
                    ChatColor.GRAY + "Hit a player 3 times to create a hemi spear",
                    ChatColor.GRAY + "for 10 second around them using packed ice."
                ));
                key = new NamespacedKey(this, "igloo");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            default:
                return null;
        }

        if (meta != null && item != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "crafting_chaos"), PersistentDataType.BYTE) ||
                meta.getPersistentDataContainer().has(new NamespacedKey(this, "lock_in"), PersistentDataType.BYTE) ||
                meta.getPersistentDataContainer().has(new NamespacedKey(this, "neutralizer"), PersistentDataType.BYTE) ||
                meta.getPersistentDataContainer().has(new NamespacedKey(this, "igloo"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "🛑 You cannot place ability items as blocks!");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                Player player = (Player) event.getWhoClicked();
                if (player.hasPermission("spark.admin")) {
                    player.getInventory().addItem(event.getCurrentItem().clone());
                    player.sendMessage(ChatColor.GREEN + "Added ability item to your inventory!");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                }
            }
        }
        
        if (event.getSlot() == 39 && event.getCurrentItem() != null) {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.hasItemMeta()) {
                if (cursorItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this, "top_hat"), PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(ChatColor.RED + "🛑 You cannot equip Top Hat directly as a helmet!");
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (antiAbilityJammed.containsKey(player.getUniqueId())) {
                if (System.currentTimeMillis() < antiAbilityJammed.get(player.getUniqueId())) {
                    if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().getKeys().size() > 0) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "🛑 You are Anti-Ability jammed! Cannot use ability items.");
                        return;
                    }
                } else {
                    antiAbilityJammed.remove(player.getUniqueId());
                }
            }
        }

        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "berserk_item"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                player.sendMessage(ChatColor.RED + "⚡ Berserk activated!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "stun_gun"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                LargeFireball fireball = player.launchProjectile(LargeFireball.class);
                fireball.setYield(1.5f);
                player.sendMessage(ChatColor.AQUA + "🔥 Fireball shot from Stun Gun!");
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "focus_mode"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "🎯 Focus Mode enabled for 60 seconds!");
                player.playSound(player.getLocation(), Sound.ITEM_SPYGLASS_USE, 1.0f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "throwable_web"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setCustomName("ThrowableWebBall");
                player.sendMessage(ChatColor.AQUA + "🕸️ Web trap thrown!");
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "xp_jammer"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                xpJammedPlayers.put(player.getUniqueId(), System.currentTimeMillis() + 15000);
                player.sendMessage(ChatColor.DARK_PURPLE + "🔮 XP Jammer activated for surrounding enemies!");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (xpJammedPlayers.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() < xpJammedPlayers.get(player.getUniqueId())) {
                if (event.getItem().getType().name().contains("XP") || event.getItem().getType() == Material.EXPERIENCE_BOTTLE) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "🛑 You are XP Jammed! Cannot consume experience items.");
                }
            } else {
                xpJammedPlayers.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player) {
            ItemStack item = event.getPotion().getItem();
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Death's Touch")) {
                    event.getAffectedEntities().forEach(entity -> {
                        entity.setFireTicks(40);
                    });
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();
            if ("ThrowableWebBall".equals(snowball.getCustomName())) {
                Location hitLoc = snowball.getLocation();
                if (event.getHitEntity() != null) {
                    hitLoc = event.getHitEntity().getLocation();
                }

                List<Block> changedBlocks = new ArrayList<>();

                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block block = hitLoc.clone().add(x, y, z).getBlock();
                            if (block.getType() == Material.AIR) {
                                block.setType(Material.COBWEB);
                                changedBlocks.add(block);
                            }
                        }
                    }
                }

                if (!changedBlocks.isEmpty()) {
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        for (Block block : changedBlocks) {
                            if (block.getType() == Material.COBWEB) {
                                block.setType(Material.AIR);
                            }
                        }
                    }, 300L);
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (stickyFingersJammed.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() < stickyFingersJammed.get(player.getUniqueId())) {
                event.setCancelled(true);
            } else {
                stickyFingersJammed.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (stickyFingersJammed.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() < stickyFingersJammed.get(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "🛑 You cannot drop items while affected by Sticky Fingers!");
            } else {
                stickyFingersJammed.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            if (lockInTarget.containsKey(attacker.getUniqueId())) {
                UUID lockedTarget = lockInTarget.get(attacker.getUniqueId());
                if (lockInTime.containsKey(attacker.getUniqueId()) && System.currentTimeMillis() < lockInTime.get(attacker.getUniqueId())) {
                    if (!target.getUniqueId().equals(lockedTarget)) {
                        event.setCancelled(true);
                        attacker.sendMessage(ChatColor.RED + "🔒 You are locked in a 1v1 duel with someone else!");
                        return;
                    }
                } else {
                    lockInTarget.remove(attacker.getUniqueId());
                    lockInTime.remove(attacker.getUniqueId());
                }
            }

            ItemStack item = attacker.getInventory().getItemInMainHand();
            if (!item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();

            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "anti_ability"), PersistentDataType.BYTE)) {
                int hits = antiAbilityHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    antiAbilityHits.put(attacker.getUniqueId(), 0);
                    antiAbilityJammed.put(target.getUniqueId(), System.currentTimeMillis() + 10000);
                    attacker.sendMessage(ChatColor.DARK_PURPLE + "🔮 Anti Ability applied to " + target.getName() + " for 10s!");
                    target.sendMessage(ChatColor.RED + "⚠️ Your abilities have been blocked for 10 seconds!");
                } else {
                    antiAbilityHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "effect_disabler"), PersistentDataType.BYTE)) {
                int hits = effectDisablerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    effectDisablerHits.put(attacker.getUniqueId(), 0);
                    for (PotionEffect effect : target.getActivePotionEffects()) {
                        target.removePotionEffect(effect.getType());
                    }
                    attacker.sendMessage(ChatColor.YELLOW + "⏰ Cleared all effects from " + target.getName() + "!");
                    target.sendMessage(ChatColor.RED + "⚠️ All your potion effects have been cleared!");
                } else {
                    effectDisablerHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "lock_in"), PersistentDataType.BYTE)) {
                int hits = lockInHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    lockInHits.put(attacker.getUniqueId(), 0);
                    lockInTarget.put(attacker.getUniqueId(), target.getUniqueId());
                    lockInTime.put(attacker.getUniqueId(), System.currentTimeMillis() + 8000);
                    attacker.sendMessage(ChatColor.RED + "⚔️ Lock In activated! 1v1 secured with " + target.getName() + " for 8s.");
                    target.sendMessage(ChatColor.RED + "⚠️ You are locked in a 1v1 duel!");
                } else {
                    lockInHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "crafting_chaos"), PersistentDataType.BYTE)) {
                int hits = craftingChaosHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    craftingChaosHits.put(attacker.getUniqueId(), 0);
                    target.openWorkbench(null, true);
                    attacker.sendMessage(ChatColor.GOLD + "🪵 Crafting table opened for " + target.getName() + "!");
                } else {
                    craftingChaosHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "scrambler"), PersistentDataType.BYTE)) {
                int hits = scramblerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    scramblerHits.put(attacker.getUniqueId(), 0);
                    ItemStack[] contents = target.getInventory().getContents();
                    List<ItemStack> list = new ArrayList<>();
                    for (ItemStack content : contents) {
                        if (content != null) list.add(content);
                    }
                    Collections.shuffle(list);
                    target.getInventory().clear();
                    for (int i = 0; i < list.size(); i++) {
                        target.getInventory().setItem(i, list.get(i));
                    }
                    attacker.sendMessage(ChatColor.WHITE + "🔀 Scrambled " + target.getName() + "'s inventory!");
                    target.sendMessage(ChatColor.RED + "⚠️ Your inventory has been scrambled!");
                } else {
                    scramblerHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "top_hat"), PersistentDataType.BYTE)) {
                int hits = topHatHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    topHatHits.put(attacker.getUniqueId(), 0);
                    ItemStack oldHelmet = target.getInventory().getHelmet();
                    target.getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                    attacker.sendMessage(ChatColor.YELLOW + "🎩 Top Hat swapped " + target.getName() + "'s helmet!");
                    
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        target.getInventory().setHelmet(oldHelmet);
                        target.sendMessage(ChatColor.GREEN + "🛡️ Your original helmet has been restored!");
                    }, 200L);
                } else {
                    topHatHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "igloo"), PersistentDataType.BYTE)) {
                int hits = iglooHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    iglooHits.put(attacker.getUniqueId(), 0);
                    Location loc = target.getLocation();
                    List<Block> domeBlocks = new ArrayList<>();

                    for (int x = -2; x <= 2; x++) {
                        for (int y = 0; y <= 3; y++) {
                            for (int z = -2; z <= 2; z++) {
                                if (x*x + y*y + z*z <= 8 && y >= 0) {
                                    Block b = loc.clone().add(x, y, z).getBlock();
                                    if (b.getType() == Material.AIR) {
                                        b.setType(Material.PACKED_ICE);
                                        domeBlocks.add(b);
                                    }
                                }
                            }
                        }
                    }

                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 4, false, true));
                    attacker.sendMessage(ChatColor.AQUA + "❄️ Igloo dome deployed around " + target.getName() + " for 10s!");
                    target.sendMessage(ChatColor.BLUE + "🧊 You have been trapped in an Igloo hemi-sphere spear!");

                    if (!domeBlocks.isEmpty()) {
                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            for (Block b : domeBlocks) {
                                if (b.getType() == Material.PACKED_ICE) {
                                    b.setType(Material.AIR);
                                }
                            }
                        }, 200L);
                    }
                } else {
                    iglooHits.put(attacker.getUniqueId(), hits);
                }
            }
        }
    }
}
