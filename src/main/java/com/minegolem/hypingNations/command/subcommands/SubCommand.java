package com.minegolem.hypingNations.command.subcommands;

import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {
    void execute(Player player, String[] args);

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
