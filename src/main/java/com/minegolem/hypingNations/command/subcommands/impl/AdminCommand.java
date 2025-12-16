package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import org.bukkit.entity.Player;

public class AdminCommand implements SubCommand {

    private final HypingNations plugin;

    public AdminCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("hypingnations.admin")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /hnations admin <reload>");
            return;
        }

        String action = args[0].toLowerCase();

        if (action.equals("reload")) {
            plugin.reload();
            player.sendMessage("§aConfiguration reloaded successfully!");
        } else {
            player.sendMessage("§cUnknown action: " + action);
        }
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Admin commands";
    }

    @Override
    public String getUsage() {
        return "/hnations admin <reload>";
    }

    @Override
    public String getPermission() {
        return "hypingnations.admin";
    }
}
