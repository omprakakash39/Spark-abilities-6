package com.omprepakash39.spark;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class SparkAbilities implements Listener {

    private final Plugin plugin;

    public SparkAbilities(Plugin plugin) {
        this.plugin = plugin;
    }

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, UUID> lockedInTarget = new HashMap<>();
    private final Map<UUID, Long> stickyFingersJammed = new HashMap<>();
    
    // Hit counters
    private final Map<UUID, Integer> itemCounterHits = new HashMap<>();
    private final Map<UUID, Integer> neutralizerHits = new HashMap<>();
    private final Map<UUID, Integer> cloggerHits = new HashMap<>();
    private final Map<UUID, Integer> iglooHits = new HashMap<>();
    private final Map<UUID, Integer> lockInHits = new HashMap<>();
    private final Map<UUID, Integer> craftingChaosHits = new HashMap<>();
    private final Map<UUID, Integer> scramblerHits = new HashMap<>();
    private final Map<UUID, Integer> topHatHits = new HashMap<>();

    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Ability Crate Preview";

    // Helper method for standard items
    public ItemStack createAbilityItem(Material material, String boldName, List<String> loreLines, String keyName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + boldName);
            
            List<String> fullLore = new ArrayList<>();
            fullLore.add(ChatColor.GRAY + "Unbreaking I");
            fullLore.add("");
            for (String line : loreLines) {
                fullLore.add(ChatColor.GRAY + line);
            }
            fullLore.add("");
            fullLore.add(ChatColor.DARK_PURPLE + "PURCHASE ADDITIONAL KEYS");
            fullLore.add(ChatColor.GREEN + "AT: STORE.FLAREMC.ORG");
            
            meta.setLore(fullLore);
            meta.getPersistentDataContainer().set(new NamespacedKey("spark", keyName), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Helper method for custom splash potions
    public ItemStack createPotionItem(String boldName, List<String> loreLines, String keyName) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + boldName);
            
            List<String> fullLore = new ArrayList<>();
            for (String line : loreLines) {
                fullLore.add(ChatColor.RED + line);
            }
            fullLore.add("");
            fullLore.add(ChatColor.DARK_PURPLE + "PURCHASE ADDITIONAL KEYS");
            fullLore.add(ChatColor.GREEN + "AT: STORE.FLAREMC.ORG");
            
            meta.setLore(fullLore);
            meta.getPersistentDataContainer().set(new NamespacedKey("spark", keyName), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        if (stickyFingersJammed.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() < stickyFingersJammed.get(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "⚠ Your inventory is jammed by Sticky Fingers!");
                return;
            } else {
                stickyFingersJammed.remove(player.getUniqueId());
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            NamespacedKey stunGunKey = new NamespacedKey("spark", "stun_gun");
            NamespacedKey focusModeKey = new NamespacedKey("spark", "focus_mode");
            NamespacedKey kothKey = new NamespacedKey("spark", "koth_starter");
            NamespacedKey antiAbilityKey = new NamespacedKey("spark", "anti_ability_ball");
            NamespacedKey xpJammerKey = new NamespacedKey("spark", "xp_jammer");

            if (meta.getPersistentDataContainer().has(stunGunKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.launchProjectile(org.bukkit.entity.Snowball.class);
                player.sendMessage(ChatColor.AQUA + "⚡ Fired Stun Gun projectile!");
                item.setAmount(item.getAmount() - 1);
            } else if (meta.getPersistentDataContainer().has(focusModeKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0, false, true));
                player.sendMessage(ChatColor.GOLD + "🎯 Focus Mode activated!");
            } else if (meta.getPersistentDataContainer().has(kothKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "👑 Random KOTH started!");
            } else if (meta.getPersistentDataContainer().has(antiAbilityKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "🛡 Anti-Ability Ball deployed!");
            } else if (meta.getPersistentDataContainer().has(xpJammerKey, PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "🔮 XP Jammer activated!");
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            if (lockedInTarget.containsKey(attacker.getUniqueId())) {
                UUID allowedTarget = lockedInTarget.get(attacker.getUniqueId());
                if (!target.getUniqueId().equals(allowedTarget)) {
                    event.setCancelled(true);
                    attacker.sendMessage(ChatColor.RED + "🔴 Locked in a 1v1 duel!");
                    return;
                }
            }

            ItemStack item = attacker.getInventory().getItemInMainHand();
            if (!item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();

            if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "effect_disabler"), PersistentDataType.BYTE)) {
                for (PotionEffect pe : target.getActivePotionEffects()) {
                    target.removePotionEffect(pe.getType());
                }
                attacker.sendMessage(ChatColor.YELLOW + "Effect Disabler used.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "sticky_fingers"), PersistentDataType.BYTE)) {
                stickyFingersJammed.put(target.getUniqueId(), System.currentTimeMillis() + 15000);
                target.sendMessage(ChatColor.RED + "⚠ Sticky Fingers applied!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "item_counter"), PersistentDataType.BYTE)) {
                int hits = itemCounterHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    itemCounterHits.put(attacker.getUniqueId(), 0);
                    attacker.sendMessage(ChatColor.LIGHT_PURPLE + "Item Counter scanned " + target.getName());
                } else {
                    itemCounterHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "neutralizer"), PersistentDataType.BYTE)) {
                int hits = neutralizerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    neutralizerHits.put(attacker.getUniqueId(), 0);
                    attacker.sendMessage(ChatColor.GOLD + "Neutralizer activated!");
                } else {
                    neutralizerHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "clogger"), PersistentDataType.BYTE)) {
                int hits = cloggerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    cloggerHits.put(attacker.getUniqueId(), 0);
                    for (int i = 0; i < target.getInventory().getSize(); i++) {
                        if (target.getInventory().getItem(i) == null) {
                            target.getInventory().setItem(i, new ItemStack(Material.WOODEN_SHOVEL));
                        }
                    }
                    attacker.sendMessage(ChatColor.GOLD + "Clogger activated!");
                    item.setAmount(item.getAmount() - 1);
                } else {
                    cloggerHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "igloo"), PersistentDataType.BYTE)) {
                int hits = iglooHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    iglooHits.put(attacker.getUniqueId(), 0);
                    Location tLoc = target.getLocation();
                    List<Block> iceBlocks = new ArrayList<>();
                    for (int x = -1; x <= 1; x++) {
                        for (int y = 0; y <= 2; y++) {
                            for (int z = -1; z <= 1; z++) {
                                Block b = tLoc.clone().add(x, y, z).getBlock();
                                if (b.getType() == Material.AIR) {
                                    b.setType(Material.PACKED_ICE);
                                    iceBlocks.add(b);
                                }
                            }
                        }
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Block b : iceBlocks) {
                            if (b.getType() == Material.PACKED_ICE) b.setType(Material.AIR);
                        }
                    }, 200L);
                    attacker.sendMessage(ChatColor.AQUA + "Igloo trapped target!");
                } else {
                    iglooHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "lock_in"), PersistentDataType.BYTE)) {
                int hits = lockInHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    lockInHits.put(attacker.getUniqueId(), 0);
                    lockedInTarget.put(attacker.getUniqueId(), target.getUniqueId());
                    attacker.sendMessage(ChatColor.RED + "🔒 Locked in duel!");
                } else {
                    lockInHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "crafting_chaos"), PersistentDataType.BYTE)) {
                int hits = craftingChaosHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    craftingChaosHits.put(attacker.getUniqueId(), 0);
                    target.openWorkbench(null, true);
                    attacker.sendMessage(ChatColor.GOLD + "Crafting Chaos opened!");
                } else {
                    craftingChaosHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "scrambler"), PersistentDataType.BYTE)) {
                int hits = scramblerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    scramblerHits.put(attacker.getUniqueId(), 0);
                    ItemStack[] contents = target.getInventory().getStorageContents();
                    List<ItemStack> list = Arrays.asList(contents);
                    Collections.shuffle(list);
                    target.getInventory().setStorageContents(list.toArray(new ItemStack[0]));
                    attacker.sendMessage(ChatColor.LIGHT_PURPLE + "Scrambler used!");
                } else {
                    scramblerHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "top_hat"), PersistentDataType.BYTE)) {
                int hits = topHatHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    topHatHits.put(attacker.getUniqueId(), 0);
                    ItemStack oldHelmet = target.getInventory().getHelmet();
                    target.getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        target.getInventory().setHelmet(oldHelmet);
                    }, 160L);
                    attacker.sendMessage(ChatColor.GOLD + "Top Hat applied!");
                } else {
                    topHatHits.put(attacker.getUniqueId(), hits);
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getEntity();
        if (potion.getItem().hasItemMeta()) {
            ItemMeta meta = potion.getItem().getItemMeta();
            if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "hulk_potion"), PersistentDataType.BYTE)) {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2));
                }
            } else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "death_touch"), PersistentDataType.BYTE)) {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 2));
                }
            } else if (meta.getPersistentDataContainer().has(new NamespacedKey("spark", "elixir_of_life"), PersistentDataType.BYTE)) {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 8));
                }
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
                    player.sendMessage(ChatColor.GREEN + "Item added to inventory!");
                }
            }
        }
    }
                             }
