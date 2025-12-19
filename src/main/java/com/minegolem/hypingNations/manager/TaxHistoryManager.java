package com.minegolem.hypingNations.manager;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.TaxHistory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaxHistoryManager {

    private final HypingNations plugin;
    private final Map<UUID, List<TaxHistory.TaxEntry>> taxHistories = new ConcurrentHashMap<>();
    private static final int MAX_ENTRIES_PER_NATION = 100;

    public TaxHistoryManager(HypingNations plugin) {
        this.plugin = plugin;
    }

    public void recordTaxPayment(UUID nationId, double amount, int chunks, boolean success) {
        TaxHistory.TaxEntry entry = new TaxHistory.TaxEntry(
                nationId,
                LocalDateTime.now(),
                amount,
                chunks,
                success
        );

        taxHistories.computeIfAbsent(nationId, k -> new ArrayList<>()).add(0, entry);

        // Keep only last MAX_ENTRIES_PER_NATION entries
        List<TaxHistory.TaxEntry> entries = taxHistories.get(nationId);
        if (entries.size() > MAX_ENTRIES_PER_NATION) {
            entries.subList(MAX_ENTRIES_PER_NATION, entries.size()).clear();
        }

        // Save to database
        plugin.getDatabaseManager().saveTaxEntryAsync(entry);
    }

    public List<TaxHistory.TaxEntry> getTaxHistory(UUID nationId) {
        return new ArrayList<>(taxHistories.getOrDefault(nationId, new ArrayList<>()));
    }

    public void loadHistory(UUID nationId) {
        try {
            List<TaxHistory.TaxEntry> entries = plugin.getDatabaseManager().loadTaxHistory(nationId);
            taxHistories.put(nationId, entries);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load tax history for nation " + nationId + ": " + e.getMessage());
        }
    }

    public void clearHistory(UUID nationId) {
        taxHistories.remove(nationId);
        plugin.getDatabaseManager().deleteTaxHistoryAsync(nationId);
    }
}
