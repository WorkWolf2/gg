package com.minegolem.hypingNations.task;

import com.minegolem.hypingNations.manager.PactManager;

public class PactCleanupTask implements Runnable {

    private final PactManager pactManager;

    public PactCleanupTask(PactManager pactManager) {
        this.pactManager = pactManager;
    }

    @Override
    public void run() {
        pactManager.cleanupExpiredPacts();
    }
}
