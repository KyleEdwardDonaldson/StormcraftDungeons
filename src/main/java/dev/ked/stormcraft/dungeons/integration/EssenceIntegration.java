package dev.ked.stormcraft.dungeons.integration;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Integration with Stormcraft-Essence for SEL and essence economy.
 */
public class EssenceIntegration {

    private final JavaPlugin plugin;
    private boolean enabled = false;
    private Object essenceAPI;

    public EssenceIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        try {
            // Try to get Stormcraft-Essence API
            Class<?> essencePluginClass = Class.forName("dev.ked.stormcraft.essence.StormcraftEssencePlugin");
            Object essencePlugin = plugin.getServer().getPluginManager().getPlugin("Stormcraft-Essence");

            if (essencePlugin != null) {
                essenceAPI = essencePluginClass.getMethod("getAPI").invoke(essencePlugin);
                enabled = true;
                plugin.getLogger().info("Stormcraft-Essence API initialized successfully");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize Stormcraft-Essence API: " + e.getMessage());
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get a player's Storm Exposure Level (SEL).
     */
    public int getPlayerSEL(Player player) {
        if (!enabled) return 0;

        try {
            Class<?> apiClass = essenceAPI.getClass();
            Object result = apiClass.getMethod("getPlayerSEL", Player.class)
                                   .invoke(essenceAPI, player);
            return (int) result;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get SEL for " + player.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get a player's essence balance.
     */
    public double getPlayerEssence(Player player) {
        if (!enabled) return 0;

        try {
            Class<?> apiClass = essenceAPI.getClass();
            Object result = apiClass.getMethod("getPlayerEssence", Player.class)
                                   .invoke(essenceAPI, player);
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get essence for " + player.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Withdraw essence from a player's balance.
     */
    public boolean withdrawEssence(Player player, double amount) {
        if (!enabled) return false;

        try {
            Class<?> apiClass = essenceAPI.getClass();
            Object result = apiClass.getMethod("withdrawEssence", Player.class, double.class)
                                   .invoke(essenceAPI, player, amount);
            return (boolean) result;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to withdraw essence from " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Award essence to a player.
     */
    public void awardEssence(Player player, double amount) {
        if (!enabled) return;

        try {
            Class<?> apiClass = essenceAPI.getClass();
            apiClass.getMethod("depositEssence", Player.class, double.class)
                   .invoke(essenceAPI, player, amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to award essence to " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Check if a player has at least the specified SEL.
     */
    public boolean hasMinimumSEL(Player player, int minSEL) {
        return getPlayerSEL(player) >= minSEL;
    }

    /**
     * Check if a player has at least the specified essence amount.
     */
    public boolean hasEssence(Player player, double amount) {
        return getPlayerEssence(player) >= amount;
    }
}
