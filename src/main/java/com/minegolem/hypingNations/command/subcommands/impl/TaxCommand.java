package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class TaxCommand implements SubCommand {
    private final HypingNations plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public TaxCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());

        if (nation == null) {
            player.sendMessage("§cYou are not part of any nation!");
            return;
        }

        double nextTax = plugin.getTaxManager().calculateTax(nation);
        int totalChunks = nation.getTotalChunks();
        double perChunk = plugin.getTaxManager().getPerChunkPrice();
        int unpaidDays = nation.getUnpaidDays();
        int maxUnpaidDays = plugin.getTaxManager().getMaxUnpaidDaysBeforeDissolution();

        player.sendMessage("§8§m                                                ");
        player.sendMessage("§e§lTax Information: §f" + nation.getName());
        player.sendMessage("");
        player.sendMessage("§7Next Tax Payment: §f$" + df.format(nextTax));
        player.sendMessage("§7Per Chunk Price: §f$" + df.format(perChunk));
        player.sendMessage("§7Taxed Chunks: §f" + totalChunks);
        player.sendMessage("");
        player.sendMessage("§7Unpaid Days: §f" + unpaidDays + "/" + maxUnpaidDays);

        if (unpaidDays > 0) {
            int remaining = maxUnpaidDays - unpaidDays;
            player.sendMessage("§c⚠ Warning: Nation will dissolve in " + remaining + " days!");
        } else {
            player.sendMessage("§a✓ Tax payments are up to date");
        }

        player.sendMessage("");
        player.sendMessage("§7Next Collection: §fDaily at midnight");
        player.sendMessage("§7Source: §fCapital city balance");
        player.sendMessage("§8§m                                                ");
    }

    @Override
    public String getName() {
        return "tax";
    }

    @Override
    public String getDescription() {
        return "View tax information";
    }

    @Override
    public String getUsage() {
        return "/hnations tax";
    }
}
