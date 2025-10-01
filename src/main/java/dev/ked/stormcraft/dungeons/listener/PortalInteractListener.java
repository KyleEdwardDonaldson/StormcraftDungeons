package dev.ked.stormcraft.dungeons.listener;

import dev.ked.stormcraft.dungeons.integration.MythicDungeonsIntegration;
import dev.ked.stormcraft.dungeons.portal.Portal;
import dev.ked.stormcraft.dungeons.portal.PortalManager;
import dev.ked.stormcraft.dungeons.requirement.RequirementChecker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Listens for player interactions with dungeon portals.
 */
public class PortalInteractListener implements Listener {

    private final JavaPlugin plugin;
    private final PortalManager portalManager;
    private final RequirementChecker requirementChecker;
    private final MythicDungeonsIntegration mythicDungeonsIntegration;

    public PortalInteractListener(JavaPlugin plugin, PortalManager portalManager,
                                  RequirementChecker requirementChecker,
                                  MythicDungeonsIntegration mythicDungeonsIntegration) {
        this.plugin = plugin;
        this.portalManager = portalManager;
        this.requirementChecker = requirementChecker;
        this.mythicDungeonsIntegration = mythicDungeonsIntegration;
    }

    @EventHandler
    public void onPortalInteract(PlayerInteractEvent event) {
        // Check if player right-clicked a block
        if (!event.getAction().isRightClick() || event.getClickedBlock() == null) {
            return;
        }

        // Check if block is a portal block
        if (event.getClickedBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        // Check if this is one of our dungeon portals
        Portal portal = portalManager.getPortalAtLocation(event.getClickedBlock().getLocation());
        if (portal == null) {
            return;
        }

        // Cancel the event (prevent normal portal behavior)
        event.setCancelled(true);

        Player player = event.getPlayer();

        // Check if player can bypass requirements
        if (player.hasPermission("stormcraft.dungeons.bypass")) {
            openDungeonGUI(player, portal);
            return;
        }

        // Check requirements
        List<String> failures = requirementChecker.checkRequirements(player, portal);

        if (failures != null && !failures.isEmpty()) {
            // Requirements not met
            player.sendMessage("§c✗ You cannot enter this dungeon!");
            player.sendMessage("§e⚠ Requirements:");
            for (String failure : failures) {
                player.sendMessage("  " + failure);
            }
            return;
        }

        // All requirements met - consume entry costs
        if (!requirementChecker.consumeEntryCosts(player, portal.getDungeonName())) {
            player.sendMessage("§c✗ Failed to process entry cost!");
            return;
        }

        // Open dungeon GUI
        openDungeonGUI(player, portal);
    }

    private void openDungeonGUI(Player player, Portal portal) {
        // Check if MythicDungeons is available
        if (mythicDungeonsIntegration == null || !mythicDungeonsIntegration.isEnabled()) {
            player.sendMessage("§c✗ Dungeon system is currently unavailable!");
            player.sendMessage("§7MythicDungeons is not installed. Contact an administrator.");
            return;
        }

        // Open MythicDungeons interface
        mythicDungeonsIntegration.openDungeonGUI(player, portal.getDungeonName());
        player.sendMessage("§a✓ Entering " + portal.getDungeonName() + "...");
    }
}
