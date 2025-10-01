package dev.ked.stormcraft.dungeons.integration;

import dev.ked.stormcraft.StormcraftPlugin;
import dev.ked.stormcraft.model.TravelingStorm;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Integration with the Stormcraft plugin for storm detection and tracking.
 */
public class StormcraftIntegration {

    private final JavaPlugin plugin;
    private final StormcraftPlugin stormcraft;

    public StormcraftIntegration(JavaPlugin plugin, StormcraftPlugin stormcraft) {
        this.plugin = plugin;
        this.stormcraft = stormcraft;
    }

    /**
     * Get all currently active traveling storms.
     */
    public List<TravelingStorm> getActiveStorms() {
        return stormcraft.getStormManager().getActiveStorms();
    }

    /**
     * Get the intensity of a specific storm (0-100 scale).
     */
    public int getStormIntensity(TravelingStorm storm) {
        // Storm intensity is based on remaining time and power
        // For now, we'll use a simple calculation based on duration
        int remainingSeconds = storm.getRemainingSeconds();
        int totalDuration = storm.getOriginalDurationSeconds();

        // Storms are most intense in the middle, ramping up and down
        double progress = 1.0 - ((double) remainingSeconds / totalDuration);

        // Bell curve: intensity peaks at 50% progress
        double intensity;
        if (progress <= 0.5) {
            // Ramp up (0 -> 1)
            intensity = progress * 2.0;
        } else {
            // Ramp down (1 -> 0)
            intensity = 2.0 - (progress * 2.0);
        }

        return (int) (intensity * 100);
    }

    /**
     * Check if a player is near a storm (within specified radius).
     */
    public boolean isPlayerNearStorm(Player player, TravelingStorm storm, double maxDistance) {
        Location playerLoc = player.getLocation();
        Location stormLoc = storm.getCurrentLocation();

        // Check if in same world
        if (!playerLoc.getWorld().equals(stormLoc.getWorld())) {
            return false;
        }

        return playerLoc.distance(stormLoc) <= maxDistance;
    }

    /**
     * Find the nearest storm to a location.
     */
    public TravelingStorm getNearestStorm(Location location) {
        List<TravelingStorm> storms = getActiveStorms();
        if (storms.isEmpty()) {
            return null;
        }

        TravelingStorm nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (TravelingStorm storm : storms) {
            Location stormLoc = storm.getCurrentLocation();
            if (!stormLoc.getWorld().equals(location.getWorld())) {
                continue;
            }

            double distance = location.distance(stormLoc);
            if (distance < nearestDistance) {
                nearest = storm;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    /**
     * Check if there are any storms meeting minimum intensity requirements.
     */
    public boolean hasQualifyingStorm(int minIntensity) {
        for (TravelingStorm storm : getActiveStorms()) {
            if (getStormIntensity(storm) >= minIntensity) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a safe spawn location near the storm epicenter.
     */
    public Location getSafeSpawnNearStorm(TravelingStorm storm, double minDistance, double maxDistance) {
        Location center = storm.getCurrentLocation();

        // Try to find a safe location within the range
        for (int attempt = 0; attempt < 10; attempt++) {
            // Random angle
            double angle = Math.random() * 2 * Math.PI;
            // Random distance between min and max
            double distance = minDistance + (Math.random() * (maxDistance - minDistance));

            // Calculate offset
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;

            Location candidate = center.clone().add(x, 0, z);

            // Find highest block at this location
            candidate.setY(candidate.getWorld().getHighestBlockYAt(candidate) + 1);

            // Check if location is safe (not in water, lava, etc.)
            if (isSafeLocation(candidate)) {
                return candidate;
            }
        }

        // Fallback: just use storm center at ground level
        Location fallback = center.clone();
        fallback.setY(fallback.getWorld().getHighestBlockYAt(fallback) + 1);
        return fallback;
    }

    private boolean isSafeLocation(Location loc) {
        // Check block below is solid
        if (!loc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
            return false;
        }

        // Check location and above are air
        if (!loc.getBlock().getType().isAir() || !loc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }

        return true;
    }
}
