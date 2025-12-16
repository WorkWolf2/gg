package com.minegolem.hypingNations.task;

import com.minegolem.hypingNations.manager.InvitationManager;

public class InvitationCleanupTask implements Runnable {

    private final InvitationManager invitationManager;

    public InvitationCleanupTask(InvitationManager invitationManager) {
        this.invitationManager = invitationManager;
    }

    @Override
    public void run() {
        invitationManager.cleanupExpiredInvitations();
    }
}
