package dev.ked.stormcraft.dungeons.listener;

import dev.ked.stormcraft.dungeons.StormcraftDungeonsPlugin;
import dev.ked.stormcraft.dungeons.reward.RewardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listens for MythicDungeons events using reflection to avoid compile-time dependency.
 */
public class DungeonEventListener implements Listener {

    private final StormcraftDungeonsPlugin plugin;
    private final RewardManager rewardManager;

    public DungeonEventListener(StormcraftDungeonsPlugin plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
    }

    /**
     * Listen for dungeon completion using reflection.
     * MythicDungeons fires events like DungeonCompleteEvent or similar.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDungeonComplete(org.bukkit.event.Event event) {
        // Check if this is a MythicDungeons completion event
        String eventName = event.getEventName();

        if (!eventName.contains("DungeonComplete") &&
            !eventName.contains("DungeonFinish") &&
            !eventName.contains("InstanceComplete")) {
            return; // Not a dungeon completion event
        }

        try {
            // Use reflection to get dungeon name and players
            Class<?> eventClass = event.getClass();

            // Try to get dungeon name
            String dungeonName = null;
            try {
                Object dungeon = eventClass.getMethod("getDungeon").invoke(event);
                dungeonName = (String) dungeon.getClass().getMethod("getName").invoke(dungeon);
            } catch (Exception e) {
                // Try alternate method
                try {
                    dungeonName = (String) eventClass.getMethod("getDungeonName").invoke(event);
                } catch (Exception e2) {
                    plugin.getLogger().warning("Failed to get dungeon name from completion event");
                    return;
                }
            }

            // Try to get players
            Object playersObj = null;
            try {
                playersObj = eventClass.getMethod("getPlayers").invoke(event);
            } catch (Exception e) {
                // Try alternate method
                try {
                    playersObj = eventClass.getMethod("getPartyMembers").invoke(event);
                } catch (Exception e2) {
                    plugin.getLogger().warning("Failed to get players from completion event");
                    return;
                }
            }

            // Award rewards to all players
            if (playersObj instanceof Iterable) {
                for (Object playerObj : (Iterable<?>) playersObj) {
                    if (playerObj instanceof Player) {
                        Player player = (Player) playerObj;
                        awardPlayerRewards(player, dungeonName);
                    }
                }
            } else if (playersObj instanceof Player) {
                Player player = (Player) playersObj;
                awardPlayerRewards(player, dungeonName);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error processing dungeon completion event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Award rewards to a player for completing a dungeon.
     */
    private void awardPlayerRewards(Player player, String dungeonName) {
        // Normalize dungeon name (remove spaces, lowercase)
        String normalizedName = dungeonName.toLowerCase().replace(" ", "_");

        // Check if this is one of our configured dungeons
        if (!isDungeonConfigured(normalizedName)) {
            plugin.getLogger().info("Dungeon '" + dungeonName + "' completed but not configured for rewards");
            return;
        }

        // Award rewards
        rewardManager.awardCompletion(player, normalizedName);

        plugin.getLogger().info(player.getName() + " completed " + dungeonName + " - rewards awarded");
    }

    /**
     * Check if a dungeon is configured in our config.
     */
    private boolean isDungeonConfigured(String dungeonName) {
        return plugin.getConfigManager().getConfig().contains("dungeons." + dungeonName);
    }
}
