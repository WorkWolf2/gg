package com.minegolem.hypingNations.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {

    void execute(CommandSender sender, String[] args);

    default List<String> onTabComplete(Player player, String[] args) {
        return List.of();
    }

    String getName();

    String getDescription();

    String getUsage();

    default String getPermission() {
        return "hypingnations." + getName();
    }
}
