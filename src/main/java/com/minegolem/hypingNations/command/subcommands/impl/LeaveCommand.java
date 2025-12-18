package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.entity.Player;

import java.util.List;

public class LeaveCommand implements SubCommand {
    private final HypingNations plugin;

    public LeaveCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        Team team = TeamAPI.getTeamByPlayer(player);
        if (team == null) {
            plugin.getMessageManager().sendMessage(player, "leave.must-be-in-team");
            return;
        }

        if (!team.getOwner().equals(player.getUniqueId())) {
            plugin.getMessageManager().sendMessage(player, "leave.must-be-owner");
            return;
        }

        Nation nation = plugin.getNationManager().getNationByCity(team.getName());
        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "leave.not-in-nation");
            return;
        }

        if (nation.getCapital().teamName().equals(team.getName())) {
            plugin.getMessageManager().sendMessage(player, "leave.capital-cannot-leave");
            return;
        }

        boolean success = plugin.getNationManager().removeCityFromNation(nation.getName(), team.getName());

        if (success) {
            plugin.getMessageManager().sendMessage(player, "leave.success",
                    MessageManager.placeholder()
                            .add("nation_name", nation.getName())
                            .build()
            );

            Player chief = plugin.getServer().getPlayer(nation.getChief());
            if (chief != null && chief.isOnline()) {
                plugin.getMessageManager().sendMessage(chief, "leave.notify-chief",
                        MessageManager.placeholder()
                                .add("city_name", team.getName())
                                .build()
                );
            }
        } else {
            plugin.getMessageManager().sendMessage(player, "leave.failed");
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
