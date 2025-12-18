package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class InviteCityCommand implements SubCommand {
    private final HypingNations plugin;

    public InviteCityCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: " + getUsage());
            return;
        }

        String cityName = args[0];

        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());
        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "invitation.not-in-nation");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), nation, "can_invite_city_to_nation")) {
            plugin.getMessageManager().sendMessage(player, "invitation.no-permission");
            return;
        }

        Team targetTeam = TeamAPI.getTeamByName(cityName);
        if (targetTeam == null) {
            plugin.getMessageManager().sendMessage(player, "invitation.city-not-found",
                    MessageManager.placeholder()
                            .add("city_name", cityName)
                            .build()
            );
            return;
        }

        if (plugin.getNationManager().isCityInNation(cityName)) {
            plugin.getMessageManager().sendMessage(player, "invitation.already-in-nation");
            return;
        }

        if (plugin.getInvitationManager().hasInvitation(cityName)) {
            plugin.getMessageManager().sendMessage(player, "invitation.already-has-invitation");
            return;
        }

        CityRef targetCity = new CityRef(cityName);

        Location cityLoc = targetCity.getLocation().orElse(null);
        if (cityLoc == null || cityLoc.getWorld() == null) {
            plugin.getMessageManager().sendMessage(player, "invitation.no-home");
            return;
        }

        if (!plugin.getRangeManager().isCityInRangeOfAnyMember(nation, targetCity)) {
            int range = plugin.getPermissionManager().getInviteRange(player.getUniqueId(), nation);
            plugin.getMessageManager().sendMessage(player, "invitation.out-of-range",
                    MessageManager.placeholder()
                            .add("max_range", range)
                            .build()
            );
            return;
        }

        plugin.getInvitationManager().createInvitation(nation.getName(), cityName);

        plugin.getMessageManager().sendMessage(player, "invitation.sent",
                MessageManager.placeholder()
                        .add("city_name", cityName)
                        .build()
        );

        Player targetMayor = plugin.getServer().getPlayer(targetTeam.getOwner());
        if (targetMayor != null && targetMayor.isOnline()) {
            plugin.getMessageManager().sendMessage(targetMayor, "invitation.received",
                    MessageManager.placeholder()
                            .add("city_name", cityName)
                            .add("nation_name", nation.getName())
                            .build()
            );
        }
    }

    @Override
    public String getName() {
        return "invitecity";
    }

    @Override
    public String getDescription() {
        return "Invite a city to join your nation";
    }

    @Override
    public String getUsage() {
        return "/hnations invitecity <city_name>";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return TeamAPI.getAllTeamNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
