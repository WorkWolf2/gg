package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import dev.canable.hypingteams.api.TeamAPI;
import dev.canable.hypingteams.object.Team;
import org.bukkit.entity.Player;

import java.util.List;

public class DeleteCommand implements SubCommand {
    private final HypingNations plugin;

    public DeleteCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());

        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "delete.not-in-nation");
            return;
        }

        if (!plugin.getPermissionManager().hasPermission(player.getUniqueId(), nation, "can_delete_nation")) {
            plugin.getMessageManager().sendMessage(player, "delete.no-permission");
            return;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            plugin.getMessageManager().sendMessage(player, "delete.confirm-required",
                    MessageManager.placeholder()
                            .add("nation_name", nation.getName())
                            .build()
            );
            return;
        }

        String nationName = nation.getName();
        int memberCities = nation.getMemberCities().size();
        int totalMembers = nation.getAllMembers().size();

        for (var cityRef : nation.getMemberCities()) {
            Team team = TeamAPI.getTeamByName(cityRef.teamName());
            if (team != null) {
                team.getMembers().forEach(memberId -> {
                    Player member = plugin.getServer().getPlayer(memberId);
                    if (member != null && member.isOnline() && !member.equals(player)) {
                        plugin.getMessageManager().sendMessage(member, "delete.notify-members",
                                MessageManager.placeholder()
                                        .add("nation_name", nationName)
                                        .add("chief_name", player.getName())
                                        .build()
                        );
                    }
                });
            }
        }

        plugin.getNationManager().deleteNation(nationName);
        plugin.getPersistenceService().deleteNation(nation.getId());

        plugin.getMessageManager().sendMessage(player, "delete.success",
                MessageManager.placeholder()
                        .add("nation_name", nationName)
                        .add("member_cities", memberCities)
                        .add("total_members", totalMembers)
                        .build()
        );

        plugin.getLogger().info(String.format(
                "Nation '%s' deleted by %s (UUID: %s). Had %d cities and %d total members.",
                nationName, player.getName(), player.getUniqueId(), memberCities, totalMembers
        ));
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "Delete your nation permanently";
    }

    @Override
    public String getUsage() {
        return "/hnations delete confirm";
    }

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return List.of("confirm").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
