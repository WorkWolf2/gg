package com.minegolem.hypingNations.task;

import com.minegolem.hypingNations.HypingNations;

public class BackupTask implements Runnable {

    private final HypingNations plugin;

    public BackupTask(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getLogger().info("Starting automatic database backup...");

        plugin.getDatabaseManager().createBackupAsync()
                .thenAccept(success -> {
                    if (success) {
                        plugin.getLogger().info("Automatic backup completed successfully");
                    } else {
                        plugin.getLogger().warning("Automatic backup failed - check logs for details");
                    }
                })
                .exceptionally(throwable -> {
                    plugin.getLogger().severe("Backup task error: " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                });
    }
}
