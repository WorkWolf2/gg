package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {
    private final HypingNations plugin;

    public LeaveCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        Team team = TeamAPI.getTeamByPlayer(player);
        if (team == null) {
            player.sendMessage("§cYou must be in a city!");
            return;
        }

        if (!team.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§cOnly the mayor can make the city leave a nation!");
            return;
        }

        Nation nation = plugin.getNationManager().getNationByCity(team.getName());
        if (nation == null) {
            player.sendMessage("§cYour city is not part of any nation!");
            return;
        }

        if (nation.getCapital().teamName().equals(team.getName())) {
            player.sendMessage("§cThe capital cannot leave the nation!");
            player.sendMessage("§7Use §f/hnations delete §7to disband the nation or transfer capital first.");
            return;
        }

        boolean success = plugin.getNationManager().removeCityFromNation(nation.getName(), team.getName());

        if (success) {
            player.sendMessage("§aYour city has left the nation §e" + nation.getName() + "§a!");

            Player chief = plugin.getServer().getPlayer(nation.getChief());
            if (chief != null && chief.isOnline()) {
                chief.sendMessage("§cThe city §e" + team.getName() + " §chas left your nation!");
            }
        } else {
            player.sendMessage("§cFailed to leave nation. Please contact an administrator.");
        }
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Make your city leave its nation";
    }

    @Override
    public String getUsage() {
        return "/hnations leave";
    }
}
