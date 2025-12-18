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

    public void createInvitation(String nationName, String cityName) {
        Invitation invitation = new Invitation(nationName, cityName, invitationExpirationMinutes);
        pendingInvitations.put(cityName.toLowerCase(), invitation);
    }

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

    public boolean acceptInvitation(String cityName) {
        Optional<Invitation> invitation = getInvitation(cityName);
        if (invitation.isPresent()) {
            pendingInvitations.remove(cityName.toLowerCase());
            return true;
        }
        return false;
    }

    public boolean cancelInvitation(String cityName) {
        return pendingInvitations.remove(cityName.toLowerCase()) != null;
    }

    public boolean hasInvitation(String cityName) {
        return getInvitation(cityName).isPresent();
    }

    public void cleanupExpiredInvitations() {
        pendingInvitations.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
