package dev.ked.stormcraft.dungeons;

import dev.ked.stormcraft.StormcraftPlugin;
import dev.ked.stormcraft.dungeons.command.DungeonCommand;
import dev.ked.stormcraft.dungeons.config.ConfigManager;
import dev.ked.stormcraft.dungeons.data.DataManager;
import dev.ked.stormcraft.dungeons.integration.EssenceIntegration;
import dev.ked.stormcraft.dungeons.integration.MythicDungeonsIntegration;
import dev.ked.stormcraft.dungeons.integration.StormcraftIntegration;
import dev.ked.stormcraft.dungeons.listener.DungeonEventListener;
import dev.ked.stormcraft.dungeons.listener.PortalInteractListener;
import dev.ked.stormcraft.dungeons.listener.StormEventListener;
import dev.ked.stormcraft.dungeons.portal.PortalManager;
import dev.ked.stormcraft.dungeons.requirement.RequirementChecker;
import dev.ked.stormcraft.dungeons.reward.RewardManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class StormcraftDungeonsPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private DataManager dataManager;
    private StormcraftIntegration stormcraftIntegration;
    private MythicDungeonsIntegration mythicDungeonsIntegration;
    private EssenceIntegration essenceIntegration;
    private PortalManager portalManager;
    private RequirementChecker requirementChecker;
    private RewardManager rewardManager;
    private Economy economy;

    @Override
    public void onEnable() {
        getLogger().info("Starting Stormcraft-Dungeons...");

        // Setup Stormcraft integration (required)
        if (!setupStormcraft()) {
            getLogger().severe("Stormcraft plugin not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Setup MythicDungeons integration (optional - runs in limited mode without it)
        if (!setupMythicDungeons()) {
            getLogger().warning("MythicDungeons plugin not found! Running in limited mode.");
            getLogger().warning("Portals will spawn but players cannot enter dungeons.");
            getLogger().warning("Install MythicDungeons to enable full functionality.");
        }

        // Load configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Setup optional integrations
        setupEconomy();
        setupEssence();

        // Initialize data manager
        dataManager = new DataManager(this);
        dataManager.loadData();

        // Initialize core systems
        requirementChecker = new RequirementChecker(this, configManager,
                                                   stormcraftIntegration,
                                                   essenceIntegration,
                                                   dataManager,
                                                   economy);

        rewardManager = new RewardManager(this, configManager,
                                         essenceIntegration,
                                         dataManager,
                                         economy);

        portalManager = new PortalManager(this, configManager,
                                         stormcraftIntegration,
                                         requirementChecker);

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        // Start portal spawning task
        portalManager.start();

        getLogger().info("Stormcraft-Dungeons enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Stopping Stormcraft-Dungeons...");

        // Stop portal manager
        if (portalManager != null) {
            portalManager.shutdown();
        }

        // Save player data
        if (dataManager != null) {
            dataManager.saveData();
        }

        getLogger().info("Stormcraft-Dungeons disabled.");
    }

    private boolean setupStormcraft() {
        if (getServer().getPluginManager().getPlugin("Stormcraft") == null) {
            return false;
        }

        try {
            StormcraftPlugin stormcraft = (StormcraftPlugin) getServer().getPluginManager().getPlugin("Stormcraft");
            stormcraftIntegration = new StormcraftIntegration(this, stormcraft);
            getLogger().info("Stormcraft integration enabled");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to integrate with Stormcraft: " + e.getMessage());
            return false;
        }
    }

    private boolean setupMythicDungeons() {
        if (getServer().getPluginManager().getPlugin("MythicDungeons") == null) {
            return false;
        }

        try {
            mythicDungeonsIntegration = new MythicDungeonsIntegration(this);
            if (mythicDungeonsIntegration.isEnabled()) {
                getLogger().info("MythicDungeons integration enabled");
                return true;
            } else {
                getLogger().severe("MythicDungeons found but integration failed!");
                return false;
            }
        } catch (Exception e) {
            getLogger().severe("Failed to integrate with MythicDungeons: " + e.getMessage());
            return false;
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found - economy features disabled");
            return;
        }

        try {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().warning("No economy provider found - economy features disabled");
                return;
            }
            economy = rsp.getProvider();
            getLogger().info("Vault economy integration enabled");
        } catch (Exception e) {
            getLogger().warning("Failed to setup economy: " + e.getMessage());
        }
    }

    private void setupEssence() {
        if (getServer().getPluginManager().getPlugin("Stormcraft-Essence") == null) {
            getLogger().warning("Stormcraft-Essence not found - SEL requirements disabled");
            return;
        }

        try {
            essenceIntegration = new EssenceIntegration(this);
            if (essenceIntegration.isEnabled()) {
                getLogger().info("Stormcraft-Essence integration enabled");
            } else {
                getLogger().warning("Stormcraft-Essence found but integration failed");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to integrate with Stormcraft-Essence: " + e.getMessage());
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
            new StormEventListener(this, portalManager), this);
        getServer().getPluginManager().registerEvents(
            new PortalInteractListener(this, portalManager, requirementChecker, mythicDungeonsIntegration), this);
        getServer().getPluginManager().registerEvents(
            new DungeonEventListener(this, rewardManager), this);
    }

    private void registerCommands() {
        DungeonCommand dungeonCommand = new DungeonCommand(this, portalManager, dataManager);
        getCommand("dungeon").setExecutor(dungeonCommand);
        getCommand("dungeon").setTabCompleter(dungeonCommand);
    }

    // Getters
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public StormcraftIntegration getStormcraftIntegration() {
        return stormcraftIntegration;
    }

    public MythicDungeonsIntegration getMythicDungeonsIntegration() {
        return mythicDungeonsIntegration;
    }

    public EssenceIntegration getEssenceIntegration() {
        return essenceIntegration;
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }

    public RequirementChecker getRequirementChecker() {
        return requirementChecker;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
