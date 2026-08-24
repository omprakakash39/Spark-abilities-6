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

    // Tracking maps
    private final Map<UUID, Long> antiAbilityJammed = new HashMap<>();
    private final Map<UUID, Long> xpJammedPlayers = new HashMap<>();
    private final Map<UUID, Long> stickyFingersJammed = new HashMap<>();
    private final Map<UUID, Long> berserkActive = new HashMap<>();
    private final Map<UUID, Long> kothCooldown = new HashMap<>();
    private final Map<UUID, UUID> lockedInTarget = new HashMap<>();

    // Hit counters
    private final Map<UUID, Integer> effectDisablerHits = new HashMap<>();
    private final Map<UUID, Integer> stickyFingersHits = new HashMap<>();
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
        getLogger().info("SparkAbilities plugin enabled successfully with professional lores & updates!");
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

        // 1. Effect Disabler
        gui.setItem(0, createGlowItem(Material.CLOCK, 1, ChatColor.YELLOW + "Effect Disabler", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Hit a player to cleanly strip away", ChatColor.GRAY + "all active potion effects for 10 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "effect_disabler"));

        // 2. Sticky Fingers
        gui.setItem(1, createGlowItem(Material.HONEYCOMB, 1, ChatColor.GOLD + "Sticky Fingers", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Hit a player to paralyze their hands,", ChatColor.GRAY + "disabling item pickup and drop for 15 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "sticky_fingers"));

        // 3. Item Counter
        gui.setItem(2, createGlowItem(Material.ITEM_FRAME, 1, ChatColor.LIGHT_PURPLE + "Item Counter", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Strike an opponent 3 times to inspect", ChatColor.GRAY + "their inventory for gapples, abilities, and XP.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "item_counter"));

        // 4. Throwable Web
        gui.setItem(3, createGlowItem(Material.COBWEB, 1, ChatColor.AQUA + "Throwable Web", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Throw a dense web projectile that erupts", ChatColor.GRAY + "into a 3x3 web trap for 8 seconds upon impact.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "throwable_web"));

        // 5. Neutralizer
        gui.setItem(4, createGlowItem(Material.BLAZE_ROD, 1, ChatColor.GOLD + "Neutralizer", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Land 3 strikes to temporarily downgrade", ChatColor.GRAY + "their armor protection tier for 8 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "neutralizer"));

        // 6. Berserk
        gui.setItem(5, createGlowItem(Material.MAGMA_CREAM, 1, ChatColor.RED + "Berserk", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Channel primal fury to gain Strength III", ChatColor.GRAY + "and Speed III for 10 seconds. Golden apples are blocked!", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "berserk"));

        // 7. Anti-Ability Ball
        gui.setItem(6, createGlowItem(Material.SLIME_BALL, 1, ChatColor.LIGHT_PURPLE + "Anti-Ability Ball", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Disrupt enemies in a 12-block radius,", lockLore("preventing them from using abilities for 10 seconds."), ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "anti_ability_ball"));

        // 8. XP Jammer
        gui.setItem(7, createGlowItem(Material.COMMAND_BLOCK, 1, ChatColor.LIGHT_PURPLE + "XP Jammer", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Suppress nearby enemies within 12 blocks,", ChatColor.GRAY + "stopping XP usage or throws for 15 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "xp_jammer"));

        // 9. Teleport Bow
        gui.setItem(8, createGlowItem(Material.BOW, 1, ChatColor.LIGHT_PURPLE + "Teleport Bow", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Snipe a player with this mystical arrow", ChatColor.GRAY + "to instantly teleport directly to them (Warzone only).", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "teleport_bow"));

        // 10. KOTH Starter
        gui.setItem(9, createGlowItem(Material.LANTERN, 1, ChatColor.LIGHT_PURPLE + "KOTH Starter", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Initiate a server-wide random KOTH event.", ChatColor.GRAY + "Subject to a 25-minute global cooldown.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "koth_starter"));

        // 11. Clogger
        gui.setItem(10, createGlowItem(Material.WOODEN_SHOVEL, 1, ChatColor.GOLD + "Clogger", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.BLUE + "+1 Attack Damage", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Hit an enemy 3 times to flood their empty", ChatColor.GRAY + "inventory slots with wooden shovels for 15 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "clogger"));

        // 12. Focus Mode
        gui.setItem(11, createGlowItem(Material.SPYGLASS, 1, ChatColor.LIGHT_PURPLE + "Focus Mode", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Lock in your concentration to deal 10% extra", ChatColor.GRAY + "increased damage against all enemies for 60 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "focus_mode"));

        // 13. Stun Gun
        gui.setItem(12, createGlowItem(Material.DIAMOND_PICKAXE, 1, ChatColor.LIGHT_PURPLE + "Stun Gun", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.BLUE + "+5 Attack Damage", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Fire a specialized stun projectile. Anyone struck", ChatColor.GRAY + "is paralyzed from moving or jumping for 8 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "stun_gun"));

        // 14. Igloo (NEW)
        gui.setItem(13, createGlowItem(Material.PACKED_ICE, 1, ChatColor.AQUA + "Igloo", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Strike a player 3 times to encase them", ChatColor.GRAY + "inside a packed ice hemisphere dome for 10 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "igloo"));

        // 15. Lock In (NEW)
        gui.setItem(14, createGlowItem(Material.REDSTONE, 1, ChatColor.RED + "Lock In", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Strike a player 3 times to lock a 1v1 duel state,", ChatColor.GRAY + "preventing you from damaging anyone else.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "lock_in"));

        // 16. Crafting Chaos (NEW)
        gui.setItem(15, createGlowItem(Material.CRAFTING_TABLE, 1, ChatColor.GOLD + "Crafting Chaos", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Hit an enemy 3 times to forcefully force-open", ChatColor.GRAY + "their crafting menu interface mid-combat.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "crafting_chaos"));

        // 17. Scrambler (NEW)
        gui.setItem(16, createGlowItem(Material.NETHER_STAR, 1, ChatColor.LIGHT_PURPLE + "Scrambler", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Strike a target 3 times to completely scramble", ChatColor.GRAY + "and randomize their inventory layout slots.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "scrambler"));

        // 18. Top Hat (NEW)
        gui.setItem(17, createGlowItem(Material.GOLDEN_HELMET, 1, ChatColor.GOLD + "Top Hat", 
                List.of(ChatColor.GRAY + "Unbreaking I", ChatColor.DARK_GRAY + "-------------------", ChatColor.GRAY + "Hit a player 3 times to forcibly replace", ChatColor.GRAY + "their helmet with a Golden Helmet for 8 seconds.", ChatColor.DARK_GRAY + "-------------------", ChatColor.YELLOW + "✦ Click to execute ability"), "top_hat"));

        // --- POTIONS (No Glow, Standard Items) ---
        // 19. Hulk Potion
        ItemStack hulk = new ItemStack(Material.POTION);
        ItemMeta hm = hulk.getItemMeta();
        hm.setDisplayName(ChatColor.BLUE + "Hulk Potion");
        hm.setLore(List.of(ChatColor.BLUE + "Strength III (00:15)", ChatColor.GRAY + "Unleash raw power and become unstoppable."));
        hm.getPersistentDataContainer().set(new NamespacedKey(this, "hulk_potion"), PersistentDataType.BYTE, (byte) 1);
        hulk.setItemMeta(hm);
        gui.setItem(18, hulk);

        // 20. Death's Touch Potion
        ItemStack death = new ItemStack(Material.SPLASH_POTION);
        ItemMeta dm = death.getItemMeta();
        dm.setDisplayName(ChatColor.BLUE + "Death's Touch Potion");
        dm.setLore(List.of(ChatColor.RED + "Instant Damage III", ChatColor.GRAY + "Deal heavy irreversible harm on impact."));
        dm.getPersistentDataContainer().set(new NamespacedKey(this, "death_potion"), PersistentDataType.BYTE, (byte) 1);
        death.setItemMeta(dm);
        gui.setItem(19, death);

        // 21. Elixir of Life Potion
        ItemStack elixir = new ItemStack(Material.SPLASH_POTION);
        ItemMeta em = elixir.getItemMeta();
        em.setDisplayName(ChatColor.BLUE + "Elixir of Life Potion");
        em.setLore(List.of(ChatColor.GREEN + "Instant Health (Potency IX)"));
        em.getPersistentDataContainer().set(new NamespacedKey(this, "elixir_potion"), PersistentDataType.BYTE, (byte) 1);
        elixir.setItemMeta(em);
        gui.setItem(20, elixir);

        // 22. Escape Potion (NEW)
        ItemStack escape = new ItemStack(Material.POTION);
        ItemMeta esm = escape.getItemMeta();
        esm.setDisplayName(ChatColor.LIGHT_PURPLE + "Escape Potion");
        esm.setLore(List.of(ChatColor.GREEN + "Speed IV, Absorption IV,", ChatColor.GREEN + "Resistance IV, Weakness IV (03:00)", ChatColor.GRAY + "The ultimate survival concoction for escape."));
        esm.getPersistentDataContainer().set(new NamespacedKey(this, "escape_potion"), PersistentDataType.BYTE, (byte) 1);
        escape.setItemMeta(esm);
        gui.setItem(21, escape);

        player.openInventory(gui);
        return true;
    }

    private String lockLore(String text) {
        return ChatColor.GRAY + text;
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
                        player.sendMessage(ChatColor.RED + "🛑 Anti-Ability Ball restriction active! You cannot use abilities.");
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
                player.sendMessage(ChatColor.RED + "⚡ Berserk activated! Strength III & Speed III for 10 seconds.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "anti_ability_ball"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                int count = 0;
                for (Player p : player.getWorld().getPlayers()) {
                    if (p != player && p.getLocation().distanceSquared(player.getLocation()) <= 144) {
                        antiAbilityJammed.put(p.getUniqueId(), System.currentTimeMillis() + 10000);
                        p.sendMessage(ChatColor.RED + "⚠️ You have been silenced by an Anti-Ability Ball for 10 seconds!");
                        count++;
                    }
                }
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Anti-Ability Ball deployed on " + count + " nearby targets.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "xp_jammer"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                int count = 0;
                for (Player p : player.getWorld().getPlayers()) {
                    if (p != player && p.getLocation().distanceSquared(player.getLocation()) <= 144) {
                        xpJammedPlayers.put(p.getUniqueId(), System.currentTimeMillis() + 15000);
                        p.sendMessage(ChatColor.RED + "⚠️ You are XP Jammed for 15 seconds!");
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
                player.sendMessage(ChatColor.AQUA + "Throwable Web projectile launched!");
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
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Focus Mode active! +10% damage boost via strength for 60 seconds.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "koth_starter"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                long now = System.currentTimeMillis();
                if (kothCooldown.containsKey(player.getUniqueId()) && now < kothCooldown.get(player.getUniqueId())) {
                    long remaining = (kothCooldown.get(player.getUniqueId()) - now) / 1000;
                    player.sendMessage(ChatColor.RED + "⏳ KOTH Starter is on cooldown! Wait " + remaining + "s.");
                    return;
                }
                kothCooldown.put(player.getUniqueId(), now + 1500000L);
                Bukkit.broadcastMessage(ChatColor.GOLD + "⚔️ A random KOTH event has been initialized by " + player.getName() + "!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "hulk_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 2));
                player.sendMessage(ChatColor.BLUE + "Hulk Potion consumed! Strength III for 15 seconds.");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "death_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.damage(6.0);
                player.sendMessage(ChatColor.RED + "Death's Touch Potion recoil triggered!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "elixir_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 9));
                player.sendMessage(ChatColor.GREEN + "Elixir of Life Potion applied instantly!");
            }
            else if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "escape_potion"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                item.setAmount(item.getAmount() - 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3600, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 3600, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 3600, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 3600, 3));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Escape Potion consumed! Speed IV, Absorption IV, Resistance IV, Weakness IV active for 3 minutes.");
            }
        }
    }
