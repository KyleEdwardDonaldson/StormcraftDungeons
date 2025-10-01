package dev.ked.stormcraft.dungeons.portal;

import dev.ked.stormcraft.dungeons.config.ConfigManager;
import dev.ked.stormcraft.dungeons.integration.StormcraftIntegration;
import dev.ked.stormcraft.dungeons.requirement.RequirementChecker;
import dev.ked.stormcraft.model.TravelingStorm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages dungeon portal spawning, rendering, and lifecycle.
 */
public class PortalManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final StormcraftIntegration stormcraftIntegration;
    private final RequirementChecker requirementChecker;

    private final Map<UUID, Portal> activePortals;
    private final Set<String> portaledStorms; // Track which storms already have portals

    private BukkitTask spawnTask;
    private BukkitTask particleTask;
    private BukkitTask cleanupTask;

    public PortalManager(JavaPlugin plugin, ConfigManager configManager,
                        StormcraftIntegration stormcraftIntegration,
                        RequirementChecker requirementChecker) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.stormcraftIntegration = stormcraftIntegration;
        this.requirementChecker = requirementChecker;
        this.activePortals = new ConcurrentHashMap<>();
        this.portaledStorms = ConcurrentHashMap.newKeySet();
    }

    /**
     * Start the portal manager tasks.
     */
    public void start() {
        int checkInterval = configManager.getConfig().getInt("portals.check_interval", 100);

        // Portal spawning task (every 5 seconds by default)
        spawnTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkForPortalSpawns, 20L, checkInterval);

        // Particle rendering task (every 20 ticks / 1 second)
        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::renderAllPortals, 20L, 20L);

        // Cleanup task (every 30 seconds)
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupInvalidPortals, 20L, 600L);

        plugin.getLogger().info("Portal manager started");
    }

    /**
     * Shutdown the portal manager.
     */
    public void shutdown() {
        if (spawnTask != null) spawnTask.cancel();
        if (particleTask != null) particleTask.cancel();
        if (cleanupTask != null) cleanupTask.cancel();

        // Remove all portals
        for (Portal portal : new ArrayList<>(activePortals.values())) {
            removePortal(portal);
        }

        plugin.getLogger().info("Portal manager stopped");
    }

    /**
     * Check if any storms qualify for portal spawning.
     */
    private void checkForPortalSpawns() {
        List<TravelingStorm> storms = stormcraftIntegration.getActiveStorms();
        int maxPortals = configManager.getConfig().getInt("storms.max_portals", 5);

        // Don't spawn more portals if at max
        if (activePortals.size() >= maxPortals) {
            return;
        }

        for (TravelingStorm storm : storms) {
            // Skip if this storm already has a portal
            String stormId = getStormId(storm);
            if (portaledStorms.contains(stormId)) {
                continue;
            }

            // Check each dungeon type
            for (String dungeonKey : getDungeonKeys()) {
                if (shouldSpawnPortal(storm, dungeonKey)) {
                    spawnPortal(storm, dungeonKey);
                    break; // Only one portal per storm
                }
            }
        }
    }

    /**
     * Check if a portal should spawn for this storm and dungeon.
     */
    private boolean shouldSpawnPortal(TravelingStorm storm, String dungeonKey) {
        String basePath = "dungeons." + dungeonKey;

        // Check if dungeon is enabled
        if (!configManager.getConfig().getBoolean(basePath + ".enabled", false)) {
            return false;
        }

        // Check if portal spawning is enabled for this dungeon
        if (!configManager.getConfig().getBoolean(basePath + ".portal.enabled", true)) {
            return false;
        }

        // Check storm intensity requirement
        int minIntensity = configManager.getConfig().getInt(basePath + ".requirements.min_storm_intensity", 40);
        if (stormcraftIntegration.getStormIntensity(storm) < minIntensity) {
            return false;
        }

        // Roll spawn chance
        double spawnChance = configManager.getConfig().getDouble(basePath + ".portal.spawn_chance", 0.5);
        return Math.random() < spawnChance;
    }

    /**
     * Spawn a portal for a dungeon near a storm.
     */
    public void spawnPortal(TravelingStorm storm, String dungeonName) {
        // Find safe spawn location
        Location spawnLoc = stormcraftIntegration.getSafeSpawnNearStorm(storm, 50, 150);

        // Create portal
        Portal portal = new Portal(dungeonName, storm, spawnLoc);
        portal.spawn();

        // Track portal
        activePortals.put(portal.getId(), portal);
        portaledStorms.add(getStormId(storm));

        // Announce to nearby players
        announcePortalSpawn(portal);

        plugin.getLogger().info("Spawned " + dungeonName + " portal at " + formatLocation(spawnLoc));
    }

    /**
     * Remove a portal.
     */
    public void removePortal(Portal portal) {
        portal.remove();
        activePortals.remove(portal.getId());
        portaledStorms.remove(getStormId(portal.getStorm()));

        plugin.getLogger().info("Removed " + portal.getDungeonName() + " portal");
    }

    /**
     * Render particles for all active portals.
     */
    private void renderAllPortals() {
        for (Portal portal : activePortals.values()) {
            portal.renderParticles();
        }
    }

    /**
     * Clean up invalid or expired portals.
     */
    private void cleanupInvalidPortals() {
        List<Portal> toRemove = new ArrayList<>();

        for (Portal portal : activePortals.values()) {
            if (!portal.isValid()) {
                toRemove.add(portal);
            }
        }

        for (Portal portal : toRemove) {
            removePortal(portal);
        }
    }

    /**
     * Announce portal spawn to nearby players.
     */
    private void announcePortalSpawn(Portal portal) {
        String dungeonDisplayName = configManager.getConfig().getString(
            "dungeons." + portal.getDungeonName() + ".display_name",
            portal.getDungeonName()
        );

        String message = "§6[Dungeons] §fA " + dungeonDisplayName + " §fportal has opened near the storm!";

        // Announce to players within 300 blocks
        Location portalLoc = portal.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(portalLoc.getWorld()) &&
                player.getLocation().distance(portalLoc) <= 300) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Get the portal at a specific location, if any.
     */
    public Portal getPortalAtLocation(Location location) {
        for (Portal portal : activePortals.values()) {
            if (portal.isPortalBlock(location)) {
                return portal;
            }
        }
        return null;
    }

    /**
     * Get all active portals.
     */
    public Collection<Portal> getActivePortals() {
        return activePortals.values();
    }

    /**
     * Get the nearest portal to a location.
     */
    public Portal getNearestPortal(Location location) {
        Portal nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Portal portal : activePortals.values()) {
            if (!portal.getLocation().getWorld().equals(location.getWorld())) {
                continue;
            }

            double distance = portal.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearest = portal;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    /**
     * Remove all portals (for admin command).
     */
    public void clearAllPortals() {
        for (Portal portal : new ArrayList<>(activePortals.values())) {
            removePortal(portal);
        }
    }

    // Helper methods
    private String getStormId(TravelingStorm storm) {
        return storm.getCurrentLocation().getWorld().getName() + "_" + storm.hashCode();
    }

    private List<String> getDungeonKeys() {
        if (configManager.getConfig().getConfigurationSection("dungeons") == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configManager.getConfig().getConfigurationSection("dungeons").getKeys(false));
    }

    private String formatLocation(Location loc) {
        return String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
