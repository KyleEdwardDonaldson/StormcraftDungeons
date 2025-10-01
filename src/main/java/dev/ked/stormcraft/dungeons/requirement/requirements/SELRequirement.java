package dev.ked.stormcraft.dungeons.requirement.requirements;

import dev.ked.stormcraft.dungeons.integration.EssenceIntegration;
import dev.ked.stormcraft.dungeons.requirement.Requirement;
import org.bukkit.entity.Player;

public class SELRequirement implements Requirement {

    private final EssenceIntegration essenceIntegration;
    private final int minSEL;

    public SELRequirement(EssenceIntegration essenceIntegration, int minSEL) {
        this.essenceIntegration = essenceIntegration;
        this.minSEL = minSEL;
    }

    @Override
    public boolean check(Player player) {
        if (essenceIntegration == null || !essenceIntegration.isEnabled()) {
            return true; // No SEL requirement if essence not available
        }
        return essenceIntegration.getPlayerSEL(player) >= minSEL;
    }

    @Override
    public String getFailureMessage(Player player) {
        int currentSEL = 0;
        if (essenceIntegration != null && essenceIntegration.isEnabled()) {
            currentSEL = essenceIntegration.getPlayerSEL(player);
        }
        return String.format("§c✗ Requires SEL %d §7(You have: %d)", minSEL, currentSEL);
    }

    @Override
    public String getName() {
        return "SEL";
    }
}
