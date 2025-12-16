package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.InvitationManager;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;


public class AcceptCityCommand implements SubCommand {
    private final HypingNations plugin;

    public AcceptCityCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        Team playerTeam = TeamAPI.getTeamByPlayer(player);
        if (playerTeam == null) {
            player.sendMessage("§cYou must be in a city!");
            return;
        }

        if (!playerTeam.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§cOnly the mayor can accept nation invitations!");
            return;
        }

        if (plugin.getNationManager().isCityInNation(playerTeam.getName())) {
            player.sendMessage("§cYour city is already part of a nation!");
            return;
        }

        Optional<InvitationManager.Invitation> invitationOpt =
                plugin.getInvitationManager().getInvitation(playerTeam.getName());

        if (invitationOpt.isEmpty()) {
            player.sendMessage("§cYou don't have any pending nation invitations!");
            player.sendMessage("§7Invitations expire after 30 minutes.");
            return;
        }

        InvitationManager.Invitation invitation = invitationOpt.get();
        String nationName = invitation.getNationName();

        Nation nation = plugin.getNationManager().getNation(nationName);
        if (nation == null) {
            player.sendMessage("§cNation not found: " + nationName);
            player.sendMessage("§7The nation may have been dissolved.");
            plugin.getInvitationManager().cancelInvitation(playerTeam.getName());
            return;
        }

        plugin.getInvitationManager().acceptInvitation(playerTeam.getName());
        boolean success = plugin.getNationManager().addCityToNation(nationName, playerTeam.getName());

        if (success) {
            player.sendMessage("§aYour city §e" + playerTeam.getName() + " §ahas joined the nation §e" + nationName + "§a!");

            nation.getRolesManager().assignRole(player.getUniqueId(), "mayor");

            Player chief = plugin.getServer().getPlayer(nation.getChief());
            if (chief != null && chief.isOnline()) {
                chief.sendMessage("§aThe city §e" + playerTeam.getName() + " §ahas joined your nation!");
            }
        } else {
            player.sendMessage("§cFailed to join nation. Please contact an administrator.");
        }
    }

    @Override
    public String getName() {
        return "acceptcity";
    }

    @Override
    public String getDescription() {
        return "Accept an invitation to join a nation";
    }

    @Override
    public String getUsage() {
        return "/hnations acceptcity";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        return List.of();
    }
}