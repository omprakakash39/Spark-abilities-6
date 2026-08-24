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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class SparkAbilities extends JavaPlugin implements Listener, CommandExecutor {

    private final String GUI_TITLE = ChatColor.DARK_GRAY + "Spark Ability GUI";

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

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("sparkgui") != null) {
            getCommand("sparkgui").setExecutor(this);
        }
        getLogger().info("SparkAbilities plugin has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SparkAbilities plugin has been disabled.");
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

        // 1. Berserk
        ItemStack berserk = new ItemStack(Material.REDSTONE, 1);
        ItemMeta m1 = berserk.getItemMeta();
        m1.setDisplayName(ChatColor.RED + "⚡ Berserk");
        m1.setLore(List.of(ChatColor.GRAY + "Gives Strength & Speed"));
        m1.getPersistentDataContainer().set(new NamespacedKey(this, "berserk_item"), PersistentDataType.BYTE, (byte) 1);
        berserk.setItemMeta(m1);
        gui.setItem(0, berserk);

        // 2. Stun Gun
        ItemStack stunGun = new ItemStack(Material.BLAZE_ROD, 1);
        ItemMeta m2 = stunGun.getItemMeta();
        m2.setDisplayName(ChatColor.AQUA + "🔥 Stun Gun");
        m2.setLore(List.of(ChatColor.GRAY + "Shoots a fireball"));
        m2.getPersistentDataContainer().set(new NamespacedKey(this, "stun_gun"), PersistentDataType.BYTE, (byte) 1);
        stunGun.setItemMeta(m2);
        gui.setItem(1, stunGun);

        // 3. Focus Mode
        ItemStack focus = new ItemStack(Material.SPYGLASS, 1);
        ItemMeta m3 = focus.getItemMeta();
        m3.setDisplayName(ChatColor.LIGHT_PURPLE + "🎯 Focus Mode");
        m3.setLore(List.of(ChatColor.GRAY + "Grants long-term strength"));
        m3.getPersistentDataContainer().set(new NamespacedKey(this, "focus_mode"), PersistentDataType.BYTE, (byte) 1);
        focus.setItemMeta(m3);
        gui.setItem(2, focus);

        // 4. Throwable Web
        ItemStack web = new ItemStack(Material.COBWEB, 1);
        ItemMeta m4 = web.getItemMeta();
        m4.setDisplayName(ChatColor.AQUA + "🕸️ Throwable Web");
        m4.setLore(List.of(ChatColor.GRAY + "Throws a web trap snowball"));
        m4.getPersistentDataContainer().set(new NamespacedKey(this, "throwable_web"), PersistentDataType.BYTE, (byte) 1);
        web.setItemMeta(m4);
        gui.setItem(3, web);

        // 5. XP Jammer
        ItemStack xpJammer = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        ItemMeta m5 = xpJammer.getItemMeta();
        m5.setDisplayName(ChatColor.DARK_PURPLE + "🔮 XP Jammer");
        m5.setLore(List.of(ChatColor.GRAY + "Jams surrounding players' XP items"));
        m5.getPersistentDataContainer().set(new NamespacedKey(this, "xp_jammer"), PersistentDataType.BYTE, (byte) 1);
        xpJammer.setItemMeta(m5);
        gui.setItem(4, xpJammer);

        // 6. Anti-Ability
        ItemStack antiAbility = new ItemStack(Material.POISONOUS_POTATO, 1);
        ItemMeta m6 = antiAbility.getItemMeta();
        m6.setDisplayName(ChatColor.DARK_PURPLE + "🔮 Anti-Ability");
        m6.setLore(List.of(ChatColor.GRAY + "Blocks target's abilities after 3 hits"));
        m6.getPersistentDataContainer().set(new NamespacedKey(this, "anti_ability"), PersistentDataType.BYTE, (byte) 1);
        antiAbility.setItemMeta(m6);
        gui.setItem(5, antiAbility);

        // 7. Effect Disabler
        ItemStack effectDisabler = new ItemStack(Material.MILK_BUCKET, 1);
        ItemMeta m7 = effectDisabler.getItemMeta();
        m7.setDisplayName(ChatColor.YELLOW + "⏰ Effect Disabler");
        m7.setLore(List.of(ChatColor.GRAY + "Clears target's potion effects after 3 hits"));
        m7.getPersistentDataContainer().set(new NamespacedKey(this, "effect_disabler"), PersistentDataType.BYTE, (byte) 1);
        effectDisabler.setItemMeta(m7);
        gui.setItem(6, effectDisabler);

        // 8. Lock In
        ItemStack lockIn = new ItemStack(Material.IRON_BARS, 1);
        ItemMeta m8 = lockIn.getItemMeta();
        m8.setDisplayName(ChatColor.RED + "⚔️ Lock In");
        m8.setLore(List.of(ChatColor.GRAY + "Secures a 1v1 duel after 3 hits"));
        m8.getPersistentDataContainer().set(new NamespacedKey(this, "lock_in"), PersistentDataType.BYTE, (byte) 1);
        lockIn.setItemMeta(m8);
        gui.setItem(7, lockIn);

        // 9. Crafting Chaos
        ItemStack craftingChaos = new ItemStack(Material.CRAFTING_TABLE, 1);
        ItemMeta m9 = craftingChaos.getItemMeta();
        m9.setDisplayName(ChatColor.GOLD + "🪵 Crafting Chaos");
        m9.setLore(List.of(ChatColor.GRAY + "Opens crafting table for target"));
        m9.getPersistentDataContainer().set(new NamespacedKey(this, "crafting_chaos"), PersistentDataType.BYTE, (byte) 1);
        craftingChaos.setItemMeta(m9);
        gui.setItem(8, craftingChaos);

        // 10. Scrambler
        ItemStack scrambler = new ItemStack(Material.PAPER, 1);
        ItemMeta m10 = scrambler.getItemMeta();
        m10.setDisplayName(ChatColor.WHITE + "🔀 Scrambler");
        m10.setLore(List.of(ChatColor.GRAY + "Scrambles target inventory after 3 hits"));
        m10.getPersistentDataContainer().set(new NamespacedKey(this, "scrambler"), PersistentDataType.BYTE, (byte) 1);
        scrambler.setItemMeta(m10);
        gui.setItem(9, scrambler);

        // 11. Top Hat
        ItemStack topHat = new ItemStack(Material.GOLDEN_HELMET, 1);
        ItemMeta m11 = topHat.getItemMeta();
        m11.setDisplayName(ChatColor.YELLOW + "🎩 Top Hat");
        m11.setLore(List.of(ChatColor.GRAY + "Swaps helmet and restores later"));
        m11.getPersistentDataContainer().set(new NamespacedKey(this, "top_hat"), PersistentDataType.BYTE, (byte) 1);
        topHat.setItemMeta(m11);
        gui.setItem(10, topHat);

        // 12. Igloo
        ItemStack igloo = new ItemStack(Material.PACKED_ICE, 1);
        ItemMeta m12 = igloo.getItemMeta();
        m12.setDisplayName(ChatColor.AQUA + "❄️ Igloo Dome");
        m12.setLore(List.of(ChatColor.GRAY + "Traps target in an ice dome"));
        m12.getPersistentDataContainer().set(new NamespacedKey(this, "igloo"), PersistentDataType.BYTE, (byte) 1);
        igloo.setItemMeta(m12);
        gui.setItem(11, igloo);

        player.openInventory(gui);
        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (antiAbilityJammed.containsKey(player.getUniqueId())) {
                if (System.currentTimeMillis() < antiAbilityJammed.get(player.getUniqueId())) {
                    if (item != null && item.hasItemMeta() && !item.getItemMeta().getPersistentDataContainer().getKeys().isEmpty()) {
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
            if (!meta.getPersistentDataContainer().getKeys().isEmpty()) {
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
                    event.getAffectedEntities().forEach(entity -> entity.setFireTicks(40));
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
