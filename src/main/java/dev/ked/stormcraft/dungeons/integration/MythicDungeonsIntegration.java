package dev.ked.stormcraft.dungeons.integration;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Integration with MythicDungeons using reflection to avoid compile-time dependency.
 */
public class MythicDungeonsIntegration {

    private final JavaPlugin plugin;
    private boolean enabled = false;
    private Object mythicDungeonsAPI;

    public MythicDungeonsIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        try {
            // Try to get MythicDungeons API
            Class<?> mdClass = Class.forName("net.playavalon.mythicdungeons.MythicDungeons");
            Object mdInstance = mdClass.getMethod("inst").invoke(null);
            mythicDungeonsAPI = mdClass.getMethod("getAPI").invoke(mdInstance);

            enabled = true;
            plugin.getLogger().info("MythicDungeons API initialized successfully");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize MythicDungeons API: " + e.getMessage());
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if a player can enter a specific dungeon.
     */
    public boolean canEnterDungeon(Player player, String dungeonName) {
        if (!enabled) return false;

        try {
            Class<?> apiClass = mythicDungeonsAPI.getClass();
            Object result = apiClass.getMethod("canPlayerJoinDungeon", Player.class, String.class)
                                   .invoke(mythicDungeonsAPI, player, dungeonName);
            return (boolean) result;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check dungeon access for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Open the dungeon GUI for a player.
     */
    public void openDungeonGUI(Player player, String dungeonName) {
        if (!enabled) {
            player.sendMessage("§cMythicDungeons integration is not available!");
            return;
        }

        try {
            Class<?> apiClass = mythicDungeonsAPI.getClass();
            apiClass.getMethod("openDungeonGUI", Player.class, String.class)
                   .invoke(mythicDungeonsAPI, player, dungeonName);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to open dungeon GUI for " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§cFailed to open dungeon interface!");
        }
    }

    /**
     * Check if a dungeon exists.
     */
    public boolean dungeonExists(String dungeonName) {
        if (!enabled) return false;

        try {
            Class<?> apiClass = mythicDungeonsAPI.getClass();
            Object result = apiClass.getMethod("dungeonExists", String.class)
                                   .invoke(mythicDungeonsAPI, dungeonName);
            return (boolean) result;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check dungeon existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a player is currently in a dungeon.
     */
    public boolean isPlayerInDungeon(Player player) {
        if (!enabled) return false;

        try {
            Class<?> apiClass = mythicDungeonsAPI.getClass();
            Object result = apiClass.getMethod("isPlayerInDungeon", Player.class)
                                   .invoke(mythicDungeonsAPI, player);
            return (boolean) result;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check dungeon status for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a player has completed a specific dungeon before.
     */
    public boolean hasCompletedDungeon(Player player, String dungeonName) {
        if (!enabled) return false;

        try {
            Class<?> apiClass = mythicDungeonsAPI.getClass();
            Object result = apiClass.getMethod("hasPlayerFinishedDungeon", Player.class, String.class)
                                   .invoke(mythicDungeonsAPI, player, dungeonName);
            return (boolean) result;
        } catch (Exception e) {
            // Method might not exist, return false
            return false;
        }
    }

    /**
     * Get the number of times a player has completed a dungeon.
     */
    public int getCompletionCount(Player player, String dungeonName) {
        if (!enabled) return 0;

        try {
            Class<?> apiClass = mythicDungeonsAPI.getClass();
            Object result = apiClass.getMethod("getPlayerDungeonCompletions", Player.class, String.class)
                                   .invoke(mythicDungeonsAPI, player, dungeonName);
            return (int) result;
        } catch (Exception e) {
            // Method might not exist or player has no completions
            return 0;
        }
    }
}
