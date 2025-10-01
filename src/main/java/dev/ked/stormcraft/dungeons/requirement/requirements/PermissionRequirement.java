package dev.ked.stormcraft.dungeons.requirement.requirements;

import dev.ked.stormcraft.dungeons.requirement.Requirement;
import org.bukkit.entity.Player;

public class PermissionRequirement implements Requirement {

    private final String permission;

    public PermissionRequirement(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean check(Player player) {
        return player.hasPermission(permission);
    }

    @Override
    public String getFailureMessage(Player player) {
        return "§c✗ You don't have permission to access this dungeon";
    }

    @Override
    public String getName() {
        return "Permission";
    }
}
