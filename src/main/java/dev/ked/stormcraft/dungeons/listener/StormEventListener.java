package dev.ked.stormcraft.dungeons.listener;

import dev.ked.stormcraft.dungeons.portal.PortalManager;
import dev.ked.stormcraft.api.events.StormcraftStormEndEvent;
import dev.ked.stormcraft.api.events.StormcraftStormStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listens for storm-related events.
 */
public class StormEventListener implements Listener {

    private final JavaPlugin plugin;
    private final PortalManager portalManager;

    public StormEventListener(JavaPlugin plugin, PortalManager portalManager) {
        this.plugin = plugin;
        this.portalManager = portalManager;
    }

    @EventHandler
    public void onStormStart(StormcraftStormStartEvent event) {
        // Portal spawning is handled by PortalManager's periodic check task
        // This is just here for potential future use
    }

    @EventHandler
    public void onStormEnd(StormcraftStormEndEvent event) {
        // Portals will be cleaned up by PortalManager's cleanup task
        // when it detects the storm has ended
    }
}
