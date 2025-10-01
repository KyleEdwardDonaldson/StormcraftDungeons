package dev.ked.stormcraft.dungeons.requirement;

import org.bukkit.entity.Player;

/**
 * Base interface for dungeon entry requirements.
 */
public interface Requirement {

    /**
     * Check if the player meets this requirement.
     */
    boolean check(Player player);

    /**
     * Get the failure message to show the player.
     */
    String getFailureMessage(Player player);

    /**
     * Get the requirement name.
     */
    String getName();
}
