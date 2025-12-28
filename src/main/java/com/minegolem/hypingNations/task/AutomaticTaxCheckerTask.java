package com.minegolem.hypingNations.task;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;

import java.util.logging.Logger;

public class AutomaticTaxCheckerTask implements Runnable {

    private final HypingNations plugin;
    private final Logger logger;

    public AutomaticTaxCheckerTask(HypingNations plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public void run() {
        for (Nation nation : plugin.getNationManager().getAllNations()) {
            if (nation.getUnpaidDays() == 0) {
                continue;
            }

            try {
                attemptTaxCollection(nation);
            } catch (Exception e) {
                logger.warning("Error attempting tax collection for nation " + nation.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void attemptTaxCollection(Nation nation) {
        double taxAmount = plugin.getTaxManager().calculateTax(nation);

        Team capitalTeam = TeamAPI.getTeamByName(nation.getCapital().teamName());

        if (capitalTeam == null) {
            logger.warning("Capital team not found for nation " + nation.getName());
            return;
        }

        if (capitalTeam.getBalance() >= taxAmount) {
            capitalTeam.addBalance(-taxAmount);

            plugin.getTaxManager().recordPayment(nation);

            plugin.getTaxHistoryManager().recordTaxPayment(
                    nation.getId(),
                    taxAmount,
                    nation.getTotalChunks(),
                    true
            );

            logger.info(String.format(
                    "Automatic tax collection: Nation '%s' paid outstanding tax of $%.2f (was %d days unpaid)",
                    nation.getName(),
                    taxAmount,
                    nation.getUnpaidDays()
            ));

            plugin.getPersistenceService().saveNation(nation);

            if (capitalTeam.getOwner() != null) {
                var chief = plugin.getServer().getPlayer(capitalTeam.getOwner());
                if (chief != null && chief.isOnline()) {
                    chief.sendMessage("§8[§6HNations§8]§r §aYour nation's outstanding tax of §f$" +
                            String.format("%.2f", taxAmount) + " §ahas been automatically collected!");
                }
            }
        }
    }
}
