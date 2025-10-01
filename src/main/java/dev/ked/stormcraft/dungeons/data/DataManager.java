package dev.ked.stormcraft.dungeons.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player completion data persistence.
 */
public class DataManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    // Cache: UUID -> (DungeonName -> CompletionCount)
    private final Map<UUID, Map<String, Integer>> completionData;

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.completionData = new HashMap<>();
    }

    /**
     * Load player data from file.
     */
    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create playerdata.yml: " + e.getMessage());
                return;
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load into cache
        if (dataConfig.contains("completions")) {
            var completionsSection = dataConfig.getConfigurationSection("completions");
            if (completionsSection != null) {
                for (String uuidStr : completionsSection.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        Map<String, Integer> dungeonCounts = new HashMap<>();

                        var playerSection = completionsSection.getConfigurationSection(uuidStr);
                        if (playerSection != null) {
                            for (String dungeonName : playerSection.getKeys(false)) {
                                int count = playerSection.getInt(dungeonName, 0);
                                dungeonCounts.put(dungeonName, count);
                            }
                        }

                        completionData.put(uuid, dungeonCounts);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in playerdata: " + uuidStr);
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded completion data for " + completionData.size() + " players");
    }

    /**
     * Save player data to file.
     */
    public void saveData() {
        dataConfig.set("completions", null); // Clear existing

        for (Map.Entry<UUID, Map<String, Integer>> entry : completionData.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Integer> dungeonEntry : entry.getValue().entrySet()) {
                dataConfig.set("completions." + uuidStr + "." + dungeonEntry.getKey(), dungeonEntry.getValue());
            }
        }

        try {
            dataConfig.save(dataFile);
            plugin.getLogger().info("Saved completion data for " + completionData.size() + " players");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save playerdata.yml: " + e.getMessage());
        }
    }

    /**
     * Get the number of times a player has completed a dungeon.
     */
    public int getCompletionCount(UUID playerId, String dungeonName) {
        return completionData.getOrDefault(playerId, new HashMap<>()).getOrDefault(dungeonName, 0);
    }

    /**
     * Increment a player's completion count for a dungeon.
     */
    public void incrementCompletion(UUID playerId, String dungeonName) {
        completionData.putIfAbsent(playerId, new HashMap<>());
        Map<String, Integer> dungeonCounts = completionData.get(playerId);
        dungeonCounts.put(dungeonName, dungeonCounts.getOrDefault(dungeonName, 0) + 1);
    }

    /**
     * Get all completion data for a player.
     */
    public Map<String, Integer> getPlayerCompletions(UUID playerId) {
        return new HashMap<>(completionData.getOrDefault(playerId, new HashMap<>()));
    }

    /**
     * Check if this is a player's first completion of a dungeon.
     */
    public boolean isFirstCompletion(UUID playerId, String dungeonName) {
        return getCompletionCount(playerId, dungeonName) == 0;
    }
}
