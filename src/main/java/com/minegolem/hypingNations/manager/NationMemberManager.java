package com.minegolem.hypingNations.manager;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.role.NationRole;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NationMemberManager {

    private final HypingNations plugin;
    private final Map<UUID, Map<UUID, NationRole>> nationRoles = new ConcurrentHashMap<>();

    public NationMemberManager(HypingNations plugin) {
        this.plugin = plugin;
    }

    public NationRole getRole(UUID nationId, UUID playerId) {
        Nation nation = plugin.getNationManager().getNation(nationId);
        if (nation == null) return NationRole.CITIZEN;

        // Chief is always CHIEF
        if (nation.getChief().equals(playerId)) {
            return NationRole.CHIEF;
        }

        // Check stored roles
        Map<UUID, NationRole> roles = nationRoles.get(nationId);
        if (roles != null && roles.containsKey(playerId)) {
            return roles.get(playerId);
        }

        // Default to CITIZEN
        return NationRole.CITIZEN;
    }

    public void setRole(UUID nationId, UUID playerId, NationRole role) {
        Nation nation = plugin.getNationManager().getNation(nationId);
        if (nation == null) return;

        // Cannot change chief's role
        if (nation.getChief().equals(playerId)) return;

        nationRoles.computeIfAbsent(nationId, k -> new ConcurrentHashMap<>())
                .put(playerId, role);

        // Save to database
        plugin.getDatabaseManager().saveNationRoleAsync(nationId, playerId, role);
    }

    public void removeRole(UUID nationId, UUID playerId) {
        Map<UUID, NationRole> roles = nationRoles.get(nationId);
        if (roles != null) {
            roles.remove(playerId);
        }

        plugin.getDatabaseManager().deleteNationRoleAsync(nationId, playerId);
    }

    public Map<UUID, NationRole> getAllRoles(UUID nationId) {
        return new ConcurrentHashMap<>(
                nationRoles.getOrDefault(nationId, new ConcurrentHashMap<>())
        );
    }

    public void loadRoles(UUID nationId) {
        try {
            Map<UUID, NationRole> roles = plugin.getDatabaseManager().loadNationRoles(nationId);
            nationRoles.put(nationId, new ConcurrentHashMap<>(roles));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load roles for nation " + nationId);
        }
    }

    public void clearNationRoles(UUID nationId) {
        nationRoles.remove(nationId);
        plugin.getDatabaseManager().deleteAllNationRolesAsync(nationId);
    }


    public boolean hasPermission(UUID nationId, UUID playerId, String permission) {
        NationRole role = getRole(nationId, playerId);
        return role.hasPermission(permission);
    }
}
