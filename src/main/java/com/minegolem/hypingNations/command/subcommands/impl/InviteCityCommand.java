package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.CityRef;
import com.minegolem.hypingNations.data.Nation;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
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
            player.sendMessage("§cYou are not part of any nation!");
            return;
        }

        if (!nation.getRolesManager().hasPermission(player.getUniqueId(), "can_invite_city_to_nation")) {
            player.sendMessage("§cYou don't have permission to invite cities!");
            return;
        }

        Team targetTeam = TeamAPI.getTeamByName(cityName);
        if (targetTeam == null) {
            player.sendMessage("§cCity not found: " + cityName);
            return;
        }

        if (plugin.getNationManager().isCityInNation(cityName)) {
            player.sendMessage("§cThat city is already part of a nation!");
            return;
        }

        if (plugin.getInvitationManager().hasInvitation(cityName)) {
            player.sendMessage("§cThat city already has a pending invitation!");
            return;
        }

        CityRef targetCity = new CityRef(cityName);
        if (!plugin.getRangeManager().isCityInRangeOfAnyMember(nation, targetCity)) {
            int range = plugin.getRangeManager().calculateEffectiveRange(nation);
            player.sendMessage("§cThat city is too far! Maximum range: " + range + " blocks");
            return;
        }

        plugin.getInvitationManager().createInvitation(nation.getName(), cityName);

        player.sendMessage("§aInvitation sent to §e" + cityName + "§a!");
        player.sendMessage("§7They must use §f/hnations acceptcity " + nation.getName() + " §7to join.");
        player.sendMessage("§7This invitation will expire in 30 minutes.");

        Player targetMayor = plugin.getServer().getPlayer(targetTeam.getOwner());
        if (targetMayor != null && targetMayor.isOnline()) {
            targetMayor.sendMessage("§8§m                                                ");
            targetMayor.sendMessage("§6§lNation Invitation");
            targetMayor.sendMessage("");
            targetMayor.sendMessage("§7Your city §e" + cityName + " §7has been invited to join");
            targetMayor.sendMessage("§7the nation §e" + nation.getName() + "§7!");
            targetMayor.sendMessage("");
            targetMayor.sendMessage("§7Use §f/hnations acceptcity " + nation.getName() + " §7to accept");
            targetMayor.sendMessage("§7This invitation expires in §e30 minutes§7.");
            targetMayor.sendMessage("§8§m                                                ");
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
