package com.minegolem.hypingNations.task;

import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import com.minegolem.hypingNations.manager.NationManager;
import com.minegolem.hypingNations.manager.TaxHistoryManager;
import com.minegolem.hypingNations.manager.TaxManager;
import com.tcoded.folialib.FoliaLib;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;

import java.util.logging.Logger;

public class NationTaxTask implements Runnable {
    private final FoliaLib foliaLib;
    private final NationManager nationManager;
    private final TaxManager taxManager;
    private final TaxHistoryManager taxHistoryManager;
    private final Logger logger;

    public NationTaxTask(FoliaLib foliaLib, NationManager nationManager, TaxManager taxManager,
                         TaxHistoryManager taxHistoryManager, Logger logger) {
        this.foliaLib = foliaLib;
        this.nationManager = nationManager;
        this.taxManager = taxManager;
        this.taxHistoryManager = taxHistoryManager;
        this.logger = logger;
    }

    @Override
    public void run() {
        logger.info("Starting daily tax collection...");

        for (Nation nation : nationManager.getAllNations()) {
            try {
                processTaxForNation(nation);
            } catch (Exception e) {
                logger.severe("Error processing tax for nation " + nation.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        logger.info("Tax collection completed.");
    }

    private void processTaxForNation(Nation nation) {
        double taxAmount = taxManager.calculateTax(nation);
        int chunks = nation.getTotalChunks();

        Team capitalTeam = TeamAPI.getTeamByName(nation.getCapital().teamName());

        if (capitalTeam == null) {
            logger.warning("Capital team not found for nation " + nation.getName() + ", dissolving...");
            nationManager.deleteNation(nation.getName());
            return;
        }

        if (capitalTeam.getBalance() >= taxAmount) {
            // Successful payment
            capitalTeam.addBalance(-taxAmount);
            taxManager.recordPayment(nation);

            // Record in tax history
            taxHistoryManager.recordTaxPayment(nation.getId(), taxAmount, chunks, true);

            logger.info("Nation " + nation.getName() + " paid tax of " + taxAmount);
        } else {
            // Failed payment
            taxManager.recordMissedPayment(nation);

            // Record in tax history
            taxHistoryManager.recordTaxPayment(nation.getId(), taxAmount, chunks, false);

            logger.warning("Nation " + nation.getName() + " missed tax payment. Unpaid days: " + nation.getUnpaidDays());

            if (taxManager.shouldDissolve(nation)) {
                logger.warning("Nation " + nation.getName() + " dissolved due to unpaid taxes.");
                nationManager.deleteNation(nation.getName());

                // Clear tax history for dissolved nation
                taxHistoryManager.clearHistory(nation.getId());
            }
        }
    }

    public void startDaily() {
        foliaLib.getScheduler().runTimerAsync(
                this,
                20L * 60 * 60 * 24,
                20L * 60 * 60 * 24
        );
    }
}

