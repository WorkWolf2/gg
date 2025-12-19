package com.minegolem.hypingNations.command.subcommands.impl.menu;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ManagePactsCommand implements SubCommand {

    private final HypingNations plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) return;

        if (args.length < 1) {
            sender.sendMessage(this.getUsage());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            plugin.getMessageManager().sendMessage(sender, "general.no-player-found");
            return;
        }

        plugin.getMenuManager().openManagePactsMenu(target, 0);
    }

    @Override
    public String getName() {
        return "managepacts";
    }

    @Override
    public String getDescription() {
        return "Manage Pacts Command";
    }

    @Override
    public String getUsage() {
        return "/hnations managepacts <player>";
    }
}
