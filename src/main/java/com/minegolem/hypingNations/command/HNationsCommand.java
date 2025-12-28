package com.minegolem.hypingNations.command;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.command.subcommands.impl.*;
import com.minegolem.hypingNations.command.subcommands.impl.menu.ManageCitiesCommand;
import com.minegolem.hypingNations.command.subcommands.impl.menu.ManageNationCommand;
import com.minegolem.hypingNations.command.subcommands.impl.menu.ManagePactsCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HNationsCommand implements CommandExecutor, TabCompleter {

    private final HypingNations plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public HNationsCommand(HypingNations plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }

    private void registerSubCommands() {
        subCommands.put("create", new CreateCommand(plugin));
        subCommands.put("info", new InfoCommand(plugin));
        subCommands.put("invitecity", new InviteCityCommand(plugin));
        subCommands.put("acceptcity", new AcceptCityCommand(plugin));
        subCommands.put("leave", new LeaveCommand(plugin));
        subCommands.put("delete", new DeleteCommand(plugin));
        subCommands.put("pact", new PactCommand(plugin));
        subCommands.put("tax", new TaxCommand(plugin));
        subCommands.put("admin", new AdminCommand(plugin));
        subCommands.put("help", new HelpCommand(plugin, subCommands));
        subCommands.put("managecities", new ManageCitiesCommand(plugin));
        subCommands.put("managenations", new ManageNationCommand(plugin));
        subCommands.put("managepacts", new ManagePactsCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            SubCommand manage = subCommands.get("managenations");
            if (manage != null) {
                manage.execute(sender, args);
            }
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            plugin.getMessageManager().sendMessage(sender, "general.unknown-command");
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            plugin.getMessageManager().sendMessage(sender, "general.no-permission");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .toList();
        }

        if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.onTabComplete(player, subArgs);
            }
        }

        return List.of();
    }
}
