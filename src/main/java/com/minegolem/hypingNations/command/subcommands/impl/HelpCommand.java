package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import org.bukkit.entity.Player;

import java.util.Map;

public class HelpCommand implements SubCommand {
    private final HypingNations plugin;
    private final Map<String, SubCommand> commands;

    public HelpCommand(HypingNations plugin, Map<String, SubCommand> commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage("§8§m                                                ");
        player.sendMessage("§6§lHypingNations Commands");
        player.sendMessage("");

        for (SubCommand cmd : commands.values()) {
            if (cmd.getName().equals("help")) continue;
            player.sendMessage("§e" + cmd.getUsage());
            player.sendMessage("  §7" + cmd.getDescription());
        }

        player.sendMessage("§8§m                                                ");
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Show this help message";
    }

    @Override
    public String getUsage() {
        return "/hnations help";
    }
}
