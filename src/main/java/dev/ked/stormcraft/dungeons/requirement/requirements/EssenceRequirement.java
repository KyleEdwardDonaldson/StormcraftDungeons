package dev.ked.stormcraft.dungeons.requirement.requirements;

import dev.ked.stormcraft.dungeons.integration.EssenceIntegration;
import dev.ked.stormcraft.dungeons.requirement.Requirement;
import org.bukkit.entity.Player;

public class EssenceRequirement implements Requirement {

    private final EssenceIntegration essenceIntegration;
    private final double requiredEssence;

    public EssenceRequirement(EssenceIntegration essenceIntegration, double requiredEssence) {
        this.essenceIntegration = essenceIntegration;
        this.requiredEssence = requiredEssence;
    }

    @Override
    public boolean check(Player player) {
        if (essenceIntegration == null || !essenceIntegration.isEnabled()) {
            return true; // No essence requirement if not available
        }
        return essenceIntegration.getPlayerEssence(player) >= requiredEssence;
    }

    @Override
    public String getFailureMessage(Player player) {
        double currentEssence = 0;
        if (essenceIntegration != null && essenceIntegration.isEnabled()) {
            currentEssence = essenceIntegration.getPlayerEssence(player);
        }
        return String.format("§c✗ Requires %.0f essence §7(You have: %.0f)", requiredEssence, currentEssence);
    }

    @Override
    public String getName() {
        return "Essence";
    }
}
