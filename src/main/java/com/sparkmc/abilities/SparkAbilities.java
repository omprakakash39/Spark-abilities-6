package com.omprakash39.spark;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SparkAbilityManager implements Listener {

    private final SparkPlugin plugin;
    private final String GUI_TITLE = ChatColor.DARK_GRAY + "Spark Ability GUI";

    // HashMaps for tracking states & cooldowns
    private final Map<UUID, Long> antiAbilityJammed = new HashMap<>();
    private final Map<UUID, Long> xpJammedPlayers = new HashMap<>();
    private final Map<UUID, Long> stickyFingersJammed = new HashMap<>();
    private final Map<UUID, UUID> lockInTarget = new HashMap<>();
    private final Map<UUID, Long> lockInTime = new HashMap<>();

    private final Map<UUID, Integer> antiAbilityHits = new HashMap<>();
    private final Map<UUID, Integer> effectDisablerHits = new HashMap<>();
    private final Map<UUID, Integer> lockInHits = new HashMap<>();
    private final Map<UUID, Integer> craftingChaosHits = new HashMap<>();
    private final Map<UUID, Integer> scramblerHits = new HashMap<>();
    private final Map<UUID, Integer> topHatHits = new HashMap<>();
    private final Map<UUID, Integer> iglooHits = new HashMap<>();

    public SparkAbilityManager(SparkPlugin plugin) {
        this.plugin = plugin;
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
            
            if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "berserk_item"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                player.sendMessage(ChatColor.RED + "⚡ Berserk activated!");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "stun_gun"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                LargeFireball fireball = player.launchProjectile(LargeFireball.class);
                fireball.setYield(1.5f);
                player.sendMessage(ChatColor.AQUA + "🔥 Fireball shot from Stun Gun!");
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "focus_mode"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "🎯 Focus Mode enabled for 60 seconds!");
                player.playSound(player.getLocation(), Sound.ITEM_SPYGLASS_USE, 1.0f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "throwable_web"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setCustomName("ThrowableWebBall");
                player.sendMessage(ChatColor.AQUA + "🕸️ Web trap thrown!");
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "xp_jammer"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                
                int affectedCount = 0;
                for (Player p : player.getWorld().getPlayers()) {
                    if (p != player && p.getLocation().distanceSquared(player.getLocation()) <= 100) {
                        xpJammedPlayers.put(p.getUniqueId(), System.currentTimeMillis() + 15000);
                        p.sendMessage(ChatColor.RED + "⚠️ You have been affected by an XP Jammer!");
                        affectedCount++;
                    }
                }
                
                player.sendMessage(ChatColor.DARK_PURPLE + "🔮 XP Jammer activated! Affected " + affectedCount + " surrounding enemies.");
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().getKeys().size() > 0) {
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
        
        // Prevent equipping Top Hat directly into helmet slot (Slot 39 or cursor shift-click)
        if (event.getRawSlot() == 5 || (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && 
            event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "top_hat"), PersistentDataType.BYTE))) {
            if (event.getSlot() == 39 || event.isShiftClick()) {
                ItemStack item = event.getCurrentItem();
                if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "top_hat"), PersistentDataType.BYTE)) {
                    if (event.getRawSlot() == 5 || event.getSlot() == 39) {
                        event.setCancelled(true);
                        event.getWhoClicked().sendMessage(ChatColor.RED + "🛑 You cannot equip Top Hat directly as a helmet!");
                    }
                }
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
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
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

            if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "anti_ability"), PersistentDataType.BYTE)) {
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
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "effect_disabler"), PersistentDataType.BYTE)) {
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
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "lock_in"), PersistentDataType.BYTE)) {
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
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "crafting_chaos"), PersistentDataType.BYTE)) {
                int hits = craftingChaosHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    craftingChaosHits.put(attacker.getUniqueId(), 0);
                    target.openWorkbench(null, true);
                    attacker.sendMessage(ChatColor.GOLD + "🪵 Crafting table opened for " + target.getName() + "!");
                } else {
                    craftingChaosHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "scrambler"), PersistentDataType.BYTE)) {
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
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "top_hat"), PersistentDataType.BYTE)) {
                int hits = topHatHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    topHatHits.put(attacker.getUniqueId(), 0);
                    ItemStack oldHelmet = target.getInventory().getHelmet();
                    target.getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                    attacker.sendMessage(ChatColor.YELLOW + "🎩 Top Hat swapped " + target.getName() + "'s helmet!");
                    
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        target.getInventory().setHelmet(oldHelmet);
                        target.sendMessage(ChatColor.GREEN + "🛡️ Your original helmet has been restored!");
                    }, 200L);
                } else {
                    topHatHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "igloo"), PersistentDataType.BYTE)) {
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
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
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
