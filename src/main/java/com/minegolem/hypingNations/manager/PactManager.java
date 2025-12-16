package com.minegolem.hypingNations.manager;

import com.minegolem.hypingNations.data.Nation;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PactManager {

    @Getter
    private final Map<String, Pact> activePacts = new ConcurrentHashMap<>();

    @Getter
    public static class Pact {
        private final Nation proposer;
        private final Nation target;
        private final int durationDays;
        private LocalDate startDate;
        private boolean active;

        public Pact(Nation proposer, Nation target, int durationDays) {
            this.proposer = proposer;
            this.target = target;
            this.durationDays = durationDays;
            this.active = false;
        }

        public void activate() {
            this.active = true;
            this.startDate = LocalDate.now();
        }

        public boolean isExpired() {
            if (!active) return false;
            return startDate.plusDays(durationDays).isBefore(LocalDate.now());
        }
    }

    public Pact proposePact(Nation proposer, Nation target, int durationDays) {
        String key = proposer.getName() + "->" + target.getName();
        Pact pact = new Pact(proposer, target, durationDays);
        activePacts.put(key, pact);
        return pact;
    }

    public boolean acceptPact(Nation proposer, Nation target) {
        String key = proposer.getName() + "->" + target.getName();
        Pact pact = activePacts.get(key);
        if (pact != null && !pact.isActive()) {
            pact.activate();

            Pact reversePact = new Pact(target, proposer, pact.getDurationDays());
            reversePact.active = true;
            reversePact.startDate = pact.getStartDate();

            String reverseKey = target.getName() + "->" + proposer.getName();
            activePacts.put(reverseKey, reversePact);

            proposer.addPact(pact);
            target.addPact(reversePact);

            return true;
        }
        return false;
    }

    public boolean denyPact(Nation proposer, Nation target) {
        String key = proposer.getName() + "->" + target.getName();
        return activePacts.remove(key) != null;
    }

    public boolean breakPact(Nation initiator, Nation target) {
        String key1 = initiator.getName() + "->" + target.getName();
        String key2 = target.getName() + "->" + initiator.getName();

        boolean removed1 = activePacts.remove(key1) != null;
        boolean removed2 = activePacts.remove(key2) != null;

        if (removed1 || removed2) {
            initiator.removePact(target.getId());
            target.removePact(initiator.getId());
            return true;
        }

        return false;
    }

    public boolean isOverclaimBlocked(Nation a, Nation b) {
        String key1 = a.getName() + "->" + b.getName();
        String key2 = b.getName() + "->" + a.getName();

        Pact p1 = activePacts.get(key1);
        Pact p2 = activePacts.get(key2);

        return (p1 != null && p1.isActive() && !p1.isExpired()) ||
                (p2 != null && p2.isActive() && !p2.isExpired());
    }

    public void cleanupExpiredPacts() {
        activePacts.entrySet().removeIf(entry -> {
            Pact pact = entry.getValue();
            if (pact.isExpired()) {
                // Remove from both nations
                pact.getProposer().removePact(pact.getTarget().getId());
                pact.getTarget().removePact(pact.getProposer().getId());
                return true;
            }
            return false;
        });
    }

    public void restorePact(String key, Pact pact) {
        activePacts.put(key, pact);
    }
}