package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class TaxCommand implements SubCommand {
    private final HypingNations plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public TaxCommand(HypingNations plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        Nation nation = plugin.getNationManager().getNationByPlayer(player.getUniqueId());

        if (nation == null) {
            plugin.getMessageManager().sendMessage(player, "tax.not-in-nation");
            return;
        }

        double nextTax = plugin.getTaxManager().calculateTax(nation);
        int totalChunks = nation.getTotalChunks();
        double perChunk = plugin.getTaxManager().getPerChunkPrice();
        int unpaidDays = nation.getUnpaidDays();
        int maxUnpaidDays = plugin.getTaxManager().getMaxUnpaidDaysBeforeDissolution();

        String statusMessage;
        if (unpaidDays > 0) {
            int remaining = maxUnpaidDays - unpaidDays;
            statusMessage = plugin.getMessageManager().getMessage("tax.warning",
                    MessageManager.placeholder()
                            .add("remaining_days", remaining)
                            .build()
            );
        } else {
            statusMessage = plugin.getMessageManager().getMessage("tax.up-to-date");
        }

        plugin.getMessageManager().sendMessage(player, "tax.display",
                MessageManager.placeholder()
                        .add("nation_name", nation.getName())
                        .add("next_tax", df.format(nextTax))
                        .add("per_chunk", df.format(perChunk))
                        .add("total_chunks", totalChunks)
                        .add("unpaid_days", unpaidDays)
                        .add("max_unpaid_days", maxUnpaidDays)
                        .add("status_message", statusMessage)
                        .build()
        );
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
