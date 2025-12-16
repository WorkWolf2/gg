package com.minegolem.hypingNations.manager;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InvitationManager {

    @Getter
    public static class Invitation {
        private final String nationName;
        private final String cityName;
        private final LocalDateTime expirationTime;

        public Invitation(String nationName, String cityName, int expirationMinutes) {
            this.nationName = nationName;
            this.cityName = cityName;
            this.expirationTime = LocalDateTime.now().plusMinutes(expirationMinutes);
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }

    private final Map<String, Invitation> pendingInvitations = new ConcurrentHashMap<>();
    private final int invitationExpirationMinutes;

    public InvitationManager(int invitationExpirationMinutes) {
        this.invitationExpirationMinutes = invitationExpirationMinutes;
    }

    public InvitationManager() {
        this(30);
    }

    /**
     * Create an invitation for a city to join a nation
     */
    public void createInvitation(String nationName, String cityName) {
        Invitation invitation = new Invitation(nationName, cityName, invitationExpirationMinutes);
        pendingInvitations.put(cityName.toLowerCase(), invitation);
    }

    /**
     * Get pending invitation for a city
     */
    public Optional<Invitation> getInvitation(String cityName) {
        Invitation invitation = pendingInvitations.get(cityName.toLowerCase());

        if (invitation == null) {
            return Optional.empty();
        }

        // Check if expired
        if (invitation.isExpired()) {
            pendingInvitations.remove(cityName.toLowerCase());
            return Optional.empty();
        }

        return Optional.of(invitation);
    }

    /**
     * Accept an invitation (removes it from pending)
     */
    public boolean acceptInvitation(String cityName) {
        Optional<Invitation> invitation = getInvitation(cityName);
        if (invitation.isPresent()) {
            pendingInvitations.remove(cityName.toLowerCase());
            return true;
        }
        return false;
    }

    /**
     * Cancel/deny an invitation
     */
    public boolean cancelInvitation(String cityName) {
        return pendingInvitations.remove(cityName.toLowerCase()) != null;
    }

    /**
     * Check if a city has a pending invitation
     */
    public boolean hasInvitation(String cityName) {
        return getInvitation(cityName).isPresent();
    }

    /**
     * Remove expired invitations (cleanup)
     */
    public void cleanupExpiredInvitations() {
        pendingInvitations.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
