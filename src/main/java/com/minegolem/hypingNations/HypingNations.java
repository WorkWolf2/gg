package com.minegolem.hypingNations;

import com.minegolem.hypingNations.command.HNationsCommand;
import com.minegolem.hypingNations.config.ConfigManager;
import com.minegolem.hypingNations.listener.TeamClaimListener;
import com.minegolem.hypingNations.manager.*;
import com.minegolem.hypingNations.service.PersistenceService;
import com.minegolem.hypingNations.task.InvitationCleanupTask;
import com.minegolem.hypingNations.task.NationTaxTask;
import com.minegolem.hypingNations.task.PactCleanupTask;
import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Getter
public final class HypingNations extends JavaPlugin {

    public static HypingNations instance;

    private ConfigManager configManager;
    private FoliaLib foliaLib;
    private PersistenceService persistenceService;

    private NationManager nationManager;
    private TaxManager taxManager;
    private PactManager pactManager;
    private RangeManager rangeManager;
    private InvitationManager invitationManager;

    @Override
    public void onEnable() {
        instance = this;

        foliaLib = new FoliaLib(this);
        configManager = new ConfigManager(this);
        persistenceService = new PersistenceService(this);

        initManagers();
        loadData();
        startTasks();
        registerCommands();

        getLogger().info("HypingNations enabled!");
    }

    @Override
    public void onDisable() {
        saveData();

        getLogger().info("HypingNations disabled!");
    }

    private void initManagers() {
        nationManager = new NationManager();
        nationManager.setPersistenceService(persistenceService);

        double perChunkPrice = configManager.getNationConfig().getTaxPerChunk();
        int maxUnpaidDays = configManager.getNationConfig().getMaxUnpaidDays();
        taxManager = new TaxManager(perChunkPrice, maxUnpaidDays);

        pactManager = new PactManager();
        rangeManager = new RangeManager(configManager.getNationConfig());
        invitationManager = new InvitationManager(30); // 30 minutes expiration
    }

    private void loadData() {
        try {
            nationManager.loadNations();
            getLogger().info("Data loaded successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to load data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            nationManager.saveNations();
            getLogger().info("Data saved successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to save data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startTasks() {
        NationTaxTask taxTask = new NationTaxTask(foliaLib, nationManager, taxManager, getLogger());
        taxTask.startDaily();

        InvitationCleanupTask cleanupTask = new InvitationCleanupTask(invitationManager);
        foliaLib.getScheduler().runTimerAsync(
                cleanupTask,
                20L * 60 * 5,
                20L * 60 * 5
        );

        PactCleanupTask pactCleanupTask = new PactCleanupTask(pactManager);
        foliaLib.getScheduler().runTimerAsync(
                pactCleanupTask,
                20L * 60 * 60,
                20L * 60 * 60
        );

        foliaLib.getScheduler().runTimerAsync(
                this::saveData,
                20L * 60 * 10,
                20L * 60 * 10
        );

        getLogger().info("All tasks started successfully!");
    }

    private void registerCommands() {
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
        getLogger().info("Pact claim protection listener registered!");
    }

    public void reload() {
        saveData();

        configManager.reloadConfigs();

        double perChunkPrice = configManager.getNationConfig().getTaxPerChunk();
        int maxUnpaidDays = configManager.getNationConfig().getMaxUnpaidDays();
        taxManager = new TaxManager(perChunkPrice, maxUnpaidDays);

        rangeManager = new RangeManager(configManager.getNationConfig());

        getLogger().info("Configuration reloaded!");
    }
}