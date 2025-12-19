package com.minegolem.hypingNations;

import com.minegolem.hypingNations.command.HNationsCommand;
import com.minegolem.hypingNations.config.ConfigManager;
import com.minegolem.hypingNations.data.database.DatabaseManager;
import com.minegolem.hypingNations.listener.TeamClaimListener;
import com.minegolem.hypingNations.manager.*;
import com.minegolem.hypingNations.menu.MenuManager;
import com.minegolem.hypingNations.role.NationPermissionManager;
import com.minegolem.hypingNations.service.DatabasePersistenceService;
import com.minegolem.hypingNations.task.*;
import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Getter
public final class HypingNations extends JavaPlugin {

    public static HypingNations instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private MenuManager menuManager;
    private FoliaLib foliaLib;
    private DatabaseManager databaseManager;
    private DatabasePersistenceService persistenceService;

    private NationManager nationManager;
    private TaxManager taxManager;
    private TaxHistoryManager taxHistoryManager;
    private PactManager pactManager;
    private RangeManager rangeManager;
    private InvitationManager invitationManager;

    private NationPermissionManager permissionManager;
    private NationMemberManager nationMemberManager;

    @Override
    public void onEnable() {
        instance = this;

        foliaLib = new FoliaLib(this);
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);

        if (!initializeDatabase()) {
            getLogger().severe("Failed to initialize database! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initManagers();
        loadData();
        startTasks();
        registerCommands();

        // Initialize menu manager
        menuManager = new MenuManager(this);

        getLogger().info("HypingNations enabled!");
    }

    @Override
    public void onDisable() {
        saveData();

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("HypingNations disabled!");
    }

    private boolean initializeDatabase() {
        String dbType = getConfig().getString("database.type", "mariadb");

        if (dbType.equalsIgnoreCase("mariadb")) {
            try {
                databaseManager = new DatabaseManager(this);
                databaseManager.initialize();

                persistenceService = new DatabasePersistenceService(databaseManager, this);

                return true;
            } catch (Exception e) {
                getLogger().severe("Failed to initialize MariaDB: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            getLogger().severe("Invalid database type: " + dbType);
            getLogger().severe("Only 'mariadb' is supported.");
            return false;
        }
    }

    private void initManagers() {
        nationManager = new NationManager();
        nationManager.setPersistenceService(persistenceService);

        double perChunkPrice = configManager.getNationConfig().getTaxPerChunk();
        int maxUnpaidDays = configManager.getNationConfig().getMaxUnpaidDays();
        taxManager = new TaxManager(perChunkPrice, maxUnpaidDays);

        taxHistoryManager = new TaxHistoryManager(this);
        nationMemberManager = new NationMemberManager(this);

        pactManager = new PactManager();
        rangeManager = new RangeManager(configManager.getNationConfig());
        invitationManager = new InvitationManager(30);

        permissionManager = new NationPermissionManager(this);
    }

    private void loadData() {
        try {
            nationManager.loadNations();

            // Load tax histories and roles for all nations
            for (var nation : nationManager.getAllNations()) {
                taxHistoryManager.loadHistory(nation.getId());
                nationMemberManager.loadRoles(nation.getId());
            }

            getLogger().info("Data loaded successfully from database");
        } catch (Exception e) {
            getLogger().severe("Failed to load data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            nationManager.saveNations();
            getLogger().info("Data saved successfully to database");
        } catch (Exception e) {
            getLogger().severe("Failed to save data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startTasks() {
        // Daily tax task
        NationTaxTask taxTask = new NationTaxTask(foliaLib, nationManager, taxManager,
                taxHistoryManager, getLogger());
        taxTask.startDaily();

        // Invitation cleanup task (every 5 minutes)
        InvitationCleanupTask cleanupTask = new InvitationCleanupTask(invitationManager);
        foliaLib.getScheduler().runTimerAsync(
                cleanupTask,
                20L * 60 * 5,
                20L * 60 * 5
        );

        // Pact cleanup task (every hour)
        PactCleanupTask pactCleanupTask = new PactCleanupTask(pactManager);
        foliaLib.getScheduler().runTimerAsync(
                pactCleanupTask,
                20L * 60 * 60,
                20L * 60 * 60
        );

        // Automatic tax checker (every 5 minutes)
        AutomaticTaxCheckerTask taxCheckerTask = new AutomaticTaxCheckerTask(this);
        foliaLib.getScheduler().runTimerAsync(
                taxCheckerTask,
                20L * 60 * 5,
                20L * 60 * 5
        );

        // Auto-save task (every 10 minutes)
        foliaLib.getScheduler().runTimerAsync(
                this::saveData,
                20L * 60 * 10,
                20L * 60 * 10
        );

        // Backup task
        if (getConfig().getBoolean("database.backup.enabled", true)) {
            int intervalHours = getConfig().getInt("database.backup.interval-hours", 6);
            BackupTask backupTask = new BackupTask(this);

            foliaLib.getScheduler().runTimerAsync(
                    backupTask,
                    20L * 60 * 60 * intervalHours,
                    20L * 60 * 60 * intervalHours
            );

            getLogger().info("Backup task started (every " + intervalHours + " hours)");
        }

        getLogger().info("All tasks started successfully!");
    }

    private void registerCommands() {
        // Main command
        HNationsCommand command = new HNationsCommand(this);
        Objects.requireNonNull(getCommand("hnations")).setExecutor(command);
        Objects.requireNonNull(getCommand("hnations")).setTabCompleter(command);

        registerListeners();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.minegolem.hypingNations.placeholder.HypingNationsExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new TeamClaimListener(this),
                this
        );
        getServer().getPluginManager().registerEvents(
                new MenuManager(this),
                this
        );
        getLogger().info("Pact claim protection listener registered!");
    }

    public void reload() {
        saveData();

        configManager.reloadConfigs();
        messageManager.reload();

        double perChunkPrice = configManager.getNationConfig().getTaxPerChunk();
        int maxUnpaidDays = configManager.getNationConfig().getMaxUnpaidDays();
        taxManager = new TaxManager(perChunkPrice, maxUnpaidDays);

        rangeManager = new RangeManager(configManager.getNationConfig());
        permissionManager = new NationPermissionManager(this);

        if (menuManager != null) {
            menuManager.reload();
        }

        getLogger().info("Configuration reloaded!");
    }
}