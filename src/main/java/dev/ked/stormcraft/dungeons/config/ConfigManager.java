package dev.ked.stormcraft.dungeons.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages plugin configuration.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load all configuration files.
     */
    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        plugin.getLogger().info("Configuration loaded");
    }

    /**
     * Reload all configuration files.
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        plugin.getLogger().info("Configuration reloaded");
    }

    /**
     * Get the main config.yml.
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
