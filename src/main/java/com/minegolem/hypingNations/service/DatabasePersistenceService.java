package com.minegolem.hypingNations.service;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.data.database.DatabaseManager;

import java.sql.SQLException;
import java.util.*;

public class DatabasePersistenceService implements PersistenceService {

    private final DatabaseManager databaseManager;
    private final HypingNations plugin;

    public DatabasePersistenceService(DatabaseManager databaseManager, HypingNations plugin) {
        this.databaseManager = databaseManager;
        this.plugin = plugin;
    }

    @Override
    public List<Nation> loadNations() {
        try {
            List<Nation> nations = databaseManager.loadAllNations();

            Map<UUID, Nation> nationMap = new HashMap<>();
            for (Nation nation : nations) {
                nationMap.put(nation.getId(), nation);
            }

            databaseManager.loadAllPacts(nationMap);

            plugin.getLogger().info("Loaded " + nations.size() + " nations from database");
            return nations;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load nations from database: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void saveNations(Collection<Nation> nations) {
        for (Nation nation : nations) {
            databaseManager.saveNationAsync(nation);
        }
    }

    @Override
    public void saveNation(Nation nation) {
        databaseManager.saveNationAsync(nation);
    }

    @Override
    public void deleteNation(UUID nationId) {
        databaseManager.deleteNationAsync(nationId).exceptionally(throwable -> {
            plugin.getLogger().severe("Failed to delete nation: " + throwable.getMessage());
            return null;
        });
    }
}
