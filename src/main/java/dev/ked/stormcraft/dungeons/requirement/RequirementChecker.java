package dev.ked.stormcraft.dungeons.requirement;

import dev.ked.stormcraft.dungeons.config.ConfigManager;
import dev.ked.stormcraft.dungeons.data.DataManager;
import dev.ked.stormcraft.dungeons.integration.EssenceIntegration;
import dev.ked.stormcraft.dungeons.integration.StormcraftIntegration;
import dev.ked.stormcraft.dungeons.portal.Portal;
import dev.ked.stormcraft.dungeons.requirement.requirements.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if players meet dungeon entry requirements.
 */
public class RequirementChecker {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final StormcraftIntegration stormcraftIntegration;
    private final EssenceIntegration essenceIntegration;
    private final DataManager dataManager;
    private final Economy economy;

    public RequirementChecker(JavaPlugin plugin, ConfigManager configManager,
                             StormcraftIntegration stormcraftIntegration,
                             EssenceIntegration essenceIntegration,
                             DataManager dataManager,
                             Economy economy) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.stormcraftIntegration = stormcraftIntegration;
        this.essenceIntegration = essenceIntegration;
        this.dataManager = dataManager;
        this.economy = economy;
    }

    /**
     * Check all requirements for a player entering a dungeon.
     * Returns null if all requirements pass, otherwise returns list of failure messages.
     */
    public List<String> checkRequirements(Player player, Portal portal) {
        String dungeonName = portal.getDungeonName();
        List<Requirement> requirements = buildRequirements(dungeonName, portal);

        List<String> failures = new ArrayList<>();

        for (Requirement requirement : requirements) {
            if (!requirement.check(player)) {
                failures.add(requirement.getFailureMessage(player));
            }
        }

        return failures.isEmpty() ? null : failures;
    }

    /**
     * Build all requirements for a specific dungeon.
     */
    private List<Requirement> buildRequirements(String dungeonName, Portal portal) {
        List<Requirement> requirements = new ArrayList<>();
        String basePath = "dungeons." + dungeonName + ".requirements";

        // SEL Requirement
        if (configManager.getConfig().contains(basePath + ".min_sel")) {
            int minSEL = configManager.getConfig().getInt(basePath + ".min_sel");
            requirements.add(new SELRequirement(essenceIntegration, minSEL));
        }

        // Essence Cost Requirement
        if (configManager.getConfig().contains(basePath + ".essence_cost")) {
            int essenceCost = configManager.getConfig().getInt(basePath + ".essence_cost");
            requirements.add(new EssenceRequirement(essenceIntegration, essenceCost));
        }

        // Storm Proximity Requirement
        if (configManager.getConfig().contains(basePath + ".max_distance_from_storm")) {
            int maxDistance = configManager.getConfig().getInt(basePath + ".max_distance_from_storm");
            int minIntensity = configManager.getConfig().getInt(basePath + ".min_storm_intensity", 0);
            requirements.add(new StormRequirement(stormcraftIntegration, portal.getStorm(), maxDistance, minIntensity));
        }

        // Completion Requirements
        if (configManager.getConfig().contains(basePath + ".required_completions")) {
            var requiredCompletions = configManager.getConfig().getConfigurationSection(basePath + ".required_completions");
            if (requiredCompletions != null) {
                for (String requiredDungeon : requiredCompletions.getKeys(false)) {
                    int count = requiredCompletions.getInt(requiredDungeon);
                    requirements.add(new CompletionRequirement(dataManager, requiredDungeon, count));
                }
            }
        }

        // Permission Requirement
        if (configManager.getConfig().contains(basePath + ".permission")) {
            String permission = configManager.getConfig().getString(basePath + ".permission");
            requirements.add(new PermissionRequirement(permission));
        }

        return requirements;
    }

    /**
     * Consume entry costs (essence) from the player.
     */
    public boolean consumeEntryCosts(Player player, String dungeonName) {
        String basePath = "dungeons." + dungeonName + ".requirements";

        // Withdraw essence cost
        if (configManager.getConfig().contains(basePath + ".essence_cost")) {
            int essenceCost = configManager.getConfig().getInt(basePath + ".essence_cost");
            if (essenceIntegration != null && essenceIntegration.isEnabled()) {
                if (!essenceIntegration.withdrawEssence(player, essenceCost)) {
                    return false;
                }
            }
        }

        return true;
    }
}
