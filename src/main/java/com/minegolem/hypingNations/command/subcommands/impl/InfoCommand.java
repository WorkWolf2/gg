package com.minegolem.hypingNations.command.subcommands.impl;

import com.minegolem.hypingNations.HypingNations;
import com.minegolem.hypingNations.command.subcommands.SubCommand;
import com.minegolem.hypingNations.data.Nation;
import com.minegolem.hypingNations.manager.MessageManager;
import com.minegolem.hypingNations.manager.PactManager;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

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
            plugin.getMessageManager().sendMessage(player, "info.not-in-nation");
            return;
        }

        double nextTax = plugin.getTaxManager().calculateTax(nation);
        int unpaidDays = nation.getUnpaidDays();
        int maxUnpaidDays = plugin.getTaxManager().getMaxUnpaidDaysBeforeDissolution();
        String yourRole = plugin.getPermissionManager().getRoleDisplayName(player.getUniqueId(), nation);
        int effectiveRange = plugin.getPermissionManager().getInviteRange(player.getUniqueId(), nation);

        StringBuilder alliesList = new StringBuilder();
        if (!nation.getActivePacts().isEmpty()) {
            alliesList.append(plugin.getMessageManager().getMessage("info.allies-header")).append("\n");
            for (PactManager.Pact pact : nation.getActivePacts().values()) {
                if (pact.isActive() && !pact.isExpired()) {
                    String entry = plugin.getMessageManager().getMessage("info.ally-entry",
                            MessageManager.placeholder()
                                    .add("ally_name", pact.getTarget().getName())
                                    .build()
                    );
                    alliesList.append(entry).append("\n");
                }
            }
        }

        plugin.getMessageManager().sendMessage(player, "info.display",
                MessageManager.placeholder()
                        .add("nation_name", nation.getName())
                        .add("chief_name", plugin.getServer().getOfflinePlayer(nation.getChief()).getName())
                        .add("capital_name", nation.getCapital().teamName())
                        .add("member_cities", nation.getMemberCities().size())
                        .add("total_members", nation.getAllMembers().size())
                        .add("your_role", yourRole)
                        .add("treasury", df.format(nation.getTreasury()))
                        .add("next_tax", df.format(nextTax))
                        .add("taxed_chunks", nation.getTotalChunks())
                        .add("unpaid_days", unpaidDays)
                        .add("max_unpaid_days", maxUnpaidDays)
                        .add("active_pacts", nation.getActivePacts().size())
                        .add("allies_list", alliesList.toString())
                        .add("effective_range", effectiveRange)
                        .build()
        );
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
