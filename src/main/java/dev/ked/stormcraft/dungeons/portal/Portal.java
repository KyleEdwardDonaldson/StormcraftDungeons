package dev.ked.stormcraft.dungeons.portal;

import dev.ked.stormcraft.model.TravelingStorm;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a dungeon portal spawned during a storm.
 */
public class Portal {

    private final UUID id;
    private final String dungeonName;
    private final TravelingStorm storm;
    private final Location location;
    private final List<Block> portalBlocks;
    private boolean active;

    public Portal(String dungeonName, TravelingStorm storm, Location location) {
        this.id = UUID.randomUUID();
        this.dungeonName = dungeonName;
        this.storm = storm;
        this.location = location;
        this.portalBlocks = new ArrayList<>();
        this.active = true;
    }

    /**
     * Spawn the physical portal structure.
     */
    public void spawn() {
        // Create a simple 3x3 nether portal frame
        Location base = location.clone();

        // Build frame (obsidian)
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                Location blockLoc = base.clone().add(x, y, 0);

                // Frame only on edges
                if (x == -1 || x == 1 || y == 0 || y == 2) {
                    blockLoc.getBlock().setType(Material.OBSIDIAN);
                    portalBlocks.add(blockLoc.getBlock());
                } else if (y == 1) {
                    // Middle - portal blocks
                    blockLoc.getBlock().setType(Material.NETHER_PORTAL);
                    portalBlocks.add(blockLoc.getBlock());
                }
            }
        }
    }

    /**
     * Remove the portal structure.
     */
    public void remove() {
        for (Block block : portalBlocks) {
            block.setType(Material.AIR);
        }
        portalBlocks.clear();
        active = false;
    }

    /**
     * Render particle effects around the portal.
     */
    public void renderParticles() {
        if (!active) return;

        Location particleLoc = location.clone().add(0, 1.5, 0);

        // Spawn portal particles in a circle
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI * i) / 20;
            double x = Math.cos(angle) * 2.0;
            double z = Math.sin(angle) * 2.0;

            location.getWorld().spawnParticle(
                Particle.PORTAL,
                particleLoc.clone().add(x, 0, z),
                1, 0, 0, 0, 0.1
            );
        }

        // Add some electric sparks
        location.getWorld().spawnParticle(
            Particle.ELECTRIC_SPARK,
            particleLoc,
            3, 0.5, 0.5, 0.5, 0.1
        );
    }

    /**
     * Check if this portal is still valid (storm still active, blocks intact).
     */
    public boolean isValid() {
        if (!active) return false;

        // Check if storm is still active
        if (storm.getRemainingSeconds() <= 0) {
            return false;
        }

        // Check if portal blocks are still intact
        int intactBlocks = 0;
        for (Block block : portalBlocks) {
            if (block.getType() == Material.NETHER_PORTAL || block.getType() == Material.OBSIDIAN) {
                intactBlocks++;
            }
        }

        // Portal is valid if at least half the blocks are intact
        return intactBlocks >= (portalBlocks.size() / 2);
    }

    /**
     * Check if a location is part of this portal.
     */
    public boolean isPortalBlock(Location loc) {
        for (Block block : portalBlocks) {
            if (block.getLocation().equals(loc)) {
                return true;
            }
        }
        return false;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getDungeonName() {
        return dungeonName;
    }

    public TravelingStorm getStorm() {
        return storm;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
