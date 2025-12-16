package com.minegolem.hypingNations.data;

import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.manager.PactManager;
import com.minegolem.hypingNations.role.NationRolesManager;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Nation {

    private final UUID id;
    private String name;
    private UUID chief;
    private CityRef capital;
    private final Set<CityRef> memberCities = new HashSet<>();
    private final NationRolesManager rolesManager = new NationRolesManager();

    private final Map<UUID, PactManager.Pact> activePacts = new HashMap<>();

    private double treasury = 0.0;
    private int taxedChunks = 0;
    private int unpaidDays = 0;

    public Nation(UUID id, String name, UUID chief, CityRef capital) {
        this.id = id;
        this.name = name;
        this.chief = chief;
        this.capital = capital;

        memberCities.add(capital);

        rolesManager.assignRole(chief, "chief");
    }

    public Nation(String name, UUID chief, CityRef capital) {
        this(UUID.randomUUID(), name, chief, capital);
    }

    public Set<CityRef> getMemberCities() {
        return Collections.unmodifiableSet(memberCities);
    }

    public void addCity(CityRef city) {
        memberCities.add(city);
    }

    public void removeCity(CityRef city) {
        if (city.equals(capital)) {
            throw new IllegalStateException("Cannot remove capital city from nation");
        }
        memberCities.remove(city);
    }

    public Optional<CityRef> getCityByUUID(UUID player) {
        return memberCities.stream()
                .filter(c -> c.getMayorUUID().equals(player) || c.getDeputyUUIDs().contains(player))
                .findFirst();
    }

    public void addToTreasury(double amount) {
        treasury += amount;
    }

    public void removeFromTreasury(double amount) {
        treasury = Math.max(0, treasury - amount);
    }

    public void incrementUnpaidDays() {
        unpaidDays++;
    }

    public void resetUnpaidDays() {
        unpaidDays = 0;
    }

    public Map<UUID, PactManager.Pact> getActivePacts() {
        return Collections.unmodifiableMap(activePacts);
    }

    public void addPact(PactManager.Pact pact) {
        activePacts.put(pact.getTarget().getId(), pact);
    }

    public void removePact(UUID nationId) {
        activePacts.remove(nationId);
    }

    public boolean hasActivePactWith(UUID nationId) {
        PactManager.Pact pact = activePacts.get(nationId);
        return pact != null && pact.isActive() && !pact.isExpired();
    }

    public int getTotalChunks() {
        return memberCities.stream()
                .mapToInt(CityRef::getChunksCount)
                .sum();
    }

    public Set<UUID> getAllMembers() {
        Set<UUID> allMembers = new HashSet<>();
        memberCities.forEach(city -> allMembers.addAll(city.getAllMembers()));
        return allMembers;
    }
}