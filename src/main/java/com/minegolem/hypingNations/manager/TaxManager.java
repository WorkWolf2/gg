package com.minegolem.hypingNations.manager;

import com.minegolem.hypingNations.data.Nation;

public class TaxManager {

    private final double perChunkPrice;
    private final int maxUnpaidDaysBeforeDissolution;

    public TaxManager(double perChunkPrice, int maxUnpaidDaysBeforeDissolution) {
        this.perChunkPrice = perChunkPrice;
        this.maxUnpaidDaysBeforeDissolution = maxUnpaidDaysBeforeDissolution;
    }

    public double calculateTax(Nation nation) {
        int totalChunks = nation.getTotalChunks();
        return totalChunks * perChunkPrice;
    }

    public void recordPayment(Nation nation) {
        nation.resetUnpaidDays();
    }

    public void recordMissedPayment(Nation nation) {
        nation.incrementUnpaidDays();
    }

    public boolean shouldDissolve(Nation nation) {
        return nation.getUnpaidDays() >= maxUnpaidDaysBeforeDissolution;
    }

    public int getUnpaidDays(Nation nation) {
        return nation.getUnpaidDays();
    }

    public double getPerChunkPrice() {
        return perChunkPrice;
    }

    public int getMaxUnpaidDaysBeforeDissolution() {
        return maxUnpaidDaysBeforeDissolution;
    }
}
