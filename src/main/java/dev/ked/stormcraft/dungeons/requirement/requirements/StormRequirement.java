package dev.ked.stormcraft.dungeons.requirement.requirements;

import dev.ked.stormcraft.dungeons.integration.StormcraftIntegration;
import dev.ked.stormcraft.dungeons.requirement.Requirement;
import dev.ked.stormcraft.model.TravelingStorm;
import org.bukkit.entity.Player;

public class StormRequirement implements Requirement {

    private final StormcraftIntegration stormcraftIntegration;
    private final TravelingStorm storm;
    private final double maxDistance;
    private final int minIntensity;

    public StormRequirement(StormcraftIntegration stormcraftIntegration, TravelingStorm storm,
                           double maxDistance, int minIntensity) {
        this.stormcraftIntegration = stormcraftIntegration;
        this.storm = storm;
        this.maxDistance = maxDistance;
        this.minIntensity = minIntensity;
    }

    @Override
    public boolean check(Player player) {
        // Check if near storm
        if (!stormcraftIntegration.isPlayerNearStorm(player, storm, maxDistance)) {
            return false;
        }

        // Check storm intensity
        return stormcraftIntegration.getStormIntensity(storm) >= minIntensity;
    }

    @Override
    public String getFailureMessage(Player player) {
        return String.format("§c✗ Must be near a storm (intensity %d+)", minIntensity);
    }

    @Override
    public String getName() {
        return "Storm Proximity";
    }
}
