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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class SparkAbilities extends JavaPlugin implements CommandExecutor, Listener {

    private static SparkAbilities instance;
    private final HashMap<UUID, Integer> hitCounters = new HashMap<>();
    private final HashMap<UUID, Integer> neutralizerHits = new HashMap<>();
    private final HashMap<UUID, Integer> stickyFingersHits = new HashMap<>();
    private final HashMap<UUID, Long> stickyFingersJammed = new HashMap<>();
    private final HashMap<UUID, Long> xpJammedPlayers = new HashMap<>();
    
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

    // --- COMMAND HANDLER ---
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

    // --- OPEN GUI METHOD (GUI ke andar saare items sirf 1 quantity ke rahenge) ---
    private void openAbilityGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, GUI_TITLE);

        gui.addItem(getGuiAbilityItem("berserk"));
        gui.addItem(getGuiAbilityItem("itemcounter"));
        gui.addItem(getGuiAbilityItem("stickyfingers"));
        gui.addItem(getGuiAbilityItem("clogger"));
        gui.addItem(getGuiAbilityItem("stungun"));
        gui.addItem(getGuiAbilityItem("focusmode"));
        gui.addItem(getGuiAbilityItem("deathtouch"));
        gui.addItem(getGuiAbilityItem("elixir"));
        gui.addItem(getGuiAbilityItem("hulk"));
        gui.addItem(getGuiAbilityItem("web"));
        gui.addItem(getGuiAbilityItem("xpjammer"));
        gui.addItem(getGuiAbilityItem("neutralizer"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    // --- HELPER FOR GUI ITEMS (Amount = 1) ---
    private ItemStack getGuiAbilityItem(String type) {
        ItemStack item = getAbilityItem(type);
        if (item != null) {
            item.setAmount(1);
        }
        return item;
    }

    // --- CREATE ABILITY ITEM HELPER (Inventory mein stackable hone ke liye amount 64) ---
    private ItemStack getAbilityItem(String type) {
        ItemStack item = null;
        NamespacedKey key;
        ItemMeta meta;

        switch (type) {
            case "berserk":
                item = new ItemStack(Material.MAGMA_CREAM, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lBerserk"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click for Strength & Speed III"));
                key = new NamespacedKey(this, "berserk_item");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "itemcounter":
                item = new ItemStack(Material.PAINTING, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lItem Counter"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit player 3 times to check inventory stats"));
                key = new NamespacedKey(this, "item_counter");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "stickyfingers":
                item = new ItemStack(Material.HONEYCOMB, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lSticky Fingers"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit player 3 times to block pickup & drop"));
                key = new NamespacedKey(this, "sticky_fingers");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "clogger":
                item = new ItemStack(Material.WOODEN_SHOVEL, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lClogger"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit player 3 times to clog inventory"));
                key = new NamespacedKey(this, "clogger");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "stungun":
                item = new ItemStack(Material.DIAMOND_HOE, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lStun Gun"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click to shoot a fireball"));
                key = new NamespacedKey(this, "stun_gun");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "focusmode":
                item = new ItemStack(Material.SPYGLASS, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lFocus Mode"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click for 10% more damage boost"));
                key = new NamespacedKey(this, "focus_mode");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;

            case "deathtouch":
                item = new ItemStack(Material.SPLASH_POTION, 1);
                PotionMeta pMeta1 = (PotionMeta) item.getItemMeta();
                pMeta1.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&lDeath's Touch Potion"));
                pMeta1.setLore(List.of(ChatColor.GRAY + "Instant Damage III Splash Potion"));
                pMeta1.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 2), true);
                key = new NamespacedKey(this, "death_touch");
                pMeta1.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(pMeta1);
                return item;

            case "elixir":
                item = new ItemStack(Material.SPLASH_POTION, 1);
                PotionMeta pMeta2 = (PotionMeta) item.getItemMeta();
                pMeta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lElixir of Life Potion"));
                pMeta2.setLore(List.of(ChatColor.GRAY + "Instant Health IX Potion"));
                pMeta2.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 8), true);
                key = new NamespacedKey(this, "elixir_life");
                pMeta2.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(pMeta2);
                return item;

            case "hulk":
                item = new ItemStack(Material.SPLASH_POTION, 1);
                PotionMeta pMeta3 = (PotionMeta) item.getItemMeta();
                pMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&2&lHulk Potion"));
                pMeta3.setLore(List.of(ChatColor.GRAY + "Strength III for 15 seconds"));
                pMeta3.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2), true);
                key = new NamespacedKey(this, "hulk_potion");
                pMeta3.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(pMeta3);
                return item;

            case "web":
                item = new ItemStack(Material.COBWEB, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lThrowable Web"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click to throw 3x3 web trap (15s duration)"));
                key = new NamespacedKey(this, "throwable_web");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;
                
            case "xpjammer":
                item = new ItemStack(Material.REPEATING_COMMAND_BLOCK, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5&lXP Jammer"));
                meta.setLore(List.of(ChatColor.GRAY + "Right click to stop all enemies within", ChatColor.GOLD + "12 blocks" + ChatColor.GRAY + " from throwing XP for " + ChatColor.GOLD + "15 seconds!"));
                key = new NamespacedKey(this, "xp_jammer");
                meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                break;
                
            case "neutralizer":
                item = new ItemStack(Material.BLAZE_ROD, 64);
                meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lNeutralizer"));
                meta.setLore(List.of(ChatColor.GRAY + "Hit a player " + ChatColor.RED + "3 times" + ChatColor.GRAY + " to swap their", ChatColor.GOLD + "Protection 4" + ChatColor.GRAY + " armor pieces to " + ChatColor.GOLD + "Protection 3" + ChatColor.GRAY + " for " + ChatColor.GOLD + "8 seconds."));
                key = new NamespacedKey(this, "neutralizer");
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

    // --- GUI CLICK LISTENER ---
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
    }

    // --- INTERACT ABILITIES (RIGHT CLICK) ---
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.getType() == Material.EXPERIENCE_BOTTLE) {
                if (xpJammedPlayers.containsKey(player.getUniqueId())) {
                    if (System.currentTimeMillis() < xpJammedPlayers.get(player.getUniqueId())) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "🛑 You are XP Jammed! Cannot throw XP right now.");
                        return;
                    } else {
                        xpJammedPlayers.remove(player.getUniqueId());
                    }
                }
            }
        }

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
                LargeFireball fireball = player.launchProjectile(LargeFireball.class);
                fireball.setYield(1.5f);
                player.sendMessage(ChatColor.AQUA + "🔥 Fireball shot from Stun Gun!");
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
            }
            // Focus Mode
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "focus_mode"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "🎯 Focus Mode enabled for 60 seconds!");
                player.playSound(player.getLocation(), Sound.ITEM_SPYGLASS_USE, 1.0f, 1.0f);
            }
            // Throwable Web
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "throwable_web"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setCustomName("ThrowableWebBall");
                player.sendMessage(ChatColor.AQUA + "🕸️ Web trap thrown!");
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
            }
            // XP Jammer Activation
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "xp_jammer"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                
                int jammed = 0;
                for (org.bukkit.entity.Entity entity : player.getNearbyEntities(12, 12, 12)) {
                    if (entity instanceof Player && entity != player) {
                        Player target = (Player) entity;
                        xpJammedPlayers.put(target.getUniqueId(), System.currentTimeMillis() + 15000);
                        target.sendMessage(ChatColor.DARK_PURPLE + "🛑 You have been XP Jammed! Cannot throw XP for 15 seconds!");
                        jammed++;
                    }
                }
                player.sendMessage(ChatColor.LIGHT_PURPLE + "🔮 XP Jammer activated! Jammed " + jammed + " enemies nearby.");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
            }
        }
    }

    // --- PROJECTILE HIT EVENT (For Throwable Web 3x3 Trap) ---
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

