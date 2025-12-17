package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import dev.canable.hypingteams.HypingTeams;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.manager.ClaimManager;
import dev.canable.hypingteams.object.Claim;
import dev.canable.hypingteams.object.Team;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCommand implements SubCommand {
    private final HypingNations plugin;

    public CreateCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: " + getUsage());
            return;
        }

        String nationName = args[0];

        Team team = TeamAPI.getTeamByPlayer(player);
        if (team == null) {
            player.sendMessage("§cYou must be in a city to create a nation!");
            return;
        }

        if (!team.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§cOnly the mayor can create a nation!");
            return;
        }

        if (plugin.getNationManager().getNationByCity(team.getName()) != null) {
            player.sendMessage("§cYour city is already part of a nation!");
            return;
        }

        if (plugin.getNationManager().isNameTaken(nationName)) {
            player.sendMessage("§cA nation with that name already exists!");
            return;
        }

        int minMembers = plugin.getConfigManager().getNationConfig().getMinMembers();
        int minClaims = plugin.getConfigManager().getNationConfig().getMinClaims();

        if (team.getMembers().size() < minMembers) {
            player.sendMessage("§cYour city needs at least §e" + minMembers + " §cmembers to create a nation!");
            return;
        }

        int claimedChunks = getTeamClaimedChunks(team.getName());
        if (claimedChunks < minClaims) {
            player.sendMessage("§cYour city needs at least §e" + minClaims + " §cclaimed chunks to create a nation!");
            player.sendMessage("§7You currently have §e" + claimedChunks + " §7claimed chunks.");
            return;
        }

        int creationPrice = plugin.getConfigManager().getNationConfig().getCreationPrice();
        if (team.getBalance() < creationPrice) {
            player.sendMessage("§cYour city needs §e$" + creationPrice + " §cto create a nation!");
            return;
        }

        team.addBalance(-creationPrice);

        Nation nation = plugin.getNationManager().createNation(nationName, player.getUniqueId(), team.getName());

        // NOTA: Non serve più caricare/assegnare ruoli
        // Il player è automaticamente Chief perché è salvato come chief nella nazione
        // Il sistema verificherà: if (nation.getChief().equals(player.getUniqueId()))

        player.sendMessage("§aSuccessfully created the nation §e" + nationName + "§a!");
        player.sendMessage("§aYour city §e" + team.getName() + " §ais now the capital.");
        player.sendMessage("§7You are now the §6Chief §7of the nation.");
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a new nation from your city";
    }

    @Override
    public String getUsage() {
        return "/hnations create <name>";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return List.of("<nation_name>");
        }
        return List.of();
    }

    private int getTeamClaimedChunks(String teamName) {
        try {
            HypingTeams hypingTeams = HypingTeams.getInstance();
            ClaimManager claimManager = hypingTeams.getClaimManager();
            Claim claim = claimManager.getByTeamName(teamName);

            return claim != null ? claim.getChunks().size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
