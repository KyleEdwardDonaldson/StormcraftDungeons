package dev.ked.stormcraft.dungeons.requirement.requirements;

import dev.ked.stormcraft.dungeons.data.DataManager;
import dev.ked.stormcraft.dungeons.requirement.Requirement;
import org.bukkit.entity.Player;

public class CompletionRequirement implements Requirement {

    private final DataManager dataManager;
    private final String requiredDungeon;
    private final int requiredCount;

    public CompletionRequirement(DataManager dataManager, String requiredDungeon, int requiredCount) {
        this.dataManager = dataManager;
        this.requiredDungeon = requiredDungeon;
        this.requiredCount = requiredCount;
    }

    @Override
    public boolean check(Player player) {
        int completions = dataManager.getCompletionCount(player.getUniqueId(), requiredDungeon);
        return completions >= requiredCount;
    }

    @Override
    public String getFailureMessage(Player player) {
        int current = dataManager.getCompletionCount(player.getUniqueId(), requiredDungeon);
        return String.format("§c✗ Must complete %s %d times §7(You have: %d)",
                           requiredDungeon, requiredCount, current);
    }

    @Override
    public String getName() {
        return "Completion";
    }
}