// --- STICKY FINGERS: Block Item Pickup ---
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

    // --- STICKY FINGERS: Block Item Drop ---
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

    // --- HIT ABILITIES ---
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player target = (Player) event.getEntity();
            ItemStack item = attacker.getInventory().getItemInMainHand();

            if (!item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();

            // Item Counter
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
            // Sticky Fingers (3 hits par 15 seconds ke liye pickup & drop block)
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "sticky_fingers"), PersistentDataType.BYTE)) {
                int hits = stickyFingersHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    stickyFingersHits.put(attacker.getUniqueId(), 0);
                    stickyFingersJammed.put(target.getUniqueId(), System.currentTimeMillis() + 15000);
                    
                    attacker.sendMessage(ChatColor.AQUA + "🍯 Sticky Fingers activated on " + target.getName() + " for 15 seconds!");
                    target.sendMessage(ChatColor.RED + "⚠️ You are trapped by Sticky Fingers! Cannot pick up or drop items for 15 seconds.");
                    target.playSound(target.getLocation(), Sound.ENTITY_HONEY_BLOCK_SLIDE, 1.0f, 1.0f);
                } else {
                    stickyFingersHits.put(attacker.getUniqueId(), hits);
                    attacker.sendMessage(ChatColor.YELLOW + "Sticky Fingers hits: " + hits + "/3");
                }
            }
            // Clogger
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
            // Neutralizer
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "neutralizer"), PersistentDataType.BYTE)) {
                int hits = neutralizerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    neutralizerHits.put(attacker.getUniqueId(), 0);
                    
                    ItemStack[] armor = target.getInventory().getArmorContents();
                    boolean changed = false;
                    for (int i = 0; i < armor.length; i++) {
                        if (armor[i] != null && armor[i].getEnchantmentLevel(Enchantment.PROTECTION) == 4) {
                            armor[i].addUnsafeEnchantment(Enchantment.PROTECTION, 3);
                            changed = true;
                        }
                    }
                    
                    if (changed) {
                        target.getInventory().setArmorContents(armor);
                        attacker.sendMessage(ChatColor.GOLD + "Neutralized " + target.getName() + "'s armor for 8 seconds!");
                        target.sendMessage(ChatColor.RED + "⚠️ Your armor's protection was reduced for 8 seconds!");
                        
                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            ItemStack[] newArmor = target.getInventory().getArmorContents();
                            for (int i = 0; i < newArmor.length; i++) {
                                if (newArmor[i] != null && newArmor[i].getEnchantmentLevel(Enchantment.PROTECTION) == 3) {
                                    newArmor[i].addUnsafeEnchantment(Enchantment.PROTECTION, 4);
                                }
                            }
                            target.getInventory().setArmorContents(newArmor);
                            target.sendMessage(ChatColor.GREEN + "🛡️ Your armor protection has returned to normal!");
                        }, 160L);
                    } else {
                        attacker.sendMessage(ChatColor.YELLOW + target.getName() + " does not have Protection 4 armor equipped.");
                    }
                } else {
                    neutralizerHits.put(attacker.getUniqueId(), hits);
                    attacker.sendMessage(ChatColor.GOLD + "Neutralizer hits: " + hits + "/3");
                }
            }
        }
    }
}
