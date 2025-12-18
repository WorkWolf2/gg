package com.minegolem.hypingNations.data;

import dev.canable.hypingteams.HypingTeams;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.manager.ClaimManager;
import dev.canable.hypingteams.object.Claim;
import dev.canable.hypingteams.object.Team;
import dev.canable.hypingteams.object.claim.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public record CityRef(String teamName) {

    public Optional<Team> getTeam() {
        return Optional.ofNullable(TeamAPI.getTeamByName(teamName));
    }

    public boolean exists() {
        return getTeam().isPresent();
    }

    public Optional<UUID> getMayorUUID() {
        return getTeam().map(Team::getOwner);
    }

    public Set<UUID> getDeputyUUIDs() {
        return getTeam()
                .map(team -> {
                    Set<UUID> deputies = new HashSet<>();
                    team.getRoleMembers().forEach((uuid, roleMember) -> {
                        if (roleMember.hasRole("deputy_mayor")) {
                            deputies.add(uuid);
                        }
                    });
                    return deputies;
                })
                .orElse(Collections.emptySet());
    }

    public boolean isMayor(UUID player) {
        return getMayorUUID()
                .map(player::equals)
                .orElse(false);
    }

    public boolean isDeputy(UUID player) {
        return getDeputyUUIDs().contains(player);
    }

    public boolean isMember(UUID player) {
        return getTeam()
                .map(team -> team.getMembers().contains(player))
                .orElse(false);
    }

    public Set<UUID> getAllMembers() {
        return getTeam()
                .map(Team::getMembers)
                .orElse(Collections.emptySet());
    }

    public Optional<Location> getLocation() {
        return getTeam()
                .flatMap(this::getHomeLocation)
                .or(() -> getTeam().flatMap(this::getClaimFallbackLocation));
    }

    private Optional<Location> getHomeLocation(Team team) {
        try {
            Location home = team.getTeamHomes().getDefaultHome();
            if (home != null && home.getWorld() != null) {
                return Optional.of(home);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private Optional<Location> getClaimFallbackLocation(Team team) {
        HypingTeams plugin = HypingTeams.getInstance();
        if (plugin == null) return Optional.empty();

        ClaimManager claimManager = plugin.getClaimManager();
        if (claimManager == null) return Optional.empty();

        Claim claim = claimManager.getByTeamName(team.getName());
        if (claim == null || claim.getChunks().isEmpty()) return Optional.empty();

        Chunk chunk = claim.getChunks().iterator().next();
        World world = Bukkit.getWorld(chunk.getWorldName());
        if (world == null) return Optional.empty();

        int x = chunk.getX() * 16 + 8;
        int z = chunk.getZ() * 16 + 8;

        return Optional.of(new Location(world, x + 0.5, 64, z + 0.5));
    }

    public int getChunksCount() {
        HypingTeams plugin = HypingTeams.getInstance();
        if (plugin == null) return 0;

        ClaimManager claimManager = plugin.getClaimManager();
        if (claimManager == null) return 0;

        return Optional.ofNullable(claimManager.getByTeamName(teamName))
                .map(claim -> claim.getChunks().size())
                .orElse(0);
    }

    @Override
    public String toString() {
        return "CityRef{" +
                "teamName='" + teamName + '\'' +
                ", exists=" + exists() +
                '}';
    }
}