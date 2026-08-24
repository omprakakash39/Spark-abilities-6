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
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
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
    private final Map<UUID, Long> berserkActive = new HashMap<>();
    private final Map<UUID, Long> kothCooldown = new HashMap<>();
    private final Map<UUID, UUID> lockedInTarget = new HashMap<>();

    private final Map<UUID, Integer> itemCounterHits = new HashMap<>();
    private final Map<UUID, Integer> neutralizerHits = new HashMap<>();
    private final Map<UUID, Integer> cloggerHits = new HashMap<>();
    private final Map<UUID, Integer> iglooHits = new HashMap<>();
    private final Map<UUID, Integer> lockInHits = new HashMap<>();
    private final Map<UUID, Integer> craftingChaosHits = new HashMap<>();
    private final Map<UUID, Integer> scramblerHits = new HashMap<>();
    private final Map<UUID, Integer> topHatHits = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("sparkgui") != null) {
            getCommand("sparkgui").setExecutor(this);
        }
        getLogger().info("SparkAbilities plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SparkAbilities plugin disabled.");
    }

    private ItemStack createGlowItem(Material mat, int amount, String name, List<String> lore, String keyName) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(new NamespacedKey(this, keyName), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
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

        gui.setItem(0, createGlowItem(Material.CLOCK, 1, ChatColor.YELLOW + "Effect Disabler", List.of(ChatColor.GRAY + "Hit to strip potion effects"), "effect_disabler"));
        gui.setItem(1, createGlowItem(Material.HONEYCOMB, 1, ChatColor.GOLD + "Sticky Fingers", List.of(ChatColor.GRAY + "Prevent drop/pickup"), "sticky_fingers"));
        gui.setItem(2, createGlowItem(Material.ITEM_FRAME, 1, ChatColor.LIGHT_PURPLE + "Item Counter", List.of(ChatColor.GRAY + "Inspect inventory"), "item_counter"));
        gui.setItem(3, createGlowItem(Material.COBWEB, 1, ChatColor.AQUA + "Throwable Web", List.of(ChatColor.GRAY + "Throw web trap"), "throwable_web"));
        gui.setItem(4, createGlowItem(Material.BLAZE_ROD, 1, ChatColor.GOLD + "Neutralizer", List.of(ChatColor.GRAY + "Downgrade armor"), "neutralizer"));
        gui.setItem(5, createGlowItem(Material.MAGMA_CREAM, 1, ChatColor.RED + "Berserk", List.of(ChatColor.GRAY + "Strength & Speed"), "berserk"));
        gui.setItem(6, createGlowItem(Material.SLIME_BALL, 1, ChatColor.LIGHT_PURPLE + "Anti-Ability Ball", List.of(ChatColor.GRAY + "Silence abilities"), "anti_ability_ball"));
        gui.setItem(7, createGlowItem(Material.COMMAND_BLOCK, 1, ChatColor.LIGHT_PURPLE + "XP Jammer", List.of(ChatColor.GRAY + "Jam XP throws"), "xp_jammer"));
        gui.setItem(8, createGlowItem(Material.BOW, 1, ChatColor.LIGHT_PURPLE + "Teleport Bow", List.of(ChatColor.GRAY + "Teleport on hit"), "teleport_bow"));
        gui.setItem(9, createGlowItem(Material.LANTERN, 1, ChatColor.LIGHT_PURPLE + "KOTH Starter", List.of(ChatColor.GRAY + "Start KOTH event"), "koth_starter"));
        gui.setItem(10, createGlowItem(Material.WOODEN_SHOVEL, 1, ChatColor.GOLD + "Clogger", List.of(ChatColor.GRAY + "Fill inventory with shovels"), "clogger"));
        gui.setItem(11, createGlowItem(Material.SPYGLASS, 1, ChatColor.LIGHT_PURPLE + "Focus Mode", List.of(ChatColor.GRAY + "Damage boost"), "focus_mode"));
        gui.setItem(12, createGlowItem(Material.DIAMOND_PICKAXE, 1, ChatColor.LIGHT_PURPLE + "Stun Gun", List.of(ChatColor.GRAY + "Paralyze target"), "stun_gun"));
        gui.setItem(13, createGlowItem(Material.PACKED_ICE, 1, ChatColor.AQUA + "Igloo", List.of(ChatColor.GRAY + "Trap in ice dome"), "igloo"));
        gui.setItem(14, createGlowItem(Material.REDSTONE, 1, ChatColor.RED + "Lock In", List.of(ChatColor.GRAY + "1v1 duel lock"), "lock_in"));
        gui.setItem(15, createGlowItem(Material.CRAFTING_TABLE, 1, ChatColor.GOLD + "Crafting Chaos", List.of(ChatColor.GRAY + "Open crafting menu"), "crafting_chaos"));
        gui.setItem(16, createGlowItem(Material.NETHER_STAR, 1, ChatColor.LIGHT_PURPLE + "Scrambler", List.of(ChatColor.GRAY + "Scramble inventory"), "scrambler"));
        gui.setItem(17, createGlowItem(Material.GOLDEN_HELMET, 1, ChatColor.GOLD + "Top Hat", List.of(ChatColor.GRAY + "Swap helmet"), "top_hat"));

        ItemStack hulk = new ItemStack(Material.POTION);
        ItemMeta hm = hulk.getItemMeta();
        hm.setDisplayName(ChatColor.BLUE + "Hulk Potion");
        hm.getPersistentDataContainer().set(new NamespacedKey(this, "hulk_potion"), PersistentDataType.BYTE, (byte) 1);
        hulk.setItemMeta(hm);
        gui.setItem(18, hulk);

        ItemStack death = new ItemStack(Material.SPLASH_POTION);
        ItemMeta dm = death.getItemMeta();
        dm.setDisplayName(ChatColor.BLUE + "Death's Touch Potion");
        dm.getPersistentDataContainer().set(new NamespacedKey(this, "death_potion"), PersistentDataType.BYTE, (byte) 1);
        death.setItemMeta(dm);
        gui.setItem(19, death);

        ItemStack elixir = new ItemStack(Material.SPLASH_POTION);
        ItemMeta em = elixir.getItemMeta();
        em.setDisplayName(ChatColor.BLUE + "Elixir of Life Potion");
        em.getPersistentDataContainer().set(new NamespacedKey(this, "elixir_potion"), PersistentDataType.BYTE, (byte) 1);
        elixir.setItemMeta(em);
        gui.setItem(20, elixir);

        ItemStack escape = new ItemStack(Material.POTION);
        ItemMeta esm = escape.getItemMeta();
        esm.setDisplayName(ChatColor.LIGHT_PURPLE + "Escape Potion");
        esm.getPersistentDataContainer().set(new NamespacedKey(this, "escape_potion"), PersistentDataType.BYTE, (byte) 1);
        escape.setItemMeta(esm);
        gui.setItem(21, escape);

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
                        player.sendMessage(ChatColor.RED + "🛑 Anti-Ability Ball restriction active!");
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
            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "berserk"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                berserkActive.put(player.getUniqueId(), System.currentTimeMillis() + 10000);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                player.sendMessage(ChatColor.RED + "⚡ Berserk activated!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "anti_ability_ball"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                int count = 0;
                for (Player p : player.getWorld().getPlayers()) {
                    if (p != player && p.getLocation().distanceSquared(player.getLocation()) <= 144) {
                        antiAbilityJammed.put(p.getUniqueId(), System.currentTimeMillis() + 10000);
                        p.sendMessage(ChatColor.RED + "⚠️ Silenced by Anti-Ability Ball!");
                        count++;
                    }
                }
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Anti-Ability Ball deployed on " + count + " players.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "xp_jammer"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                int count = 0;
                for (Player p : player.getWorld().getPlayers()) {
                    if (p != player && p.getLocation().distanceSquared(player.getLocation()) <= 144) {
                        xpJammedPlayers.put(p.getUniqueId(), System.currentTimeMillis() + 15000);
                        p.sendMessage(ChatColor.RED + "⚠️ You are XP Jammed!");
                        count++;
                    }
                }
                player.sendMessage(ChatColor.LIGHT_PURPLE + "XP Jammer locked down " + count + " enemies.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "throwable_web"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                Snowball sb = player.launchProjectile(Snowball.class);
                sb.setCustomName("CustomThrowableWeb");
                player.sendMessage(ChatColor.AQUA + "Throwable Web launched!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "stun_gun"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                Snowball sb = player.launchProjectile(Snowball.class);
                sb.setCustomName("CustomStunGunBall");
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Stun Gun fired!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "focus_mode"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 1200, 0));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Focus Mode active!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "koth_starter"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                long now = System.currentTimeMillis();
                if (kothCooldown.containsKey(player.getUniqueId()) && now < kothCooldown.get(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "⏳ KOTH Starter is on cooldown!");
                    return;
                }
                kothCooldown.put(player.getUniqueId(), now + 1500000L);
                Bukkit.broadcastMessage(ChatColor.GOLD + "⚔️ KOTH event initialized by " + player.getName() + "!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "hulk_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2));
                player.sendMessage(ChatColor.BLUE + "Hulk Potion consumed!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "death_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.damage(6.0);
                player.sendMessage(ChatColor.RED + "Death's Touch Potion triggered!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "elixir_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 9));
                player.sendMessage(ChatColor.GREEN + "Elixir of Life applied!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "escape_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3600, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 3600, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 3600, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 3600, 3));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Escape Potion consumed!");
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (berserkActive.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() < berserkActive.get(player.getUniqueId())) {
                if (event.getItem().getType().name().contains("GOLDEN_APPLE")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "🛑 Golden apples blocked during Berserk!");
                }
            } else {
                berserkActive.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
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
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Snowball sb = (Snowball) event.getEntity();
            if ("CustomThrowableWeb".equals(sb.getCustomName())) {
                Location hitLoc = event.getHitEntity() != null ? event.getHitEntity().getLocation() : sb.getLocation();
                List<Block> placed = new ArrayList<>();
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block b = hitLoc.clone().add(x, y, z).getBlock();
                            if (b.getType() == Material.AIR) {
                                b.setType(Material.COBWEB);
                                placed.add(b);
                            }
                        }
                    }
                }
                if (!placed.isEmpty()) {
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        for (Block b : placed) {
                            if (b.getType() == Material.COBWEB) b.setType(Material.AIR);
                        }
                    }, 160L);
                }
            } else if ("CustomStunGunBall".equals(sb.getCustomName()) && event.getHitEntity() instanceof Player) {
                Player target = (Player) event.getHitEntity();
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 160, 5, false, true));
                target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 160, 250, false, true));
                target.sendMessage(ChatColor.RED + "⚡ Paralyzed by Stun Gun!");
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
                    attacker.sendMessage(ChatColor.RED + "🛑 Locked in a 1v1 duel!");
                    return;
                }
            }

            ItemStack item = attacker.getInventory().getItemInMainHand();
            if (!item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();

            if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "effect_disabler"), PersistentDataType.BYTE)) {
                for (PotionEffect pe : target.getActivePotionEffects()) {
                    target.removePotionEffect(pe.getType());
                }
                attacker.sendMessage(ChatColor.YELLOW + "Effect Disabler used.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "sticky_fingers"), PersistentDataType.BYTE)) {
                stickyFingersJammed.put(target.getUniqueId(), System.currentTimeMillis() + 15000);
                target.sendMessage(ChatColor.RED + "⚠️ Sticky Fingers applied!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "item_counter"), PersistentDataType.BYTE)) {
                int hits = itemCounterHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    itemCounterHits.put(attacker.getUniqueId(), 0);
                    attacker.sendMessage(ChatColor.LIGHT_PURPLE + "Item Counter scanned target inventory.");
                } else {
                    itemCounterHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "neutralizer"), PersistentDataType.BYTE)) {
                int hits = neutralizerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    neutralizerHits.put(attacker.getUniqueId(), 0);
                    attacker.sendMessage(ChatColor.GOLD + "Neutralizer activated!");
                } else {
                    neutralizerHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "clogger"), PersistentDataType.BYTE)) {
                int hits = cloggerHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    cloggerHits.put(attacker.getUniqueId(), 0);
                    for (int i = 0; i < target.getInventory().getSize(); i++) {
                        if (target.getInventory().getItem(i) == null) {
                            target.getInventory().setItem(i, new ItemStack(Material.WOODEN_SHOVEL));
                        }
                    }
                    attacker.sendMessage(ChatColor.GOLD + "Clogger activated!");
                } else {
                    cloggerHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "igloo"), PersistentDataType.BYTE)) {
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
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        for (Block b : iceBlocks) {
                            if (b.getType() == Material.PACKED_ICE) b.setType(Material.AIR);
                        }
                    }, 200L);
                    attacker.sendMessage(ChatColor.AQUA + "Igloo trapped target!");
                } else {
                    iglooHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "lock_in"), PersistentDataType.BYTE)) {
                int hits = lockInHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    lockInHits.put(attacker.getUniqueId(), 0);
                    lockedInTarget.put(attacker.getUniqueId(), target.getUniqueId());
                    attacker.sendMessage(ChatColor.RED + "🔒 Locked in 1v1 duel!");
                } else {
                    lockInHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "crafting_chaos"), PersistentDataType.BYTE)) {
                int hits = craftingChaosHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    craftingChaosHits.put(attacker.getUniqueId(), 0);
                    target.openWorkbench(null, true);
                    attacker.sendMessage(ChatColor.GOLD + "Crafting Chaos opened!");
                } else {
                    craftingChaosHits.put(attacker.getUniqueId(), hits);
                }
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "scrambler"), PersistentDataType.BYTE)) {
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
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "top_hat"), PersistentDataType.BYTE)) {
                int hits = topHatHits.getOrDefault(attacker.getUniqueId(), 0) + 1;
                if (hits >= 3) {
                    topHatHits.put(attacker.getUniqueId(), 0);
                    ItemStack oldHelmet = target.getInventory().getHelmet();
                    target.getInventory().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                    Bukkit.getScheduler().runTaskLater(this, () -> {
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

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.hasItemMeta() && !item.getItemMeta().getPersistentDataContainer().getKeys().isEmpty()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "🛑 Ability items cannot be placed!");
        }
    }
}
