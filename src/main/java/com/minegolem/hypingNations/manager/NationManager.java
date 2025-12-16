package com.minegolem.hypingNations.manager;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.service.PersistenceService;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NationManager {

    private final Map<UUID, Nation> nations = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToId = new ConcurrentHashMap<>();
    private final Map<String, UUID> cityToNation = new ConcurrentHashMap<>();

    @Setter
    private PersistenceService persistenceService;

    public void loadNations() {
        if (persistenceService == null) {
            throw new IllegalStateException("PersistenceService not set");
        }

        List<Nation> loadedNations = persistenceService.loadNations();

        for (Nation nation : loadedNations) {
            nations.put(nation.getId(), nation);
            nameToId.put(nation.getName().toLowerCase(), nation.getId());

            for (CityRef city : nation.getMemberCities()) {
                cityToNation.put(city.teamName().toLowerCase(), nation.getId());
            }
        }

        for (Nation nation : nations.values()) {
            for (PactManager.Pact pact : nation.getActivePacts().values()) {
                if (pact.isActive() && !pact.isExpired()) {
                    String key = nation.getName() + "->" + pact.getTarget().getName();
                    HypingNations.instance.getPactManager().getActivePacts().put(key, pact);
                }
            }
        }
    }

    public void saveNations() {
        if (persistenceService == null) {
            throw new IllegalStateException("PersistenceService not set");
        }

        persistenceService.saveNations(nations.values());
    }

    public Nation createNation(String name, UUID chief, String capitalTeamName) {
        if (nameToId.containsKey(name.toLowerCase())) {
            throw new IllegalStateException("Nation already exists: " + name);
        }

        if (cityToNation.containsKey(capitalTeamName.toLowerCase())) {
            throw new IllegalStateException("City already belongs to a nation");
        }

        CityRef capital = new CityRef(capitalTeamName);
        Nation nation = new Nation(name, chief, capital);

        nations.put(nation.getId(), nation);
        nameToId.put(name.toLowerCase(), nation.getId());
        cityToNation.put(capitalTeamName.toLowerCase(), nation.getId());

        return nation;
    }

    public Nation getNation(String name) {
        UUID id = nameToId.get(name.toLowerCase());
        return id != null ? nations.get(id) : null;
    }

    public Nation getNation(UUID id) {
        return nations.get(id);
    }

    public Nation getNationByCity(String cityTeamName) {
        UUID nationId = cityToNation.get(cityTeamName.toLowerCase());
        return nationId != null ? nations.get(nationId) : null;
    }

    public Nation getNationByPlayer(UUID player) {
        return nations.values().stream()
                .filter(n -> n.getCityByUUID(player).isPresent() || n.getAllMembers().contains(player))
                .findFirst()
                .orElse(null);
    }

    public void deleteNation(String name) {
        Nation nation = getNation(name);
        if (nation == null) return;

        nation.getMemberCities().forEach(city ->
                cityToNation.remove(city.teamName().toLowerCase())
        );

        nameToId.remove(name.toLowerCase());
        nations.remove(nation.getId());
    }

    public void deleteNation(UUID id) {
        Nation nation = nations.get(id);
        if (nation != null) {
            deleteNation(nation.getName());
        }
    }

    public boolean addCityToNation(String nationName, String cityTeamName) {
        Nation nation = getNation(nationName);
        if (nation == null) return false;

        if (cityToNation.containsKey(cityTeamName.toLowerCase())) {
            return false;
        }

        CityRef city = new CityRef(cityTeamName);
        nation.addCity(city);
        cityToNation.put(cityTeamName.toLowerCase(), nation.getId());

        return true;
    }

    public boolean removeCityFromNation(String nationName, String cityTeamName) {
        Nation nation = getNation(nationName);
        if (nation == null) return false;

        CityRef city = new CityRef(cityTeamName);
        try {
            nation.removeCity(city);
            cityToNation.remove(cityTeamName.toLowerCase());

            if (persistenceService != null) {
                saveNations();
            }

            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public Set<Nation> getAllNations() {
        return new HashSet<>(nations.values());
    }

    public boolean isNameTaken(String name) {
        return nameToId.containsKey(name.toLowerCase());
    }

    public boolean isCityInNation(String cityTeamName) {
        return cityToNation.containsKey(cityTeamName.toLowerCase());
    }
}

