package dev.ked.stormcraft.dungeons.reward;

import dev.ked.stormcraft.dungeons.config.ConfigManager;
import dev.ked.stormcraft.dungeons.data.DataManager;
import dev.ked.stormcraft.dungeons.integration.EssenceIntegration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages reward distribution for dungeon completions.
 */
public class RewardManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final EssenceIntegration essenceIntegration;
    private final DataManager dataManager;
    private final Economy economy;

    public RewardManager(JavaPlugin plugin, ConfigManager configManager,
                        EssenceIntegration essenceIntegration,
                        DataManager dataManager,
                        Economy economy) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.essenceIntegration = essenceIntegration;
        this.dataManager = dataManager;
        this.economy = economy;
    }

    /**
     * Award rewards to a player for completing a dungeon.
     */
    public void awardCompletion(Player player, String dungeonName) {
        boolean firstCompletion = dataManager.isFirstCompletion(player.getUniqueId(), dungeonName);

        // Calculate essence reward
        double essenceReward = calculateEssenceReward(player, dungeonName, firstCompletion);

        // Award essence
        if (essenceIntegration != null && essenceIntegration.isEnabled() && essenceReward > 0) {
            essenceIntegration.awardEssence(player, essenceReward);
            player.sendMessage(String.format("§a+ %.0f Essence", essenceReward));

            if (firstCompletion) {
                double bonusAmount = essenceReward * getBonusMultiplier(dungeonName);
                if (bonusAmount > 0) {
                    player.sendMessage(String.format("§e+ %.0f Essence §7(First Completion Bonus!)", bonusAmount));
                }
            }
        }

        // Track completion
        dataManager.incrementCompletion(player.getUniqueId(), dungeonName);

        // Completion message
        String dungeonDisplayName = configManager.getConfig().getString(
            "dungeons." + dungeonName + ".display_name", dungeonName
        );
        player.sendMessage(String.format("§a§l✓ %s Complete! §a%.0f essence earned",
                                        dungeonDisplayName, essenceReward));
    }

    /**
     * Calculate essence reward for completing a dungeon.
     */
    private double calculateEssenceReward(Player player, String dungeonName, boolean firstCompletion) {
        String basePath = "dungeons." + dungeonName + ".rewards";

        // Base reward
        double baseReward = configManager.getConfig().getDouble(basePath + ".essence_base", 0);

        // Add variance
        double variance = configManager.getConfig().getDouble(basePath + ".essence_variance", 0);
        double actualReward = baseReward + (Math.random() * variance * 2) - variance;

        // Apply first completion bonus
        if (firstCompletion) {
            double bonusMultiplier = getBonusMultiplier(dungeonName);
            actualReward += (baseReward * bonusMultiplier);
        }

        return Math.max(0, actualReward);
    }

    /**
     * Get the completion bonus multiplier for a dungeon.
     */
    private double getBonusMultiplier(String dungeonName) {
        return configManager.getConfig().getDouble(
            "dungeons." + dungeonName + ".rewards.completion_bonus", 0.0
        );
    }
}
