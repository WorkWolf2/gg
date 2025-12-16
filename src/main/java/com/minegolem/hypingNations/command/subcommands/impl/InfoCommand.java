package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.PactManager;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class InfoCommand implements SubCommand {
    private final HypingNations plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public InfoCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());

        if (nation == null) {
            player.sendMessage("§cYou are not part of any nation!");
            return;
        }

        player.sendMessage("§8§m                                                ");
        player.sendMessage("§6§lNation Info: §e" + nation.getName());
        player.sendMessage("");
        player.sendMessage("§7Chief: §f" + plugin.getServer().getOfflinePlayer(nation.getChief()).getName());
        player.sendMessage("§7Capital: §f" + nation.getCapital().teamName());
        player.sendMessage("§7Member Cities: §f" + nation.getMemberCities().size());
        player.sendMessage("§7Total Members: §f" + nation.getAllMembers().size());
        player.sendMessage("");

        // Tax info
        double nextTax = plugin.getTaxManager().calculateTax(nation);
        int unpaidDays = nation.getUnpaidDays();
        int maxUnpaidDays = plugin.getTaxManager().getMaxUnpaidDaysBeforeDissolution();

        player.sendMessage("§e§lEconomy:");
        player.sendMessage("§7Treasury: §f$" + df.format(nation.getTreasury()));
        player.sendMessage("§7Next Tax: §f$" + df.format(nextTax));
        player.sendMessage("§7Taxed Chunks: §f" + nation.getTotalChunks());
        player.sendMessage("§7Unpaid Days: §f" + unpaidDays + "/" + maxUnpaidDays);
        player.sendMessage("");

        // Pacts info
        player.sendMessage("§b§lDiplomacy:");
        player.sendMessage("§7Active Pacts: §f" + nation.getActivePacts().size());

        if (!nation.getActivePacts().isEmpty()) {
            player.sendMessage("§7Allies:");
            for (PactManager.Pact pact : nation.getActivePacts().values()) {
                if (pact.isActive() && !pact.isExpired()) {
                    Nation ally = pact.getTarget();
                    player.sendMessage("  §f- " + ally.getName());
                }
            }
        }

        // Range info
        int effectiveRange = plugin.getRangeManager().calculateEffectiveRange(nation);
        player.sendMessage("");
        player.sendMessage("§a§lInfluence:");
        player.sendMessage("§7Recruitment Range: §f" + effectiveRange + " blocks");

        player.sendMessage("§8§m                                                ");
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "View information about your nation";
    }

    @Override
    public String getUsage() {
        return "/hnations info";
    }
}
