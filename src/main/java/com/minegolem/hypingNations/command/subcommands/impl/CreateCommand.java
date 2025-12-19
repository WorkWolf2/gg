package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import dev.canable.hypingteams.HypingTeams;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.manager.ClaimManager;
import dev.canable.hypingteams.object.Claim;
import dev.canable.hypingteams.object.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCommand implements SubCommand {
    private final HypingNations plugin;

    public CreateCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        if (args.length < 1) {
            player.sendMessage("§cUsage: " + getUsage());
            return;
        }

        String nationName = args[0];

        Team team = TeamAPI.getTeamByPlayer(player);
        if (team == null) {
            plugin.getMessageManager().sendMessage(player, "creation.must-be-in-team");
            return;
        }

        if (!team.getOwner().equals(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "creation.must-be-owner");
            return;
        }

        if (plugin.getNationManager().getNationByCity(team.getName()) != null) {
            plugin.getMessageManager().sendMessage(player, "creation.already-in-nation");
            return;
        }

        if (plugin.getNationManager().isNameTaken(nationName)) {
            plugin.getMessageManager().sendMessage(player, "creation.name-taken");
            return;
        }

        int minMembers = plugin.getConfigManager().getNationConfig().getMinMembers();
        int minClaims = plugin.getConfigManager().getNationConfig().getMinClaims();

        if (team.getMembers().size() < minMembers) {
            plugin.getMessageManager().sendMessage(player, "creation.not-enough-members",
                    MessageManager.placeholder()
                            .add("min_members", minMembers)
                            .build()
            );
            return;
        }

        int claimedChunks = getTeamClaimedChunks(team.getName());
        if (claimedChunks < minClaims) {
            plugin.getMessageManager().sendMessage(player, "creation.not-enough-claims",
                    MessageManager.placeholder()
                            .add("min_claims", minClaims)
                            .add("current_claims", claimedChunks)
                            .build()
            );
            return;
        }

        int creationPrice = plugin.getConfigManager().getNationConfig().getCreationPrice();
        if (team.getBalance() < creationPrice) {
            plugin.getMessageManager().sendMessage(player, "creation.not-enough-money",
                    MessageManager.placeholder()
                            .add("creation_price", creationPrice)
                            .build()
            );
            return;
        }

        team.addBalance(-creationPrice);

        Nation nation = plugin.getNationManager().createNation(nationName, player.getUniqueId(), team.getName());

        plugin.getMessageManager().sendMessage(player, "creation.success",
                MessageManager.placeholder()
                        .add("nation_name", nationName)
                        .add("capital_name", team.getName())
                        .build()
        );
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
