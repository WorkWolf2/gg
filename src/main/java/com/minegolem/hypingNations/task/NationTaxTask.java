package com.minegolem.hypingNations.task;

import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.NationManager;
import com.minegolem.hypingNations.manager.TaxManager;
import com.tcoded.folialib.FoliaLib;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;

import java.util.logging.Logger;

public class NationTaxTask implements Runnable {
    private final FoliaLib foliaLib;
    private final NationManager nationManager;
    private final TaxManager taxManager;
    private final Logger logger;

    public NationTaxTask(FoliaLib foliaLib, NationManager nationManager, TaxManager taxManager, Logger logger) {
        this.foliaLib = foliaLib;
        this.nationManager = nationManager;
        this.taxManager = taxManager;
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

        Team capitalTeam = TeamAPI.getTeamByName(nation.getCapital().teamName());

        if (capitalTeam == null) {
            logger.warning("Capital team not found for nation " + nation.getName() + ", dissolving...");
            nationManager.deleteNation(nation.getName());
            return;
        }

        if (capitalTeam.getBalance() >= taxAmount) {
            capitalTeam.addBalance(-taxAmount);
            taxManager.recordPayment(nation);
            logger.info("Nation " + nation.getName() + " paid tax of " + taxAmount);
        } else {
            taxManager.recordMissedPayment(nation);
            logger.warning("Nation " + nation.getName() + " missed tax payment. Unpaid days: " + nation.getUnpaidDays());

            if (taxManager.shouldDissolve(nation)) {
                logger.warning("Nation " + nation.getName() + " dissolved due to unpaid taxes.");
                nationManager.deleteNation(nation.getName());
            }
        }
    }

    public void startDaily() {
        foliaLib.getScheduler().runTimerAsync(
                this,
                20L * 60 * 60 * 24, // delay 24h
                20L * 60 * 60 * 24  // repeat 24h
        );
    }
}

