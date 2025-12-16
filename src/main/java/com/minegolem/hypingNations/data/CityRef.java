package com.minegolem.hypingNations.data;

import dev.canable.hypingteams.HypingTeams;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.manager.ClaimManager;
import dev.canable.hypingteams.object.Claim;
import dev.canable.hypingteams.object.Team;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record CityRef(String teamName) {

    public CityRef {
        if (TeamAPI.getTeamByName(teamName) == null) {
            throw new IllegalArgumentException("Team does not exist: " + teamName);
        }
    }

    public Team getTeam() {
        return TeamAPI.getTeamByName(teamName);
    }

    public UUID getMayorUUID() {
        return getTeam().getOwner();
    }

    public Set<UUID> getDeputyUUIDs() {
        Set<UUID> deputies = new HashSet<>();

        getTeam().getRoleMembers().forEach((uuid, roleMember) -> {
            if (roleMember.hasRole("deputy_mayor")) {
                deputies.add(uuid);
            }
        });
        return deputies;
    }

    public boolean isDeputy(UUID player) {
        return getDeputyUUIDs().contains(player);
    }

    public boolean isMayor(UUID player) {
        return getMayorUUID().equals(player);
    }

    public boolean isMember(UUID player) {
        return getTeam().getMembers().contains(player);
    }

    public Location getLocation() {
        return getTeam().getTeamHomes().getDefaultHome();
    }

    public int getChunksCount() {
        Team team = getTeam();
        if (team == null) {
            return 0;
        }

        try {
            HypingTeams hypingTeams = HypingTeams.getInstance();
            if (hypingTeams == null) {
                return 0;
            }

            ClaimManager claimManager = hypingTeams.getClaimManager();
            if (claimManager == null) {
                return 0;
            }

            Claim claim = claimManager.getByTeamName(teamName);
            if (claim == null) {
                return 0;
            }

            return claim.getChunks().size();

        } catch (Exception e) {
            return 0;
        }
    }

    public Set<UUID> getAllMembers() {
        return getTeam().getMembers();
    }
}