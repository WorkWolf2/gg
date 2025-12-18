package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.manager.MessageManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

public class AdminCommand implements SubCommand {

    private final HypingNations plugin;

    public AdminCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission("hypingnations.admin")) {
            plugin.getMessageManager().sendMessage(player, "general.no-permission");
            return;
        }

        if (args.length < 1) {
            plugin.getMessageManager().sendMessage(player, "admin.usage");
            return;
        }

        String action = args[0].toLowerCase();

        if (action.equals("reload")) {
            plugin.reload();
            plugin.getMessageManager().sendMessage(player, "general.reload-success");
        } else {
            plugin.getMessageManager().sendMessage(player, "admin.unknown-action",
                    MessageManager.placeholder()
                            .add("action", action)
                            .build()
            );
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

    @Override
    public List<String> onTabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
