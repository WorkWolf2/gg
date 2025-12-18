package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.InvitationManager;
import com.minegolem.hypingNations.manager.MessageManager;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.entity.Player;

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
            plugin.getMessageManager().sendMessage(player, "invitation.must-be-in-team");
            return;
        }

        if (!playerTeam.getOwner().equals(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "invitation.must-be-owner");
            return;
        }

        if (plugin.getNationManager().isCityInNation(playerTeam.getName())) {
            plugin.getMessageManager().sendMessage(player, "invitation.already-in-nation");
            return;
        }

        Optional<InvitationManager.Invitation> invitationOpt =
                plugin.getInvitationManager().getInvitation(playerTeam.getName());

        if (invitationOpt.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "invitation.no-invitation");
            plugin.getMessageManager().sendMessage(player, "invitation.invitation-expired");
            return;
        }

        InvitationManager.Invitation invitation = invitationOpt.get();
        String nationName = invitation.getNationName();

        Nation nation = plugin.getNationManager().getNation(nationName);
        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "invitation.nation-not-found",
                    MessageManager.placeholder()
                            .add("nation_name", nationName)
                            .build()
            );
            plugin.getMessageManager().sendMessage(player, "invitation.nation-dissolved");
            plugin.getInvitationManager().cancelInvitation(playerTeam.getName());
            return;
        }

        plugin.getInvitationManager().acceptInvitation(playerTeam.getName());
        boolean success = plugin.getNationManager().addCityToNation(nationName, playerTeam.getName());

        if (success) {
            plugin.getMessageManager().sendMessage(player, "invitation.accept-success",
                    MessageManager.placeholder()
                            .add("city_name", playerTeam.getName())
                            .add("nation_name", nationName)
                            .build()
            );

            Player chief = plugin.getServer().getPlayer(nation.getChief());
            if (chief != null && chief.isOnline()) {
                plugin.getMessageManager().sendMessage(chief, "invitation.accept-notify-chief",
                        MessageManager.placeholder()
                                .add("city_name", playerTeam.getName())
                                .build()
                );
            }
        } else {
            plugin.getMessageManager().sendMessage(player, "invitation.accept-failed");
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
}