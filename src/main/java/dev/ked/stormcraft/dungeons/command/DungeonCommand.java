package dev.ked.stormcraft.dungeons.command;

import dev.ked.stormcraft.dungeons.StormcraftDungeonsPlugin;
import dev.ked.stormcraft.dungeons.data.DataManager;
import dev.ked.stormcraft.dungeons.portal.Portal;
import dev.ked.stormcraft.dungeons.portal.PortalManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main command handler for /dungeon.
 */
public class DungeonCommand implements CommandExecutor, TabCompleter {

    private final StormcraftDungeonsPlugin plugin;
    private final PortalManager portalManager;
    private final DataManager dataManager;

    public DungeonCommand(StormcraftDungeonsPlugin plugin, PortalManager portalManager, DataManager dataManager) {
        this.plugin = plugin;
        this.portalManager = portalManager;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "list":
                return handleList(sender);
            case "stats":
                return handleStats(sender);
            case "nearest":
                return handleNearest(sender);
            case "reload":
                return handleReload(sender);
            case "clear":
                return handleClear(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l[Dungeons] §fHelp");
        sender.sendMessage("§e/dungeon list §7- List active portals");
        sender.sendMessage("§e/dungeon stats §7- Show your completions");
        sender.sendMessage("§e/dungeon nearest §7- Find nearest portal");
        if (sender.hasPermission("stormcraft.dungeons.admin")) {
            sender.sendMessage("§c/dungeon reload §7- Reload configuration");
            sender.sendMessage("§c/dungeon clear §7- Remove all portals");
        }
    }

    private boolean handleList(CommandSender sender) {
        var portals = portalManager.getActivePortals();

        if (portals.isEmpty()) {
            sender.sendMessage("§6[Dungeons] §7No active portals");
            return true;
        }

        sender.sendMessage("§6[Dungeons] §fActive Portals:");
        for (Portal portal : portals) {
            String dungeonName = plugin.getConfigManager().getConfig().getString(
                "dungeons." + portal.getDungeonName() + ".display_name",
                portal.getDungeonName()
            );

            String location = String.format("%d, %d, %d",
                portal.getLocation().getBlockX(),
                portal.getLocation().getBlockY(),
                portal.getLocation().getBlockZ()
            );

            // Calculate distance if sender is a player
            String distanceStr = "";
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.getWorld().equals(portal.getLocation().getWorld())) {
                    double distance = player.getLocation().distance(portal.getLocation());
                    distanceStr = String.format(" §7(%.0fm away)", distance);
                }
            }

            sender.sendMessage(String.format("§e• %s §7at §f%s%s", dungeonName, location, distanceStr));
        }

        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can view stats!");
            return true;
        }

        Player player = (Player) sender;
        Map<String, Integer> completions = dataManager.getPlayerCompletions(player.getUniqueId());

        if (completions.isEmpty()) {
            sender.sendMessage("§6[Dungeons] §7You haven't completed any dungeons yet");
            return true;
        }

        sender.sendMessage("§6[Dungeons] §fYour Statistics:");
        for (Map.Entry<String, Integer> entry : completions.entrySet()) {
            String dungeonName = plugin.getConfigManager().getConfig().getString(
                "dungeons." + entry.getKey() + ".display_name",
                entry.getKey()
            );
            sender.sendMessage(String.format("§e• %s: §f%d completions", dungeonName, entry.getValue()));
        }

        return true;
    }

    private boolean handleNearest(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can find nearest portal!");
            return true;
        }

        Player player = (Player) sender;
        Portal nearest = portalManager.getNearestPortal(player.getLocation());

        if (nearest == null) {
            sender.sendMessage("§6[Dungeons] §7No active portals found");
            return true;
        }

        String dungeonName = plugin.getConfigManager().getConfig().getString(
            "dungeons." + nearest.getDungeonName() + ".display_name",
            nearest.getDungeonName()
        );

        double distance = player.getLocation().distance(nearest.getLocation());
        String location = String.format("%d, %d, %d",
            nearest.getLocation().getBlockX(),
            nearest.getLocation().getBlockY(),
            nearest.getLocation().getBlockZ()
        );

        sender.sendMessage(String.format("§6[Dungeons] §fNearest portal: %s §7at §f%s §7(%.0fm away)",
            dungeonName, location, distance));

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("stormcraft.dungeons.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        plugin.getConfigManager().reloadConfigs();
        sender.sendMessage("§a✓ Configuration reloaded");
        return true;
    }

    private boolean handleClear(CommandSender sender) {
        if (!sender.hasPermission("stormcraft.dungeons.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        int count = portalManager.getActivePortals().size();
        portalManager.clearAllPortals();
        sender.sendMessage(String.format("§a✓ Removed %d portals", count));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("list", "stats", "nearest", "help");
            if (sender.hasPermission("stormcraft.dungeons.admin")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("reload");
                subcommands.add("clear");
            }

            String input = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        }

        return completions;
    }
}
