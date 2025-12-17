package com.minegolem.hypingNations.manager;

import com.minegolem.hypingNations.config.NationConfig;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RangeManager {

    private final NationConfig config;

    private final Map<String, Integer> rangeCache = new ConcurrentHashMap<>();

    public RangeManager(NationConfig config) {
        this.config = config;
    }

    public int calculateEffectiveRange(Nation nation) {
        return rangeCache.computeIfAbsent(nation.getId().toString(),
                k -> config.getBaseRecruitRange() +
                        config.getPerCityBonusRange() * (nation.getMemberCities().size() - 1));
    }

    public void invalidateCache(String nationId) {
        rangeCache.remove(nationId);
    }

    public boolean isCityInRange(Nation nation, CityRef city) {
        int effectiveRange = calculateEffectiveRange(nation);

        Location capitalLoc = nation.getCapital().getLocation().orElse(null);
        Location cityLoc = city.getLocation().orElse(null);

        if (capitalLoc == null || cityLoc == null) return false;
        if (capitalLoc.getWorld() == null || cityLoc.getWorld() == null) {
            return false;
        }

        if (!capitalLoc.getWorld().equals(cityLoc.getWorld())) {
            return false;
        }

        double distance = capitalLoc.distance(cityLoc);
        return distance <= effectiveRange;
    }

    public boolean isCityInRangeOfAnyMember(Nation nation, CityRef targetCity) {
        int effectiveRange = calculateEffectiveRange(nation);
        Location targetLoc = targetCity.getLocation().orElse(null);

        if (targetLoc == null) return false;
        if (targetLoc.getWorld() == null) {
            return false;
        }
        return nation.getMemberCities().stream()
                .map(CityRef::getLocation)
                .filter(loc -> loc.get().getWorld() != null && loc.get().getWorld().equals(targetLoc.getWorld()))
                .anyMatch(loc -> loc.get().distance(targetLoc) <= effectiveRange);
    }
}
